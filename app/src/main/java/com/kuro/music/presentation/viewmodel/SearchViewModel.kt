package com.kuro.music.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuro.music.data.repository.SearchRepository
import com.kuro.music.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchState(
    val query: String = "",
    val results: List<Song> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val searchHistory: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val error: String? = null,
    val selectedFilter: SearchFilter = SearchFilter.SONGS
)

enum class SearchFilter(val label: String, val apiFilter: String) {
    SONGS("Songs", "music_songs"),
    ALBUMS("Albums", "music_albums"),
    ARTISTS("Artists", "channels"),
    PLAYLISTS("Playlists", "music_playlists")
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val searchRepository: SearchRepository
) : ViewModel() {

    companion object {
        private const val SEARCH_HISTORY_KEY = "search_history"
        private const val MAX_HISTORY = 15
    }

    private val _searchState = MutableStateFlow(SearchState())
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    private var searchJob: Job? = null
    private var suggestionsJob: Job? = null

    private val prefs = context.getSharedPreferences("kuro_search", Context.MODE_PRIVATE)

    init {
        loadSearchHistory()
    }

    private fun loadSearchHistory() {
        val history = prefs.getStringSet(SEARCH_HISTORY_KEY, emptySet())
            ?.toList()
            ?.sortedByDescending { it } // Simple ordering
            ?.take(MAX_HISTORY)
            ?: emptyList()

        // Store with timestamps using a different approach
        val historyList = prefs.getString("search_history_ordered", "")
            ?.split("|||")
            ?.filter { it.isNotBlank() }
            ?.take(MAX_HISTORY)
            ?: emptyList()

        _searchState.value = _searchState.value.copy(searchHistory = historyList)
    }

    private fun saveToHistory(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return

        val current = _searchState.value.searchHistory.toMutableList()
        // Remove existing entry if present (to move to top)
        current.remove(trimmed)
        // Add to front
        current.add(0, trimmed)
        // Limit size
        val limited = current.take(MAX_HISTORY)

        prefs.edit()
            .putString("search_history_ordered", limited.joinToString("|||"))
            .apply()

        _searchState.value = _searchState.value.copy(searchHistory = limited)
    }

    fun removeFromHistory(query: String) {
        val current = _searchState.value.searchHistory.toMutableList()
        current.remove(query)
        prefs.edit()
            .putString("search_history_ordered", current.joinToString("|||"))
            .apply()
        _searchState.value = _searchState.value.copy(searchHistory = current)
    }

    fun clearSearchHistory() {
        prefs.edit()
            .remove("search_history_ordered")
            .remove(SEARCH_HISTORY_KEY)
            .apply()
        _searchState.value = _searchState.value.copy(searchHistory = emptyList())
    }

    fun onQueryChanged(query: String) {
        _searchState.value = _searchState.value.copy(query = query)

        // Debounce suggestions
        suggestionsJob?.cancel()
        if (query.isNotBlank()) {
            suggestionsJob = viewModelScope.launch {
                delay(200)
                val suggestions = searchRepository.getSuggestions(query)
                _searchState.value = _searchState.value.copy(suggestions = suggestions)
            }
        } else {
            _searchState.value = _searchState.value.copy(suggestions = emptyList(), results = emptyList())
        }
    }

    fun search(query: String = _searchState.value.query) {
        if (query.isBlank()) return

        // Save to search history
        saveToHistory(query)

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _searchState.value = _searchState.value.copy(
                isSearching = true,
                query = query,
                suggestions = emptyList(),
                error = null
            )
            try {
                val results = searchRepository.search(
                    query,
                    _searchState.value.selectedFilter.apiFilter
                )
                _searchState.value = _searchState.value.copy(
                    results = results,
                    isSearching = false
                )
            } catch (e: Exception) {
                _searchState.value = _searchState.value.copy(
                    isSearching = false,
                    error = e.message ?: "Search failed"
                )
            }
        }
    }

    fun onFilterSelected(filter: SearchFilter) {
        _searchState.value = _searchState.value.copy(selectedFilter = filter)
        if (_searchState.value.query.isNotBlank()) {
            search()
        }
    }

    fun clearSearch() {
        _searchState.value = _searchState.value.copy(
            query = "",
            results = emptyList(),
            suggestions = emptyList()
        )
    }
}
