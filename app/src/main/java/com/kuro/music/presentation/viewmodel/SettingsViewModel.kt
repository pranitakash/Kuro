package com.kuro.music.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuro.music.data.local.UserPreferences
import com.kuro.music.data.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val streamingQuality: String = "medium",
    val downloadQuality: String = "medium",
    val normalizeVolume: Boolean = false,
    val crossfadeDuration: Int = 0,
    val gaplessPlayback: Boolean = true,
    val themeMode: String = "system",
    val accentColor: String = "dynamic",
    val gridView: Boolean = false,
    val pipedInstance: String = "https://api.piped.private.coffee",
    val ytDlpFallback: Boolean = true,
    val saveSearchHistory: Boolean = true,
    val savePlayHistory: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            userPreferences.streamingQuality.collect { v ->
                _state.value = _state.value.copy(streamingQuality = v)
            }
        }
        viewModelScope.launch {
            userPreferences.downloadQuality.collect { v ->
                _state.value = _state.value.copy(downloadQuality = v)
            }
        }
        viewModelScope.launch {
            userPreferences.normalizeVolume.collect { v ->
                _state.value = _state.value.copy(normalizeVolume = v)
            }
        }
        viewModelScope.launch {
            userPreferences.crossfadeDuration.collect { v ->
                _state.value = _state.value.copy(crossfadeDuration = v)
            }
        }
        viewModelScope.launch {
            userPreferences.gaplessPlayback.collect { v ->
                _state.value = _state.value.copy(gaplessPlayback = v)
            }
        }
        viewModelScope.launch {
            userPreferences.themeMode.collect { v ->
                _state.value = _state.value.copy(themeMode = v)
            }
        }
        viewModelScope.launch {
            userPreferences.pipedInstance.collect { v ->
                _state.value = _state.value.copy(pipedInstance = v)
            }
        }
        viewModelScope.launch {
            userPreferences.ytDlpFallback.collect { v ->
                _state.value = _state.value.copy(ytDlpFallback = v)
            }
        }
        viewModelScope.launch {
            userPreferences.saveSearchHistory.collect { v ->
                _state.value = _state.value.copy(saveSearchHistory = v)
            }
        }
        viewModelScope.launch {
            userPreferences.savePlayHistory.collect { v ->
                _state.value = _state.value.copy(savePlayHistory = v)
            }
        }
    }

    fun setStreamingQuality(quality: String) {
        viewModelScope.launch { userPreferences.setStreamingQuality(quality) }
    }

    fun setDownloadQuality(quality: String) {
        viewModelScope.launch { userPreferences.setDownloadQuality(quality) }
    }

    fun setNormalizeVolume(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setNormalizeVolume(enabled) }
    }

    fun setCrossfadeDuration(seconds: Int) {
        viewModelScope.launch { userPreferences.setCrossfadeDuration(seconds) }
    }

    fun setGaplessPlayback(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setGaplessPlayback(enabled) }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { userPreferences.setThemeMode(mode) }
    }

    fun setPipedInstance(url: String) {
        viewModelScope.launch { userPreferences.setPipedInstance(url) }
    }

    fun setYtDlpFallback(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setYtDlpFallback(enabled) }
    }

    fun setSaveSearchHistory(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setSaveSearchHistory(enabled) }
    }

    fun setSavePlayHistory(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setSavePlayHistory(enabled) }
    }

    fun clearSearchHistory() {
        viewModelScope.launch { libraryRepository.clearHistory() }
    }

    fun clearPlayHistory() {
        viewModelScope.launch { libraryRepository.clearHistory() }
    }

    fun clearCaches() {
        viewModelScope.launch { userPreferences.clearAllCaches() }
    }
}
