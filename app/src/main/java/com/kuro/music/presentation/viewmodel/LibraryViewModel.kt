package com.kuro.music.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuro.music.data.local.entity.PlaylistEntity
import com.kuro.music.data.repository.LibraryRepository
import com.kuro.music.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryState(
    val playlists: List<PlaylistEntity> = emptyList(),
    val likedSongCount: Int = 0,
    val downloadCount: Int = 0,
    val showCreatePlaylistDialog: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    val likedSongs: StateFlow<List<Song>> = libraryRepository.getLikedSongs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val recentlyPlayed: StateFlow<List<Song>> = libraryRepository.getRecentlyPlayed()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val downloadedSongs: StateFlow<List<Song>> = libraryRepository.getDownloadedSongs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        loadLibrary()
    }

    private fun loadLibrary() {
        viewModelScope.launch {
            libraryRepository.getAllPlaylists().collect { playlists ->
                _state.value = _state.value.copy(playlists = playlists, isLoading = false)
            }
        }
        viewModelScope.launch {
            libraryRepository.getLikedSongCount().collect { count ->
                _state.value = _state.value.copy(likedSongCount = count)
            }
        }
        viewModelScope.launch {
            libraryRepository.getDownloadedSongs().collect { songs ->
                _state.value = _state.value.copy(downloadCount = songs.size)
            }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            libraryRepository.createPlaylist(name)
            _state.value = _state.value.copy(showCreatePlaylistDialog = false)
        }
    }

    fun deletePlaylist(id: String) {
        viewModelScope.launch {
            libraryRepository.deletePlaylist(id)
        }
    }

    fun showCreateDialog() {
        _state.value = _state.value.copy(showCreatePlaylistDialog = true)
    }

    fun hideCreateDialog() {
        _state.value = _state.value.copy(showCreatePlaylistDialog = false)
    }

    fun clearHistory() {
        viewModelScope.launch {
            libraryRepository.clearHistory()
        }
    }

    fun addSongToPlaylist(playlistId: String, song: Song) {
        viewModelScope.launch {
            libraryRepository.addSongToPlaylist(playlistId, song)
        }
    }
}
