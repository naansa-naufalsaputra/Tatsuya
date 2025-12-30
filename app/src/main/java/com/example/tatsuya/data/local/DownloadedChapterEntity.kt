package com.example.tatsuya.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_chapters")
data class DownloadedChapterEntity(
    @PrimaryKey val chapterId: String,
    val mangaId: String,
    val chapterName: String,
    val savedPath: String, // Path relative to filesDir
    val downloadedAt: Long = System.currentTimeMillis()
)
