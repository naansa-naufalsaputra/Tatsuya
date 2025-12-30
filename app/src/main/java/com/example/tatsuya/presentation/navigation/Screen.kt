package com.example.tatsuya.presentation.navigation

sealed class Screen(val route: String) {
    object Library : Screen("library")
    object Browse : Screen("browse")
    object Detail : Screen("detail/{mangaId}") {
        fun createRoute(mangaId: Long) = "detail/$mangaId"
    }
    object Reader : Screen("reader/{mangaId}/{chapterId}") {
        fun createRoute(mangaId: Long, chapterId: Long) = "reader/$mangaId/$chapterId"
    }
}
