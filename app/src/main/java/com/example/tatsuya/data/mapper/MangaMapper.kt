package com.example.tatsuya.data.mapper

import com.example.tatsuya.data.local.MangaEntity
import com.example.tatsuya.domain.model.Manga

fun MangaEntity.toDomain(): Manga {
    return Manga(
        id = id,
        title = title,
        coverUrl = coverUrl,
        url = url,
        author = author,
        description = description,
        genres = emptyList(), // Default kosong untuk database lokal
        chapters = emptyList(), // Default kosong
        isFavorite = isFavorite
    )
}

fun Manga.toEntity(): MangaEntity {
    return MangaEntity(
        id = id,
        title = title,
        coverUrl = coverUrl,
        url = url,
        author = author,
        description = description,
        isFavorite = true,
        addedAt = System.currentTimeMillis()
    )
}
