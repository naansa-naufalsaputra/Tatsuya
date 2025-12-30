package com.example.tatsuya.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MangaDao {

    // --- BAGIAN FAVORITE (LAMA) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManga(manga: MangaEntity)

    @Query("DELETE FROM manga_table WHERE id = :mangaId")
    suspend fun deleteMangaById(mangaId: String)

    @Query("SELECT * FROM manga_table")
    fun getFavoriteManga(): Flow<List<MangaEntity>>

    @Query("SELECT EXISTS(SELECT * FROM manga_table WHERE id = :mangaId)")
    fun isMangaFavorite(mangaId: String): Flow<Boolean>

    @Query("SELECT * FROM manga_table WHERE id = :mangaId")
    suspend fun getMangaById(mangaId: String): MangaEntity?

    // --- BAGIAN HISTORY (BARU) ---
    @Query("SELECT * FROM manga_table WHERE lastRead IS NOT NULL ORDER BY lastRead DESC")
    fun getHistory(): Flow<List<MangaEntity>>

    @Query("UPDATE manga_table SET lastRead = :timestamp WHERE id = :mangaId")
    suspend fun updateLastRead(mangaId: String, timestamp: Long)

    @Query("UPDATE manga_table SET totalChapters = :total WHERE id = :mangaId")
    suspend fun updateTotalChapters(mangaId: String, total: Int)




    // --- BAGIAN PROGRESS (Advanced History) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapterProgress(progress: ChapterProgressEntity)

    @Query("SELECT * FROM chapter_progress WHERE chapterId = :chapterId")
    suspend fun getChapterProgress(chapterId: String): ChapterProgressEntity?

    @Query("SELECT * FROM chapter_progress WHERE mangaId = :mangaId")
    fun getReadingProgress(mangaId: String): Flow<List<ChapterProgressEntity>>

    @Query("SELECT * FROM chapter_progress WHERE mangaId = :mangaId ORDER BY lastReadAt DESC LIMIT 1")
    suspend fun getLatestProgressForManga(mangaId: String): ChapterProgressEntity?

    @Query("SELECT * FROM chapter_progress")
    fun getAllReadingProgress(): Flow<List<ChapterProgressEntity>>

    @Query("SELECT * FROM chapter_progress WHERE mangaId = :mangaId")
    suspend fun getReadingProgressSync(mangaId: String): List<ChapterProgressEntity>

    // --- Downloads ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(entity: DownloadedChapterEntity)

    @Query("DELETE FROM downloaded_chapters WHERE chapterId = :chapterId")
    suspend fun deleteDownload(chapterId: String)

    @Query("SELECT * FROM downloaded_chapters WHERE chapterId = :chapterId")
    suspend fun getDownload(chapterId: String): DownloadedChapterEntity?

    @Query("SELECT * FROM downloaded_chapters WHERE mangaId = :mangaId")
    fun getDownloadsForManga(mangaId: String): Flow<List<DownloadedChapterEntity>>

    @Query("""
        SELECT * FROM manga_table 
        WHERE isFavorite = 1 
        OR id IN (SELECT DISTINCT mangaId FROM downloaded_chapters)
    """)
    fun getLibraryManga(): Flow<List<MangaEntity>>
}
