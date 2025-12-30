package com.example.tatsuya.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.example.tatsuya.data.repository.AppTheme
import com.example.tatsuya.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val theme = preferencesRepository.theme

    private val _cacheSize = MutableStateFlow("Calculating...")
    val cacheSize: StateFlow<String> = _cacheSize.asStateFlow()

    init {
        calculateCacheSize()
    }

    fun setTheme(newTheme: AppTheme) {
        viewModelScope.launch {
            preferencesRepository.setTheme(newTheme)
        }
    }

    fun calculateCacheSize() {
        viewModelScope.launch(Dispatchers.IO) {
            val sizeBytes = getDirSize(context.cacheDir) + 
                            getDirSize(context.codeCacheDir) + 
                            getDirSize(context.externalCacheDir)
            
            // Format size
            val sizeMb = sizeBytes / (1024.0 * 1024.0)
            _cacheSize.value = String.format("%.2f MB used", sizeMb)
        }
    }

    private fun getDirSize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0
        var result: Long = 0
        dir.listFiles()?.forEach { file ->
            result += if (file.isDirectory) {
                getDirSize(file)
            } else {
                file.length()
            }
        }
        return result
    }

    fun clearCache() {
        viewModelScope.launch(Dispatchers.IO) {
            // Delete Android Cache
            deleteDir(context.cacheDir)
            deleteDir(context.codeCacheDir)
            deleteDir(context.externalCacheDir)
            
            // Clear Coil Cache explicitly
            withContext(Dispatchers.Main) {
                ImageLoader(context).diskCache?.clear()
            }

            calculateCacheSize()
        }
    }

    private fun deleteDir(dir: File?) {
        if (dir == null || !dir.exists()) return
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                deleteDir(file)
            }
            file.delete()
        }
    }
}
