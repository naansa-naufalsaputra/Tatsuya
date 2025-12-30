package com.example.tatsuya.presentation.detail

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tatsuya.R
import com.example.tatsuya.domain.model.Manga
import com.example.tatsuya.util.Resource

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    navController: NavController, // Added NavController for Back navigation
    onChapterClick: (String, String, String) -> Unit,
    viewModel: MangaDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val downloadedIds by viewModel.downloadedChapterIds.collectAsState()
    val downloadingIds by viewModel.downloadingChapterIds.collectAsState()
    val toastMsg by viewModel.toastMessage.collectAsState()
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { /* Menu */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val result = state) {
                is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is Resource.Error -> Text(text = result.message ?: "Error", modifier = Modifier.align(Alignment.Center))
                is Resource.Success -> {
                    val manga = result.data
                    if (manga != null) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            // --- INFO HEADER SECTION ---
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    // Cover Image
                                    AsyncImage(
                                        model = manga.coverUrl,
                                        contentDescription = "Cover",
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(150.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Gray),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // Info Text
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = manga.title,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = manga.author,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Status: Ongoing | Progress: ${manga.totalProgress}%", 
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        // Metadata Row (Source, etc)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Book, 
                                                contentDescription = "Source",
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            
                                            val sourceName = if (manga.id.startsWith("kc-")) "Source: KomikCast" else "Source: MangaDex"
                                            val sourceColor = if (manga.id.startsWith("kc-")) androidx.compose.ui.graphics.Color(0xFFFF9800) else MaterialTheme.colorScheme.primary

                                            Text(
                                                text = sourceName,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = sourceColor
                                            )
                                        }
                                    }
                                }
                            }

                            // --- ACTION BUTTONS ---
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Favorite Button
                                    Button(
                                        onClick = { viewModel.toggleFavorite() },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (manga.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (manga.isFavorite) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    ) {
                                        Icon(
                                            imageVector = if (manga.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = if (manga.isFavorite) "In Library" else "Add to Library")
                                    }
                                    
                                    // WebView / Browser Button
                                    OutlinedButton(
                                        onClick = { /* Open WebView */ },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(text = "WebView")
                                    }
                                }
                            }

                            // --- DESCRIPTION & GENRES ---
                            item {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = manga.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 5,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Genres (FlowRow)
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        manga.genres.forEach { genre ->
                                            SuggestionChip(
                                                onClick = { },
                                                label = { Text(text = genre, style = MaterialTheme.typography.labelSmall) },
                                                colors = SuggestionChipDefaults.suggestionChipColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                ),
                                                border = null // Remove border or use BorderStroke if required by this version
                                            )
                                        }
                                    }
                                }
                            }

                            // --- CHAPTER HEADER ---
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${manga.chapters.size} Chapters",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    IconButton(onClick = { /* Filter */ }) {
                                        Icon(painter = painterResource(R.drawable.ic_download), contentDescription = "Filter", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                            }
                            
                            // --- CHAPTER LIST ---
                            items(manga.chapters) { chapter ->
                                val isDownloaded = downloadedIds.contains(chapter.id)
                                val isDownloading = downloadingIds.contains(chapter.id)
                                val isRead = chapter.isRead
                                val progressText = if (chapter.totalPages > 0 && !isRead) "Page ${chapter.lastPageRead}/${chapter.totalPages}" else null
                                
                                // Lebih kontras: Dark Mode pakai SurfaceVariant (abu-abu terang), Light Mode pakai abu-abu muda
                                val baseBackgroundColor = if (isDark) {
                                    MaterialTheme.colorScheme.surfaceVariant // Solid atau transparansi rendah biar kontras banget sama hitam
                                } else {
                                    Color(0xFFF5F5F5) // Abu-abu muda solid untuk Light Mode
                                }
                                
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp) // Tambah jeda antar item biar kelihatan bedanya
                                        .clip(RoundedCornerShape(8.dp)) // Kasih rounded biar cakep
                                        .clickable { 
                                            viewModel.addToHistory()
                                            onChapterClick(chapter.id, manga.id, chapter.name) 
                                        }
                                        .background(
                                            if (isDownloaded) 
                                                MaterialTheme.colorScheme.primaryContainer // Pakai warna container biar solid tapi tetap tema
                                            else baseBackgroundColor
                                        )
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = chapter.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (isDownloaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = if (isRead) "Read" else (progressText ?: "Unknown Date"), 
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isRead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        
                                        IconButton(onClick = { viewModel.downloadChapter(chapter) }) {
                                            if (isDownloaded) {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_check),
                                                    contentDescription = "Downloaded",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            } else if (isDownloading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(18.dp),
                                                    strokeWidth = 2.dp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            } else {
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_download),
                                                    contentDescription = "Download",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                                Divider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), 
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                        
                        // Floating 'Start Reading' Button
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            ExtendedFloatingActionButton(
                                onClick = { 
                                    if (manga.chapters.isNotEmpty()) {
                                        viewModel.addToHistory()
                                        val firstChapter = manga.chapters.last() // Last in list is usually 1st chapter
                                        onChapterClick(firstChapter.id, manga.id, firstChapter.name)
                                    }
                                },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                icon = { Icon(Icons.Default.Book, contentDescription = null) },
                                text = { Text("Start Reading") }
                            )
                        }
                    }
                }
            }
        }
    }
}