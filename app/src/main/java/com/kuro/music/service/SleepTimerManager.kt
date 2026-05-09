package com.kuro.music.service

import android.os.CountDownTimer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class SleepTimerState(
    val isActive: Boolean = false,
    val remainingMs: Long = 0L,
    val totalMs: Long = 0L,
    val afterCurrentSong: Boolean = false
)

@Singleton
class SleepTimerManager @Inject constructor() {

    companion object {
        private const val TAG = "SleepTimerManager"
    }

    private val _state = MutableStateFlow(SleepTimerState())
    val state: StateFlow<SleepTimerState> = _state.asStateFlow()

    private var timer: CountDownTimer? = null
    private var onTimerFinished: (() -> Unit)? = null

    fun start(durationMs: Long, onFinished: () -> Unit) {
        cancel()
        onTimerFinished = onFinished
        Log.d(TAG, "Starting sleep timer: ${durationMs / 60000} minutes")

        _state.value = SleepTimerState(
            isActive = true,
            remainingMs = durationMs,
            totalMs = durationMs,
            afterCurrentSong = false
        )

        timer = object : CountDownTimer(durationMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _state.value = _state.value.copy(remainingMs = millisUntilFinished)
            }

            override fun onFinish() {
                Log.d(TAG, "Sleep timer finished")
                _state.value = SleepTimerState()
                onTimerFinished?.invoke()
            }
        }.start()
    }

    fun setAfterCurrentSong(onFinished: () -> Unit) {
        cancel()
        onTimerFinished = onFinished
        _state.value = SleepTimerState(
            isActive = true,
            afterCurrentSong = true
        )
    }

    fun checkSongEnd() {
        if (_state.value.afterCurrentSong && _state.value.isActive) {
            Log.d(TAG, "After current song: stopping")
            _state.value = SleepTimerState()
            onTimerFinished?.invoke()
        }
    }

    fun cancel() {
        timer?.cancel()
        timer = null
        onTimerFinished = null
        _state.value = SleepTimerState()
        Log.d(TAG, "Sleep timer cancelled")
    }

    fun formatRemaining(): String {
        val ms = _state.value.remainingMs
        val minutes = (ms / 60000).toInt()
        val seconds = ((ms % 60000) / 1000).toInt()
        return String.format("%d:%02d", minutes, seconds)
    }
}
