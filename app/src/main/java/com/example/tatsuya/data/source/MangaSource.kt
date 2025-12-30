package com.example.tatsuya.data.source

import com.example.tatsuya.domain.model.Chapter
import com.example.tatsuya.domain.model.Manga
import com.example.tatsuya.domain.model.Page
import com.example.tatsuya.util.Resource

interface MangaSource {
    val id: String
    val baseUrl: String

    suspend fun getPopularManga(page: Int): List<Manga>
    suspend fun searchManga(query: String): List<Manga>
    suspend fun getMangaDetails(mangaId: String): Manga
    suspend fun getChapterList(mangaId: String): List<Chapter>
    suspend fun getPageList(chapterId: String): List<Page>
    suspend fun getChapterDetails(chapterId: String): Chapter
}
