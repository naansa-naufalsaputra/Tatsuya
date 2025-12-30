package com.example.tatsuya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tatsuya.presentation.detail.DetailScreen
import com.example.tatsuya.presentation.library.LibraryScreen
import com.example.tatsuya.presentation.reader.ReaderScreen
import com.example.tatsuya.presentation.history.HistoryScreen
import com.example.tatsuya.presentation.screens.BrowseScreen
import com.example.tatsuya.ui.theme.TatsuyaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @javax.inject.Inject
    lateinit var preferencesRepository: com.example.tatsuya.data.repository.PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Schedule Background Update Checker (12 Hours)
        val request = androidx.work.PeriodicWorkRequestBuilder<com.example.tatsuya.worker.UpdateCheckWorker>(
            12, java.util.concurrent.TimeUnit.HOURS
        ).build()
        
        androidx.work.WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "MangaUpdateCheck",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            request
        )

        setContent {
            val theme by preferencesRepository.theme.collectAsState(initial = com.example.tatsuya.data.repository.AppTheme.SYSTEM)
            
            val isDarkTheme = when (theme) {
                com.example.tatsuya.data.repository.AppTheme.LIGHT -> false
                com.example.tatsuya.data.repository.AppTheme.DARK -> true
                else -> isSystemInDarkTheme()
            }

            TatsuyaTheme(darkTheme = isDarkTheme) {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    
    // List item untuk Bottom Navigation
    val items = listOf("Browse", "Library", "History")

    Scaffold(
        bottomBar = {
            // Tampilkan BottomBar hanya jika bukan di halaman Reader atau Detail atau Settings
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val currentRoute = currentDestination?.route

            // Hide bottom bar on Settings too
            if (currentRoute?.startsWith("reader") == false && 
                currentRoute?.startsWith("detail") == false &&
                currentRoute != "settings") {
                NavigationBar {
                    items.forEach { screen ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == screen.lowercase() } == true
                        NavigationBarItem(
                            icon = { 
                                Icon(
                                    when (screen) {
                                        "Browse" -> Icons.Default.Home
                                        "Library" -> Icons.Default.List
                                        "History" -> Icons.Default.DateRange
                                        else -> Icons.Default.Home
                                    },
                                    contentDescription = screen
                                ) 
                            },
                            label = { Text(screen) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.lowercase()) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "browse",
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. HALAMAN BROWSE
            composable("browse") {
                BrowseScreen(
                    onMangaClick = { mangaId ->
                        navController.navigate("detail/$mangaId")
                    }
                )
            }

            // 2. HALAMAN LIBRARY
            composable("library") {
                LibraryScreen(
                    onMangaClick = { mangaId ->
                        navController.navigate("detail/$mangaId")
                    },
                    onSettingsClick = {
                        navController.navigate("settings")
                    }
                )
            }

            // 5. HALAMAN HISTORY
            composable("history") {
                HistoryScreen(
                    onMangaClick = { mangaId ->
                        navController.navigate("detail/$mangaId")
                    }
                )
            }
            
            // 6. HALAMAN SETTINGS
            composable("settings") {
                com.example.tatsuya.presentation.settings.SettingsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 3. HALAMAN DETAIL (Menerima String ID)
            composable(
                route = "detail/{mangaId}",
                arguments = listOf(navArgument("mangaId") { type = NavType.StringType })
            ) {
                // DetailViewModel akan otomatis mengambil ID dari SavedStateHandle
                DetailScreen(
                    navController = navController,
                    onChapterClick = { chapterId, mangaId, title ->
                        val safeTitle = java.net.URLEncoder.encode(title, "UTF-8")
                        val safeMangaId = java.net.URLEncoder.encode(mangaId, "UTF-8")
                        navController.navigate("reader/$chapterId?mangaId=$safeMangaId&title=$safeTitle")
                    }
                )
            }

            // 4. HALAMAN READER (Menerima String ID + mangaId + title)
            composable(
                route = "reader/{chapterId}?mangaId={mangaId}&title={title}",
                arguments = listOf(
                    navArgument("chapterId") { type = NavType.StringType },
                    navArgument("mangaId") { type = NavType.StringType; defaultValue = "unknown" },
                    navArgument("title") { type = NavType.StringType; defaultValue = "Unknown Chapter" }
                )
            ) {
                ReaderScreen()
            }
        }
    }
}
