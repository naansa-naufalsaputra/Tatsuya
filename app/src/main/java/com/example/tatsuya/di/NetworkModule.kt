package com.example.tatsuya.di

import com.example.tatsuya.data.remote.MangaDexApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMangaDexApi(): MangaDexApi {
        return Retrofit.Builder()
            .baseUrl("https://api.mangadex.org/") // URL Server Utama
            .addConverterFactory(GsonConverterFactory.create()) // Alat penerjemah JSON
            .build()
            .create(MangaDexApi::class.java)
    }
}
