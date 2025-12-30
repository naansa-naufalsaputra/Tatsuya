package com.example.tatsuya.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Library : BottomNavItem(Screen.Library.route, "Library", Icons.Default.CollectionsBookmark)
    object Browse : BottomNavItem(Screen.Browse.route, "Browse", Icons.Default.Explore)
}
