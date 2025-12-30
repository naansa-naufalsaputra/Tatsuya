package com.example.tatsuya.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tatsuya.domain.model.Manga
import com.example.tatsuya.domain.repository.MangaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: MangaRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _filter = MutableStateFlow(LibraryFilter.ALL)
    val filter = _filter.asStateFlow()

    // Combine source of truth (Repo) with Search and Filter states
    val libraryManga: StateFlow<List<Manga>> = combine(
        repository.getLibraryManga(),
        _searchQuery,
        _filter
    ) { mangaList, query, currentFilter ->
        mangaList.filter { manga ->
            val matchesQuery = manga.title.contains(query, ignoreCase = true)
            // Note: Currently 'Downloaded' filter logic would require checking if it has downloads. 
            // Since our query fetches BOTH favorites and downloads, we might need extra info to filter strictly 'Downloaded'.
            // For now, we stick to 'All' or simple text search.
            matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChange(newFilter: LibraryFilter) {
        _filter.value = newFilter
    }
}

enum class LibraryFilter {
    ALL, DOWNLOADED, READING
}
