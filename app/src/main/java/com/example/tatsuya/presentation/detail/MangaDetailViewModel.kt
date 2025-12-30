package com.example.tatsuya.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tatsuya.domain.model.Chapter
import com.example.tatsuya.domain.model.Manga
import com.example.tatsuya.domain.repository.MangaRepository
import com.example.tatsuya.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MangaDetailViewModel @Inject constructor(
    private val repository: MangaRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow<Resource<Manga>>(Resource.Loading())
    val state: StateFlow<Resource<Manga>> = _state

    private val _downloadedChapterIds = MutableStateFlow<List<String>>(emptyList())
    val downloadedChapterIds: StateFlow<List<String>> = _downloadedChapterIds

    private val _downloadingChapterIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadingChapterIds: StateFlow<Set<String>> = _downloadingChapterIds

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    init {
        val mangaId = savedStateHandle.get<String>("mangaId")
        if (mangaId != null) {
            getMangaDetails(mangaId)
            observeDownloads(mangaId)
        }
    }

    private fun getMangaDetails(mangaId: String) {
        viewModelScope.launch {
            _state.value = Resource.Loading()
            _state.value = repository.getMangaDetails(mangaId)
        }
    }

    private fun observeDownloads(mangaId: String) {
        viewModelScope.launch {
            repository.getDownloadedChapters(mangaId).collectLatest { ids ->
                _downloadedChapterIds.value = ids
                // Remove finished downloads from loading set
                _downloadingChapterIds.value = _downloadingChapterIds.value - ids.toSet()
            }
        }
    }

    fun downloadChapter(chapter: Chapter) {
        // Optimistic UI update
        _downloadingChapterIds.value = _downloadingChapterIds.value + chapter.id
        
        viewModelScope.launch {
            _toastMessage.value = "Starting download: " + chapter.name
            repository.downloadChapter(chapter)
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun addToHistory() {
        val currentManga = state.value.data
        if (currentManga != null) {
            viewModelScope.launch {
                repository.updateLastRead(currentManga)
            }
        }
    }
    fun toggleFavorite() {
        val currentManga = state.value.data
        if (currentManga != null) {
            val newFavoriteStatus = !currentManga.isFavorite
            val updatedManga = currentManga.copy(isFavorite = newFavoriteStatus)
            
            // Update local state immediately for UI responsiveness
            _state.value = Resource.Success(updatedManga)
            
            viewModelScope.launch {
                if (newFavoriteStatus) {
                    _toastMessage.value = "Added to Library"
                } else {
                    _toastMessage.value = "Removed from Library"
                }
                repository.addToLibrary(updatedManga)
            }
        }
    }
}
