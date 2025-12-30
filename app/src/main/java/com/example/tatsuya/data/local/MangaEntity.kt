package com.example.tatsuya.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "manga_table")
data class MangaEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val title: String,
    val coverUrl: String,
    val url: String,
    val author: String,
    val description: String,
    val isFavorite: Boolean = false,
    val addedAt: Long = System.currentTimeMillis(),
    val lastRead: Long? = null,
    val totalChapters: Int = 0 
)
