package com.example.tatsuya.domain.repository

import com.example.tatsuya.domain.model.Chapter
import com.example.tatsuya.domain.model.Manga
import com.example.tatsuya.domain.model.Page
import com.example.tatsuya.util.Resource
import kotlinx.coroutines.flow.Flow

interface MangaRepository {
    // Remote (Internet)
    suspend fun getPopularManga(page: Int, tagId: String? = null): Resource<List<Manga>>
    
    // PERBAIKAN: Hapus parameter 'page', cukup 'query' saja
    suspend fun searchManga(query: String): Resource<List<Manga>>
    
    suspend fun getMangaDetails(mangaId: String): Resource<Manga>
    suspend fun getChapterPages(chapterId: String): Resource<List<Page>>
    suspend fun getChapter(chapterId: String): Resource<Chapter>

    // Local (Database)
    fun getFavoriteManga(): Flow<List<Manga>>
    fun getLibraryManga(): Flow<List<Manga>>
    suspend fun addToLibrary(manga: Manga)
    suspend fun removeFromLibrary(mangaId: String)

    // History
    fun getHistory(): Flow<List<Manga>>
    suspend fun updateLastRead(manga: Manga)
    
    // Reading Progress
    suspend fun saveReadingProgress(chapterId: String, mangaId: String, chapterTitle: String, page: Int, totalPages: Int)
    fun getReadingProgress(mangaId: String): Flow<List<com.example.tatsuya.data.local.ChapterProgressEntity>>

    // Downloads
    fun downloadChapter(chapter: Chapter)
    fun getDownloadedChapters(mangaId: String): Flow<List<String>> // Returns list of chapterIds
}
