package com.example.tatsuya.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// Perhatikan version naik jadi 2 karena struktur berubah
@Database(
    entities = [MangaEntity::class, ChapterEntity::class, PageEntity::class, ChapterProgressEntity::class, DownloadedChapterEntity::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class) // Pasang alat bantu converter
abstract class MangaDatabase : RoomDatabase() {
    abstract val mangaDao: MangaDao
}
