package com.kuro.music.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuro.music.data.local.dao.HistoryDao
import com.kuro.music.data.mapper.toSong
import com.kuro.music.data.repository.SearchRepository
import com.kuro.music.data.repository.StreamRepository
import com.kuro.music.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val trending: List<Song> = emptyList(),
    val quickPicks: List<Song> = emptyList(),
    val recentlyPlayed: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedMood: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val historyDao: HistoryDao,
    private val streamRepository: StreamRepository
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeState())
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()

    init {
        loadHome()
    }

    fun loadHome() {
        viewModelScope.launch {
            _homeState.value = _homeState.value.copy(isLoading = true, error = null)
            try {
                // Get music-specific content via search (more reliable than trending for music)
                val trendingMusic = searchRepository.search("trending music 2026 hits", "music_songs")
                val quickPicks = searchRepository.search("best new songs", "music_songs")

                _homeState.value = _homeState.value.copy(
                    trending = trendingMusic.take(20),
                    quickPicks = quickPicks.take(10),
                    isLoading = false
                )
                Log.d("HomeViewModel", "Loaded ${trendingMusic.size} trending + ${quickPicks.size} quick picks")

                // Prefetch stream URLs for the first few songs so they play instantly
                val songsToPreload = trendingMusic.take(3) + quickPicks.take(2)
                songsToPreload.forEach { song ->
                    launch {
                        streamRepository.prefetchStreamUrl(song.id)
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to load home: ${e.message}")
                _homeState.value = _homeState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load music"
                )
            }
        }

        // Load recently played from Room
        viewModelScope.launch {
            historyDao.getRecentlyPlayed(20).collect { songs ->
                _homeState.value = _homeState.value.copy(
                    recentlyPlayed = songs.map { it.toSong() }
                )
            }
        }
    }

    fun onMoodSelected(mood: String?) {
        _homeState.value = _homeState.value.copy(selectedMood = mood)
        if (mood != null) {
            viewModelScope.launch {
                _homeState.value = _homeState.value.copy(isLoading = true)
                try {
                    val results = searchRepository.search("$mood music songs", "music_songs")
                    _homeState.value = _homeState.value.copy(
                        trending = results.take(20),
                        isLoading = false
                    )
                } catch (e: Exception) {
                    _homeState.value = _homeState.value.copy(isLoading = false)
                }
            }
        } else {
            loadHome()
        }
    }
}
