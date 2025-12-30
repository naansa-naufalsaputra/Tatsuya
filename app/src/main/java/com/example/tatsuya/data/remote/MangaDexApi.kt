package com.example.tatsuya.data.remote

import com.example.tatsuya.data.remote.dto.ChapterListResponse
import com.example.tatsuya.data.remote.dto.ChapterPagesResponse
import com.example.tatsuya.data.remote.dto.MangaListResponse
import com.example.tatsuya.data.remote.dto.SingleMangaResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MangaDexApi {
    @GET("manga")
    suspend fun getMangaList(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("includes[]") includes: List<String> = listOf("cover_art", "author"),
        @Query("includedTags[]") includedTags: List<String>? = null,
        @Query("order[followedCount]") order: String = "desc"
    ): MangaListResponse

    @GET("manga")
    suspend fun searchManga(
        @Query("title") title: String,
        @Query("limit") limit: Int = 20,
        @Query("includes[]") includes: List<String> = listOf("cover_art", "author")
    ): MangaListResponse

    @GET("manga/{mangaId}")
    suspend fun getMangaInfo(
        @Path("mangaId") mangaId: String,
        @Query("includes[]") includes: List<String> = listOf("cover_art", "author")
    ): SingleMangaResponse

    @GET("manga/{mangaId}/feed")
    suspend fun getMangaChapters(
        @Path("mangaId") mangaId: String,
        @Query("translatedLanguage[]") languages: List<String> = listOf("id", "en"), // Ambil Indo & Inggris
        @Query("order[chapter]") order: String = "asc",
        @Query("limit") limit: Int = 500 // FIX: Naikkan limit ke MAX (500)
    ): ChapterListResponse

    @GET("at-home/server/{chapterId}")
    suspend fun getChapterPages(@Path("chapterId") chapterId: String): ChapterPagesResponse

    @GET("chapter/{chapterId}")
    suspend fun getChapter(@Path("chapterId") chapterId: String): com.example.tatsuya.data.remote.dto.SingleChapterResponse
}
