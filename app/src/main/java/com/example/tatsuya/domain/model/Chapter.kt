package com.example.tatsuya.domain.model

data class Chapter(
    val id: String,
    val name: String,
    val url: String,
    val mangaId: String,
    val isRead: Boolean = false,
    val lastPageRead: Int = 0,
    val totalPages: Int = 0
)
