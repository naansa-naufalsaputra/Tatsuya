package com.example.tatsuya.data.repository

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.tatsuya.data.local.MangaDao
import com.example.tatsuya.data.local.MangaEntity
import com.example.tatsuya.data.mapper.toDomain
import com.example.tatsuya.data.mapper.toEntity
import com.example.tatsuya.data.source.KomikCastSource
import com.example.tatsuya.data.source.MangaDexSource
import com.example.tatsuya.data.source.MangaSource
import com.example.tatsuya.domain.model.Chapter
import com.example.tatsuya.domain.model.Manga
import com.example.tatsuya.domain.model.Page
import com.example.tatsuya.domain.repository.MangaRepository
import com.example.tatsuya.util.Resource
import com.example.tatsuya.worker.ChapterDownloadWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject

class MangaRepositoryImpl @Inject constructor(
    private val mangaDexSource: MangaDexSource,
    private val komikCastSource: KomikCastSource,
    private val dao: MangaDao,
    private val workManager: WorkManager,
    @ApplicationContext private val context: Context
) : MangaRepository {

    // Helper: Pilih source berdasarkan ID
    private fun getSource(mangaId: String): MangaSource {
        return if (mangaId.startsWith("kc-")) komikCastSource else mangaDexSource
    }

    override suspend fun getPopularManga(page: Int, tagId: String?): Resource<List<Manga>> {
        return try {
            // Priority: MangaDex (karena API lebih stabil & support genre filter)
            // Bisa diganti logic: jika tagId=null, gabungkan 2 sumber?
            // Untuk MVP: Pakai MangaDexSource utama, dan tambahkan KomikCast di page 1 jika tanpa filter
            
            val dexManga = mangaDexSource.getPopularManga(page, tagId)
            
            val result = if (page == 1 && tagId == null) {
                // Contoh: Gabung hasil KomikCast di awal (experimental)
                // val kcManga = komikCastSource.getPopularManga(1)
                // dexManga + kcManga
                dexManga // Sementara hanya return MangaDex agar UI rapi
            } else {
                dexManga
            }
            
            Resource.Success(result)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown Error")
        }
    }

    override suspend fun searchManga(query: String): Resource<List<Manga>> {
        return try {
            val dexResults = try {
                mangaDexSource.searchManga(query)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
            
            val kcResults = try {
                komikCastSource.searchManga(query)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
            
            // Gabungkan hasil: KomikCast dulu baru MangaDex (atau sebaliknya)
            if (dexResults.isEmpty() && kcResults.isEmpty()) {
                 Resource.Error("Tidak ada hasil ditemukan di kedua sumber.")
            } else {
                 Resource.Success(kcResults + dexResults)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Search Error")
        }
    }

    override suspend fun getMangaDetails(mangaId: String): Resource<Manga> {
        return try {
            val source = getSource(mangaId)
            val mangaFromSource = source.getMangaDetails(mangaId)
            
            // KomikCast getMangaDetails mungkin belum include chapters lengkap di satu request (tergantung implementasi),
            // Tapi di implementasi source saya tadi, chapter list kosong, jadi kita harus fetch manual jika kosong?
            // Oh tunggu, implementasi KomikCastSource saya tadi 'chapters = emptyList()' di getMangaDetails.
            // Kita perlu fetch chapters terpisah dan merge.
            
            var chapters = if (mangaFromSource.chapters.isEmpty()) {
                source.getChapterList(mangaId)
            } else {
                mangaFromSource.chapters
            }

            // Sync Reading Progress (Local DB)
            val progressList = dao.getReadingProgressSync(mangaId)
            val progressMap = progressList.associateBy { it.chapterId }
            
            val chaptersWithProgress = chapters.map { chapter ->
                val p = progressMap[chapter.id]
                chapter.copy(
                    isRead = p != null && p.pageNumber >= p.totalPages - 1,
                    lastPageRead = p?.pageNumber ?: 0,
                    totalPages = p?.totalPages ?: 0
                )
            }
            
            val totalChapters = chaptersWithProgress.size
            val readCount = chaptersWithProgress.count { it.isRead }
            val totalProgress = if (totalChapters > 0) (readCount * 100 / totalChapters) else 0

            val completeManga = mangaFromSource.copy(
                chapters = chaptersWithProgress,
                totalProgress = totalProgress
            )
            
            // Update Entity Total for Notif
             dao.updateTotalChapters(mangaId, totalChapters)
            
            Resource.Success(completeManga)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Detail Error")
        }
    }

    override suspend fun getChapterPages(chapterId: String): Resource<List<Page>> {
        return try {
            val downloadEntity = dao.getDownload(chapterId)

            if (downloadEntity != null) {
                // Offline Mode
                val dir = File(context.filesDir, downloadEntity.savedPath)
                if (dir.exists()) {
                    val files = dir.listFiles()?.sortedBy { it.nameWithoutExtension.toIntOrNull() ?: 999 }
                    if (!files.isNullOrEmpty()) {
                        val pages = files.mapIndexed { index, file ->
                            Page(index, file.absolutePath, chapterId) 
                        }
                        return Resource.Success(pages)
                    }
                }
            }
            
            // Online Mode (Fallback)
            val source = getSource(chapterId.substringAfterLast("kc-").let { if (chapterId.startsWith("kc-")) chapterId else "" }.ifEmpty { "mangadex" })
            // Logic deteksi source ID chapter agak tricky kalau chapterId MangaDex tidak punya prefix.
            // Tapi karena kita passing ID chapter yang digenerate oleh Source, kita bisa cek prefix "kc-" nya.
            
            val targetSource = if (chapterId.startsWith("kc-")) komikCastSource else mangaDexSource
            val pages = targetSource.getPageList(chapterId)
            Resource.Success(pages)
        } catch (e: Exception) {
            Resource.Error("Gagal muat gambar: " + e.message)
        }
    }

    override suspend fun getChapter(chapterId: String): Resource<Chapter> {
        return try {
             val source = getSource(chapterId.substringAfterLast("kc-").let { if (chapterId.startsWith("kc-")) chapterId else "" }.ifEmpty { "mangadex" })
             // Logic deteksi ulang ID source
             val targetSource = if (chapterId.startsWith("kc-")) komikCastSource else mangaDexSource
             
             val chapter = targetSource.getChapterDetails(chapterId)
             Resource.Success(chapter)
        } catch (e: Exception) {
            Resource.Error("Gagal muat info chapter: " + e.message)
        }
    }

    override fun getFavoriteManga(): Flow<List<Manga>> {
        return dao.getFavoriteManga().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getLibraryManga(): Flow<List<Manga>> {
        return dao.getLibraryManga().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun addToLibrary(manga: Manga) {
        dao.insertManga(manga.toEntity())
    }

    override suspend fun removeFromLibrary(mangaId: String) {
        dao.deleteMangaById(mangaId)
    }

    override fun getHistory(): Flow<List<Manga>> {
        return combine(
            dao.getHistory(),
            dao.getAllReadingProgress()
        ) { mangas, allProgress ->
            // Group progress by mangaId for fast lookup
            val progressMap = allProgress.groupBy { it.mangaId }

            mangas.map { entity ->
                // Find latest progress for this specific manga
                val pList = progressMap[entity.id]
                val latestProgress = pList?.maxByOrNull { it.lastReadAt }
                
                android.util.Log.d("DEBUG_HISTORY", "Manga: ${entity.id} (${entity.title}), Progress Found: ${latestProgress != null}")
                
                var historyStr: String? = null
                
                if (latestProgress != null) {
                    val chapTitle = latestProgress.chapterTitle
                    val pct = if (latestProgress.totalPages > 0) (latestProgress.pageNumber * 100 / latestProgress.totalPages) else 0
                    historyStr = "Ch. $chapTitle - Page ${latestProgress.pageNumber}/${latestProgress.totalPages} ($pct%)"
                }

                entity.toDomain().copy(historyText = historyStr)
            }
        }
    }

    override suspend fun updateLastRead(manga: Manga) {
        val existing = dao.getMangaById(manga.id)
        if (existing != null) {
            dao.updateLastRead(manga.id, System.currentTimeMillis())
        } else {
            // Jika manga belum ada di DB (bukan favorite), insert dengan isFavorite=false
            dao.insertManga(manga.toEntity().copy(
                lastRead = System.currentTimeMillis(),
                isFavorite = false
            ))
        }
    }

    override suspend fun saveReadingProgress(
        chapterId: String,
        mangaId: String,
        chapterTitle: String,
        page: Int,
        totalPages: Int
    ) {
        val entity = com.example.tatsuya.data.local.ChapterProgressEntity(
            chapterId = chapterId,
            mangaId = mangaId,
            chapterTitle = chapterTitle,
            pageNumber = page,
            totalPages = totalPages,
            lastReadAt = System.currentTimeMillis()
        )
        dao.insertChapterProgress(entity)
        dao.updateLastRead(mangaId, System.currentTimeMillis())
    }

    override fun downloadChapter(chapter: Chapter) {
        val data = workDataOf(
            "chapterId" to chapter.id,
            "mangaId" to chapter.mangaId,
            "chapterName" to chapter.name
        )

        val request = OneTimeWorkRequestBuilder<ChapterDownloadWorker>()
            .setInputData(data)
            .build()

        workManager.enqueue(request)
    }

    override fun getDownloadedChapters(mangaId: String): Flow<List<String>> {
        return dao.getDownloadsForManga(mangaId).map { list -> list.map { it.chapterId } }
    }

    override fun getReadingProgress(mangaId: String): Flow<List<com.example.tatsuya.data.local.ChapterProgressEntity>> {
        return dao.getReadingProgress(mangaId)
    }
}
