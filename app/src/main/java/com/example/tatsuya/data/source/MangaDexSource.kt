package com.example.tatsuya.data.source

import com.example.tatsuya.data.remote.MangaDexApi
import com.example.tatsuya.data.remote.dto.MangaDataDto
import com.example.tatsuya.domain.model.Chapter
import com.example.tatsuya.domain.model.Manga
import com.example.tatsuya.domain.model.Page
import javax.inject.Inject

class MangaDexSource @Inject constructor(
    private val api: MangaDexApi
) : MangaSource {

    override val id: String = "mangadex"
    override val baseUrl: String = "https://mangadex.org"

    override suspend fun getPopularManga(page: Int): List<Manga> {
        val offset = (page - 1) * 20
        return api.getMangaList(offset = offset).data.map { mapDtoToManga(it) }
    }
    
    // Helper untuk overload method yang support tag
    suspend fun getPopularManga(page: Int, tagId: String?): List<Manga> {
        val offset = (page - 1) * 20
        val tags = if (tagId != null) listOf(tagId) else null
        return api.getMangaList(offset = offset, includedTags = tags).data.map { mapDtoToManga(it) }
    }

    override suspend fun searchManga(query: String): List<Manga> {
        return api.searchManga(title = query).data.map { mapDtoToManga(it) }
    }

    override suspend fun getMangaDetails(mangaId: String): Manga {
        val mangaInfo = api.getMangaInfo(mangaId).data
        val chapterResponse = api.getMangaChapters(mangaId)
        
        val baseManga = mapDtoToManga(mangaInfo)

        val chapters = chapterResponse.data
            .filter { (it.attributes.pages ?: 0) > 0 } 
            .map { chDto ->
                val chapNum = chDto.attributes.chapter ?: "?"
                val chapTitle = chDto.attributes.title ?: ""
                val lang = chDto.attributes.translatedLanguage ?: "en"
                
                val fullName = "Ch. " + chapNum + " (" + lang.uppercase() + ") - " + chapTitle
                Chapter(
                    id = chDto.id,
                    name = fullName,
                    url = "",
                    mangaId = mangaId
                )
            }.reversed()
            
        // Catatan: Logic merging progress & isRead ada di Repository, 
        // Source hanya return pure data dari Network.
        
        return baseManga.copy(chapters = chapters)
    }

    override suspend fun getChapterList(mangaId: String): List<Chapter> {
        // Method ini duplicate dengan bagian 'getMangaDetails' tapi berguna jika butuh chapters saja
        val chapterResponse = api.getMangaChapters(mangaId)
        return chapterResponse.data
            .filter { (it.attributes.pages ?: 0) > 0 }
            .map { chDto ->
                val chapNum = chDto.attributes.chapter ?: "?"
                val chapTitle = chDto.attributes.title ?: ""
                val lang = chDto.attributes.translatedLanguage ?: "en"
                
                Chapter(
                    id = chDto.id,
                    name = "Ch. $chapNum ($lang) - $chapTitle",
                    url = "",
                    mangaId = mangaId
                )
            }.reversed()
    }

    override suspend fun getPageList(chapterId: String): List<Page> {
        val response = api.getChapterPages(chapterId)
        val baseUrl = response.baseUrl
        val hash = response.chapter.hash
        
        return response.chapter.data.mapIndexed { index, fileName ->
            val imgUrl = baseUrl + "/data/" + hash + "/" + fileName
            Page(index, imgUrl, chapterId)
        }
    }

    override suspend fun getChapterDetails(chapterId: String): Chapter {
        val response = api.getChapter(chapterId)
        val dto = response.data
        val mangaId = dto.relationships.find { it.type == "manga" }?.id ?: "unknown"
        val title = dto.attributes.title ?: ""
        val chNum = dto.attributes.chapter ?: "?"

        return Chapter(
            id = dto.id,
            name = "Ch. $chNum - $title",
            url = "",
            mangaId = mangaId
        )
    }

    private fun mapDtoToManga(dto: MangaDataDto): Manga {
        val title = dto.attributes.title.values.firstOrNull() ?: "No Title"
        val descMap = dto.attributes.description
        val desc = descMap?.get("en") ?: descMap?.values?.firstOrNull() ?: "No Description"
        
        val coverFileName = dto.relationships.find { it.type == "cover_art" }?.attributes?.fileName
        val coverUrl = if (coverFileName != null) {
            "https://uploads.mangadex.org/covers/" + dto.id + "/" + coverFileName + ".256.jpg"
        } else {
            ""
        }
        val authorName = dto.relationships.find { it.type == "author" }?.attributes?.name ?: "Unknown"

        return Manga(
            id = dto.id,
            title = title,
            coverUrl = coverUrl,
            url = "",
            author = authorName,
            description = desc,
            genres = emptyList(),
            chapters = emptyList(),
            isFavorite = false
        )
    }
}
