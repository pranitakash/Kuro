package com.kuro.music.service

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class EqPreset(
    val name: String,
    val bandLevels: ShortArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EqPreset) return false
        return name == other.name && bandLevels.contentEquals(other.bandLevels)
    }
    override fun hashCode() = 31 * name.hashCode() + bandLevels.contentHashCode()
}

@Singleton
class AudioEffectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AudioEffectManager"
    }

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    private var currentPresetIndex: Int = 0
    private var bassBoostStrength: Short = 0
    private var isEnabled: Boolean = false

    val builtInPresets: List<String>
        get() = equalizer?.let { eq ->
            (0 until eq.numberOfPresets).map { eq.getPresetName(it.toShort()) }
        } ?: listOf("Normal", "Bass Boost", "Rock", "Pop", "Jazz", "Classical")

    val numberOfBands: Int
        get() = equalizer?.numberOfBands?.toInt() ?: 5

    val bandFreqRange: List<Int>
        get() = equalizer?.let { eq ->
            (0 until eq.numberOfBands).map { eq.getCenterFreq(it.toShort()) / 1000 }
        } ?: emptyList()

    val bandLevelRange: Pair<Short, Short>
        get() = equalizer?.bandLevelRange?.let { Pair(it[0], it[1]) } ?: Pair(-1500, 1500)

    fun attachToSession(audioSessionId: Int) {
        try {
            release()
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = isEnabled
            }
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = isEnabled
                if (strengthSupported) {
                    setStrength(bassBoostStrength)
                }
            }
            loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                enabled = false
            }
            Log.d(TAG, "Audio effects attached to session: $audioSessionId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach audio effects: ${e.message}", e)
        }
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        equalizer?.enabled = enabled
        bassBoost?.enabled = enabled
    }

    fun selectPreset(index: Int) {
        try {
            equalizer?.usePreset(index.toShort())
            currentPresetIndex = index
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set preset: ${e.message}")
        }
    }

    fun setBandLevel(band: Int, level: Short) {
        try {
            equalizer?.setBandLevel(band.toShort(), level)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set band level: ${e.message}")
        }
    }

    fun getBandLevel(band: Int): Short =
        equalizer?.getBandLevel(band.toShort()) ?: 0

    fun setBassBoostStrength(strength: Short) {
        bassBoostStrength = strength
        try {
            bassBoost?.let {
                if (it.strengthSupported) {
                    it.setStrength(strength)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set bass boost: ${e.message}")
        }
    }

    fun setLoudnessGain(gainMb: Int) {
        try {
            loudnessEnhancer?.setTargetGain(gainMb)
            loudnessEnhancer?.enabled = gainMb > 0
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set loudness: ${e.message}")
        }
    }

    fun getCurrentPresetIndex(): Int = currentPresetIndex
    fun getBassBoostStrength(): Short = bassBoostStrength
    fun isEffectsEnabled(): Boolean = isEnabled

    fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
            loudnessEnhancer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing effects: ${e.message}")
        }
        equalizer = null
        bassBoost = null
        loudnessEnhancer = null
    }
}
