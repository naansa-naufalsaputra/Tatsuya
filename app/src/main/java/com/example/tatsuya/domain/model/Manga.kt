package com.example.tatsuya.domain.model

data class Manga(
    val id: String,
    val title: String,
    val coverUrl: String,
    val url: String,
    val author: String,
    val description: String,
    val genres: List<String>,
    val chapters: List<Chapter> = emptyList(),
    val isFavorite: Boolean = false,
    val historyText: String? = null,
    val totalProgress: Int = 0 // Persentase kelulusan (chapters read / total)
)
