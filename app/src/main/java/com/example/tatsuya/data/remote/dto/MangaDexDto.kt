package com.example.tatsuya.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MangaListResponse(val data: List<MangaDataDto>)
data class SingleMangaResponse(val data: MangaDataDto)

data class MangaDataDto(val id: String, val attributes: MangaAttributesDto, val relationships: List<RelationshipDto>)
data class MangaAttributesDto(val title: Map<String, String>, val description: Map<String, String>?)
data class RelationshipDto(val type: String, val id: String, val attributes: RelationshipAttributesDto?)
data class RelationshipAttributesDto(val fileName: String?, val name: String?)

data class ChapterListResponse(
    val data: List<ChapterDataDto>,
    val limit: Int,
    val offset: Int,
    val total: Int
)
data class ChapterDataDto(
    val id: String,
    val attributes: ChapterAttributesDto,
    val relationships: List<RelationshipDto> = emptyList() 
)

// UPDATE: Tambah 'translatedLanguage' agar kita tahu ini bahasa apa
data class ChapterAttributesDto(
    val title: String?, 
    val chapter: String?, 
    val pages: Int?,
    val translatedLanguage: String? 
)

data class ChapterPagesResponse(val baseUrl: String, val chapter: ChapterHashDto)
data class ChapterHashDto(val hash: String, val data: List<String>)
