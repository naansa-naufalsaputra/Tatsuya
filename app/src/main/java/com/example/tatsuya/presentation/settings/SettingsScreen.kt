package com.example.tatsuya.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tatsuya.data.repository.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val cacheSize by viewModel.cacheSize.collectAsState()
    val currentTheme by viewModel.theme.collectAsState(initial = AppTheme.SYSTEM)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- HEADER ---
            item {
                Text(
                    text = "Application Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            // --- STORAGE ---
            item {
                SettingsSectionTitle("Storage")
            }
            item {
                ListItem(
                    headlineContent = { Text("Clear Image Cache") },
                    supportingContent = { Text(cacheSize) },
                    trailingContent = {
                        Button(onClick = { viewModel.clearCache() }) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Clear")
                        }
                    }
                )
                Divider()
            }

            // --- APPEARANCE ---
            item {
                SettingsSectionTitle("Appearance")
            }
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Theme", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    }
                    
                    ThemeRadioButton("System Default", AppTheme.SYSTEM, currentTheme) { viewModel.setTheme(it) }
                    ThemeRadioButton("Light Mode", AppTheme.LIGHT, currentTheme) { viewModel.setTheme(it) }
                    ThemeRadioButton("Dark Mode", AppTheme.DARK, currentTheme) { viewModel.setTheme(it) }
                }
                Divider(Modifier.padding(top = 8.dp))
            }

            // --- ABOUT ---
            item {
                SettingsSectionTitle("About")
            }
            item {
                ListItem(
                    headlineContent = { Text("Tatsuya Manga Reader") },
                    supportingContent = { Text("Version 1.0.0") }
                )
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun ThemeRadioButton(
    text: String,
    theme: AppTheme,
    currentTheme: AppTheme,
    onSelect: (AppTheme) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onSelect(theme) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = (theme == currentTheme),
            onClick = { onSelect(theme) }
        )
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}
