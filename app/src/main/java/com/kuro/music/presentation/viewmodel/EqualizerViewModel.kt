package com.kuro.music.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.kuro.music.service.AudioEffectManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class EqualizerState(
    val isEnabled: Boolean = false,
    val presets: List<String> = emptyList(),
    val selectedPresetIndex: Int = 0,
    val bandFrequencies: List<Int> = emptyList(),
    val bandLevels: List<Short> = emptyList(),
    val minBandLevel: Short = -1500,
    val maxBandLevel: Short = 1500,
    val bassBoostStrength: Int = 0,
    val numberOfBands: Int = 5
)

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val audioEffectManager: AudioEffectManager
) : ViewModel() {

    private val _state = MutableStateFlow(EqualizerState())
    val state: StateFlow<EqualizerState> = _state.asStateFlow()

    fun initialize() {
        val presets = audioEffectManager.builtInPresets
        val numBands = audioEffectManager.numberOfBands
        val freqs = audioEffectManager.bandFreqRange
        val levels = (0 until numBands).map { audioEffectManager.getBandLevel(it) }
        val range = audioEffectManager.bandLevelRange

        _state.value = EqualizerState(
            isEnabled = audioEffectManager.isEffectsEnabled(),
            presets = presets,
            selectedPresetIndex = audioEffectManager.getCurrentPresetIndex(),
            bandFrequencies = freqs,
            bandLevels = levels,
            minBandLevel = range.first,
            maxBandLevel = range.second,
            bassBoostStrength = audioEffectManager.getBassBoostStrength().toInt(),
            numberOfBands = numBands
        )
    }

    fun toggleEnabled(enabled: Boolean) {
        audioEffectManager.setEnabled(enabled)
        _state.value = _state.value.copy(isEnabled = enabled)
    }

    fun selectPreset(index: Int) {
        audioEffectManager.selectPreset(index)
        // Re-read band levels after preset change
        val numBands = _state.value.numberOfBands
        val levels = (0 until numBands).map { audioEffectManager.getBandLevel(it) }
        _state.value = _state.value.copy(
            selectedPresetIndex = index,
            bandLevels = levels
        )
    }

    fun setBandLevel(band: Int, level: Short) {
        audioEffectManager.setBandLevel(band, level)
        val levels = _state.value.bandLevels.toMutableList()
        if (band < levels.size) {
            levels[band] = level
            _state.value = _state.value.copy(bandLevels = levels)
        }
    }

    fun setBassBoost(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        audioEffectManager.setBassBoostStrength(clamped.toShort())
        _state.value = _state.value.copy(bassBoostStrength = clamped)
    }
}
