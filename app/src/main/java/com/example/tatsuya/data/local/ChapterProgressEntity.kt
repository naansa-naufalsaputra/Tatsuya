package com.example.tatsuya.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chapter_progress")
data class ChapterProgressEntity(
    @PrimaryKey val chapterId: String,
    val mangaId: String,
    val chapterTitle: String, // Simpan judul chapter di sini
    val pageNumber: Int,
    val totalPages: Int,
    val lastReadAt: Long = System.currentTimeMillis()
)
