package com.kuro.music.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuro.music.data.repository.LibraryRepository
import com.kuro.music.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistDetailState(
    val playlistId: String = "",
    val playlistName: String = "",
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = true,
    val showRenameDialog: Boolean = false,
    val showDeleteDialog: Boolean = false
)

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PlaylistDetailState())
    val state: StateFlow<PlaylistDetailState> = _state.asStateFlow()

    init {
        val playlistId = savedStateHandle.get<String>("playlistId") ?: ""
        _state.value = _state.value.copy(playlistId = playlistId)
        loadPlaylist(playlistId)
    }

    private fun loadPlaylist(playlistId: String) {
        viewModelScope.launch {
            val playlist = libraryRepository.getPlaylistById(playlistId)
            _state.value = _state.value.copy(playlistName = playlist?.name ?: "Unknown")
        }
        viewModelScope.launch {
            libraryRepository.getPlaylistSongs(playlistId).collect { songs ->
                _state.value = _state.value.copy(songs = songs, isLoading = false)
            }
        }
    }

    fun removeSong(songId: String) {
        viewModelScope.launch {
            libraryRepository.removeSongFromPlaylist(_state.value.playlistId, songId)
        }
    }

    fun renamePlaylist(newName: String) {
        viewModelScope.launch {
            libraryRepository.renamePlaylist(_state.value.playlistId, newName)
            _state.value = _state.value.copy(playlistName = newName, showRenameDialog = false)
        }
    }

    fun deletePlaylist(onDeleted: () -> Unit) {
        viewModelScope.launch {
            libraryRepository.deletePlaylist(_state.value.playlistId)
            onDeleted()
        }
    }

    fun showRenameDialog() {
        _state.value = _state.value.copy(showRenameDialog = true)
    }

    fun hideRenameDialog() {
        _state.value = _state.value.copy(showRenameDialog = false)
    }

    fun showDeleteDialog() {
        _state.value = _state.value.copy(showDeleteDialog = true)
    }

    fun hideDeleteDialog() {
        _state.value = _state.value.copy(showDeleteDialog = false)
    }

    fun addSong(song: Song) {
        viewModelScope.launch {
            libraryRepository.addSongToPlaylist(_state.value.playlistId, song)
        }
    }
}
