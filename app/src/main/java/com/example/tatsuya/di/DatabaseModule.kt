package com.example.tatsuya.di

import android.app.Application
import androidx.room.Room
import com.example.tatsuya.data.local.MangaDao
import com.example.tatsuya.data.local.MangaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMangaDatabase(app: Application): MangaDatabase {
        val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE manga_table ADD COLUMN totalChapters INTEGER NOT NULL DEFAULT 0")
            }
        }

        return Room.databaseBuilder(
            app,
            MangaDatabase::class.java,
            "manga_db"
        )
        .addMigrations(MIGRATION_6_7)
        .fallbackToDestructiveMigration() // Aman: Jika versi db beda, hapus yang lama
        .build()
    }

    @Provides
    @Singleton
    fun provideMangaDao(database: MangaDatabase): MangaDao {
        return database.mangaDao
    }

    @Provides
    @Singleton
    fun provideWorkManager(@dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context): androidx.work.WorkManager {
        return androidx.work.WorkManager.getInstance(context)
    }
}
