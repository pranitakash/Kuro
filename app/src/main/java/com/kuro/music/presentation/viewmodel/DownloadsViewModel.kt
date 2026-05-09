package com.kuro.music.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuro.music.data.repository.DownloadRepository
import com.kuro.music.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DownloadsState(
    val storageUsed: Long = 0L,
    val isLoading: Boolean = true
)

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DownloadsState())
    val state: StateFlow<DownloadsState> = _state.asStateFlow()

    val downloadedSongs: StateFlow<List<Song>> = downloadRepository.getDownloadedSongs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        loadStorageInfo()
    }

    private fun loadStorageInfo() {
        viewModelScope.launch {
            val used = downloadRepository.getStorageUsed()
            _state.value = _state.value.copy(storageUsed = used, isLoading = false)
        }
    }

    fun downloadSong(song: Song, quality: String = "medium") {
        downloadRepository.downloadSong(song, quality)
    }

    fun deleteDownload(songId: String) {
        viewModelScope.launch {
            downloadRepository.deleteDownload(songId)
            loadStorageInfo()
        }
    }

    fun cancelDownload(songId: String) {
        downloadRepository.cancelDownload(songId)
    }

    fun formatStorageSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> String.format("%.1f GB", bytes.toDouble() / (1024 * 1024 * 1024))
        }
    }
}
