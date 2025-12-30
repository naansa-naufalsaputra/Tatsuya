package com.example.tatsuya.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tatsuya.domain.model.Manga
import com.example.tatsuya.domain.repository.MangaRepository
import com.example.tatsuya.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: MangaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow<Resource<Manga>>(Resource.Loading())
    val state: StateFlow<Resource<Manga>> = _state

    init {
        // Ambil ID dari navigasi
        val mangaId = savedStateHandle.get<String>("mangaId")
        if (mangaId != null) {
            getMangaDetails(mangaId)
        }
    }

    private fun getMangaDetails(mangaId: String) {
        viewModelScope.launch {
            _state.value = Resource.Loading()
            val result = repository.getMangaDetails(mangaId)
            _state.value = result
        }
    }

    fun toggleFavorite(manga: Manga) {
        viewModelScope.launch {
            if (manga.isFavorite) {
                repository.removeFromLibrary(manga.id)
            } else {
                repository.addToLibrary(manga)
            }
            // Refresh data status
            getMangaDetails(manga.id)
        }
    }
}
