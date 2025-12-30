package com.example.tatsuya.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chapter_table",
    foreignKeys = [
        ForeignKey(
            entity = MangaEntity::class,
            parentColumns = ["id"],
            childColumns = ["mangaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["mangaId"])]
)
data class ChapterEntity(
    @PrimaryKey val id: String,
    val mangaId: String,
    val name: String,
    val url: String,
    val uploadDate: Long = 0
)
