package com.example.tatsuya.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "page_table",
    foreignKeys = [
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["chapterId"])]
)
data class PageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chapterId: String,
    val imageUrl: String,
    val pageNumber: Int
)
