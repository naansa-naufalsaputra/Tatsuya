package com.example.tatsuya.presentation.browse

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
class BrowseViewModel @Inject constructor(
    private val repository: MangaRepository
) : ViewModel() {

    private val _state = MutableStateFlow<Resource<List<Manga>>>(Resource.Loading())
    val state: StateFlow<Resource<List<Manga>>> = _state

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    // Genre Categories (Hardcoded top genres from MangaDex)
    data class Genre(val name: String, val id: String?)
    
    val genres = listOf(
        Genre("All", null),
        Genre("Action", "391b0423-d847-456f-aff0-8b0cfc03066b"),
        Genre("Adventure", "87cc87cd-a395-47af-b27a-93258283bbc6"),
        Genre("Fantasy", "cdc58593-87dd-415e-bbc0-2ec27bf404cc"),
        Genre("Romance", "423e2eae-3702-498e-a7f5-29116a410660"),
        Genre("Comedy", "4d32cc48-9f00-4cca-9b5a-a839f0764984"),
        Genre("Slice of Life", "e5301a23-ebd9-49dd-a0cb-2add944c7fe9"),
        Genre("Drama", "b9af3a63-f058-46de-a9a0-e0c13906197a"),
        Genre("Horror", "cdad7e6d-8760-41da-80ce-b5e227a11f95"),
        Genre("Mystery", "ee968100-4191-4968-93d3-f82d72be7e46"),
        Genre("Psychological", "3b60b75c-a2d7-486d-ab56-66f5d05c9362"),
        Genre("Sci-Fi", "256c8bd9-4904-4360-bf4f-508a76d67183"),
        Genre("Isekai", "ace04997-f6bd-436e-b261-779182193d3d")
    )
    
    private val _selectedGenre = MutableStateFlow(genres.first())
    val selectedGenre: StateFlow<Genre> = _selectedGenre

    init {
        getPopularManga()
    }

    private fun getPopularManga() {
        viewModelScope.launch {
            _state.value = Resource.Loading()
            // Pass selected genre ID (if any)
            val result = repository.getPopularManga(1, _selectedGenre.value.id)
            _state.value = result
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            getPopularManga()
        } else {
            // Reset genre when searching
            _selectedGenre.value = genres.first()
            searchManga(query)
        }
    }
    
    fun onGenreSelected(genre: Genre) {
        _selectedGenre.value = genre
        _searchQuery.value = "" // Clear search when picking genre
        getPopularManga()
    }

    fun searchManga(query: String) {
         viewModelScope.launch {
            _state.value = Resource.Loading()
            val result = repository.searchManga(query)
            _state.value = result
        }
    }
}
