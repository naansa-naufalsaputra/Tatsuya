package com.example.tatsuya.presentation.detail

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tatsuya.domain.model.Chapter
import com.example.tatsuya.domain.model.Manga
import com.example.tatsuya.util.Resource

@Composable
fun MangaDetailScreen(
    navController: NavController,
    viewModel: MangaDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val downloadedIds by viewModel.downloadedChapterIds.collectAsState()
    val toastMsg by viewModel.toastMessage.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val result = state) {
            is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            is Resource.Error -> Text(text = result.message ?: "Error", color = Color.Red, modifier = Modifier.align(Alignment.Center))
            is Resource.Success -> {
                val manga = result.data
                if (manga != null) {
                    MangaDetailContent(
                        manga = manga,
                        downloadedIds = downloadedIds,
                        onBackClick = { navController.popBackStack() },
                        onChapterClick = { chapter ->
                            navController.navigate("reader/" + chapter.id)
                        },
                        onDownloadClick = { chapter ->
                            viewModel.downloadChapter(chapter)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MangaDetailContent(
    manga: Manga,
    downloadedIds: List<String>,
    onBackClick: () -> Unit,
    onChapterClick: (Chapter) -> Unit,
    onDownloadClick: (Chapter) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // --- HEADER GAMBAR ---
        item {
            Box(modifier = Modifier.height(300.dp).fillMaxWidth()) {
                AsyncImage(
                    model = manga.coverUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                startY = 300f
                            )
                        )
                )
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = manga.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Author: " + manga.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )
                }
            }
        }

        // --- SINOPSIS ---
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Synopsis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = manga.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // --- JUDUL LIST CHAPTER ---
        item {
            Text(
                text = "Chapters (" + manga.chapters.size + ")",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // --- DAFTAR CHAPTER (LOOPING) ---
        items(manga.chapters) { chapter ->
            val isDownloaded = downloadedIds.contains(chapter.id)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onChapterClick(chapter) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // --- TOMBOL DL ---
                    IconButton(onClick = { onDownloadClick(chapter) }) {
                        if (isDownloaded) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Downloaded",
                                tint = Color.Green
                            )
                        } else {
                            Text(
                                text = "DL",
                                color = Color.Blue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // --- JUDUL CHAPTER ---
                    Text(
                        text = chapter.name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                }
            }
        }

        // --- SPACER BAWAH AGAR TIDAK MENTOK ---
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}