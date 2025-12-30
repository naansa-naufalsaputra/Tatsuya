package com.example.tatsuya.domain.source

import com.example.tatsuya.domain.model.Chapter
import com.example.tatsuya.domain.model.Manga
import com.example.tatsuya.domain.model.Page

interface MangaSource {
    suspend fun getPopularManga(page: Int): List<Manga>
    
    suspend fun searchManga(query: String, page: Int): List<Manga>
    
    suspend fun getMangaDetails(manga: Manga): Manga

    suspend fun getChapterList(mangaUrl: String): List<Chapter>
    
    suspend fun getPageList(chapterUrl: String): List<Page>
}
