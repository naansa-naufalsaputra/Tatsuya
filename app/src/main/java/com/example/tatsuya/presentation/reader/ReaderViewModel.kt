package com.example.tatsuya.presentation.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tatsuya.domain.model.Page
import com.example.tatsuya.domain.repository.MangaRepository
import com.example.tatsuya.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val repository: MangaRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow<Resource<List<Page>>>(Resource.Loading())
    val state: StateFlow<Resource<List<Page>>> = _state

    init {
        val chapterId = savedStateHandle.get<String>("chapterId")
        if (chapterId != null) {
            getPages(chapterId)
        }
    }

    private fun getPages(chapterId: String) {
        viewModelScope.launch {
            _state.value = Resource.Loading()
            val result = repository.getChapterPages(chapterId)
            _state.value = result
        }
    }

    fun onPageChanged(page: Int, totalPages: Int) {
        val chapterId = savedStateHandle.get<String>("chapterId")
        val mangaId = savedStateHandle.get<String>("mangaId")
        val title = savedStateHandle.get<String>("title")

        if (chapterId != null) {
            viewModelScope.launch {
                var safeMangaId = mangaId ?: "unknown"
                var safeTitle = title ?: "Unknown Chapter"
                
                // Decode just in case
                try {
                    safeMangaId = java.net.URLDecoder.decode(safeMangaId, "UTF-8")
                    safeTitle = java.net.URLDecoder.decode(safeTitle, "UTF-8")
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (safeMangaId == "unknown") {
                    // Fail-safe: Fetch from API
                    val resource = repository.getChapter(chapterId)
                    if (resource is Resource.Success && resource.data != null) {
                        safeMangaId = resource.data.mangaId
                        // Optional: update title if needed
                        // safeTitle = resource.data.name 
                    }
                }

                android.util.Log.d("DEBUG_HISTORY", "Saving progress: mangaId=$safeMangaId, chId=$chapterId, title=$safeTitle, page=$page")
                repository.saveReadingProgress(chapterId, safeMangaId, safeTitle, page, totalPages)
            }
        } else {
             android.util.Log.e("DEBUG_HISTORY", "ChapterId is null in onPageChanged")
        }
    }
}
