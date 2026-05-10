package com.kuro.music.presentation.viewmodel

import android.content.ComponentName
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.kuro.music.data.local.dao.HistoryDao
import com.kuro.music.data.local.dao.LikedSongDao
import com.kuro.music.data.local.dao.SongDao
import com.kuro.music.data.local.entity.HistoryEntity
import com.kuro.music.data.local.entity.LikedSongEntity
import com.kuro.music.data.mapper.toEntity
import com.kuro.music.data.repository.StreamRepository
import com.kuro.music.domain.model.Song
import com.kuro.music.service.MusicService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.coroutines.resume

data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val shuffleEnabled: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLiked: Boolean = false
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val streamRepository: StreamRepository,
    private val songDao: SongDao,
    private val historyDao: HistoryDao,
    private val likedSongDao: LikedSongDao
) : ViewModel() {

    companion object {
        private const val TAG = "PlayerViewModel"
    }

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var positionUpdateJob: kotlinx.coroutines.Job? = null
    private var isControllerConnected = false

    init {
        connectToService()
    }

    private fun connectToService() {
        try {
            val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
            controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
            controllerFuture?.addListener({
                try {
                    mediaController = controllerFuture?.get()
                    mediaController?.addListener(playerListener)
                    isControllerConnected = true
                    startPositionUpdates()
                    Log.d(TAG, "MediaController connected successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to connect MediaController: ${e.message}", e)
                    isControllerConnected = false
                }
            }, MoreExecutors.directExecutor())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create session token: ${e.message}", e)
        }
    }

    /**
     * Wait until the MediaController is connected, with a timeout.
     */
    private suspend fun awaitController(): MediaController? {
        // Already connected
        mediaController?.let { return it }

        // Wait up to 3 seconds for the connection (reduced from 5s)
        return withTimeoutOrNull(3000L) {
            while (mediaController == null) {
                delay(50) // Check more frequently for faster response
            }
            mediaController
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
            Log.d(TAG, "isPlaying changed: $isPlaying")
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            mediaItem?.let { item ->
                val queue = _playerState.value.queue
                val index = queue.indexOfFirst { it.id == item.mediaId }
                val song = if (index >= 0) queue[index] else null
                _playerState.value = _playerState.value.copy(
                    currentSong = song,
                    currentIndex = index,
                    duration = mediaController?.duration?.coerceAtLeast(0) ?: 0L
                )
                song?.let { s ->
                    recordHistory(s)
                    // Update liked state for new song
                    viewModelScope.launch {
                        val liked = likedSongDao.isSongLikedSync(s.id)
                        _playerState.value = _playerState.value.copy(isLiked = liked)
                    }
                }
                Log.d(TAG, "Media item transition: ${song?.title}")
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateName = when (playbackState) {
                Player.STATE_IDLE -> "IDLE"
                Player.STATE_BUFFERING -> "BUFFERING"
                Player.STATE_READY -> "READY"
                Player.STATE_ENDED -> "ENDED"
                else -> "UNKNOWN($playbackState)"
            }
            Log.d(TAG, "Playback state: $stateName")

            when (playbackState) {
                Player.STATE_READY -> {
                    _playerState.value = _playerState.value.copy(
                        isLoading = false,
                        duration = mediaController?.duration?.coerceAtLeast(0) ?: 0L
                    )
                }
                Player.STATE_BUFFERING -> {
                    _playerState.value = _playerState.value.copy(isLoading = true)
                }
                Player.STATE_ENDED -> {
                    _playerState.value = _playerState.value.copy(isPlaying = false)
                }
                else -> {}
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e(TAG, "Player error: ${error.message} (code: ${error.errorCode})", error)
            val errorMsg = when (error.errorCode) {
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
                    "Network error — check your connection"
                PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ->
                    "Stream unavailable (HTTP error)"
                PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND ->
                    "Audio stream not found"
                PlaybackException.ERROR_CODE_DECODER_INIT_FAILED ->
                    "Audio format not supported"
                else -> "Playback error: ${error.message}"
            }
            _playerState.value = _playerState.value.copy(
                isLoading = false,
                error = errorMsg
            )
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _playerState.value = _playerState.value.copy(shuffleEnabled = shuffleModeEnabled)
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _playerState.value = _playerState.value.copy(repeatMode = repeatMode)
        }
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = viewModelScope.launch {
            while (true) {
                mediaController?.let { controller ->
                    if (controller.isPlaying) {
                        _playerState.value = _playerState.value.copy(
                            currentPosition = controller.currentPosition.coerceAtLeast(0),
                            duration = controller.duration.coerceAtLeast(0)
                        )
                    }
                }
                delay(250)
            }
        }
    }

    fun playSong(song: Song) {
        viewModelScope.launch {
            _playerState.value = _playerState.value.copy(isLoading = true, error = null)
            Log.d(TAG, "playSong called: ${song.title} (id: ${song.id})")

            try {
                // Resolve stream URL
                Log.d(TAG, "Resolving stream URL...")
                val streamUrl = try {
                    streamRepository.resolveStreamUrl(song.id)
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e // Don't swallow cancellation
                } catch (e: Exception) {
                    Log.e(TAG, "Stream resolution failed: ${e.message}", e)
                    _playerState.value = _playerState.value.copy(
                        isLoading = false,
                        error = "Could not load song — ${e.message ?: "try again"}"
                    )
                    return@launch
                }
                Log.d(TAG, "Stream URL resolved: ${streamUrl.take(80)}...")

                // Connect to media controller
                val controller = awaitController()
                if (controller == null) {
                    Log.e(TAG, "MediaController not available after waiting")
                    _playerState.value = _playerState.value.copy(
                        isLoading = false,
                        error = "Player service not ready — try again"
                    )
                    return@launch
                }

                val updatedSong = song.copy(streamUrl = streamUrl)

                // Save song to DB (fire-and-forget, don't block playback)
                launch { songDao.insertSong(updatedSong.toEntity()) }

                // Build media item and play immediately
                val mediaItem = MusicService.buildMediaItem(
                    id = song.id,
                    title = song.title,
                    artist = song.artist,
                    thumbnailUrl = song.thumbnailUrl,
                    streamUrl = streamUrl
                )

                Log.d(TAG, "Setting media item and playing...")
                controller.setMediaItem(mediaItem)
                controller.prepare()
                controller.play()

                // Check liked state without blocking playback
                val isLiked = likedSongDao.isSongLikedSync(song.id)

                _playerState.value = _playerState.value.copy(
                    currentSong = updatedSong,
                    queue = listOf(updatedSong),
                    currentIndex = 0,
                    isLoading = false,
                    isLiked = isLiked
                )
                Log.d(TAG, "Play command issued for: ${song.title}")

            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e // Rethrow cancellation — this is not an error
            } catch (e: Exception) {
                Log.e(TAG, "playSong failed: ${e.message}", e)
                _playerState.value = _playerState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to play song"
                )
            }
        }
    }

    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        viewModelScope.launch {
            if (songs.isEmpty()) return@launch
            _playerState.value = _playerState.value.copy(isLoading = true, error = null)
            try {
                val startSong = songs[startIndex]

                // Resolve stream URL and controller in parallel
                val streamUrlDeferred = async { streamRepository.resolveStreamUrl(startSong.id) }
                val controllerDeferred = async { awaitController() }

                val streamUrl = streamUrlDeferred.await()
                val controller = controllerDeferred.await()

                if (controller == null) {
                    _playerState.value = _playerState.value.copy(
                        isLoading = false,
                        error = "Player service not ready"
                    )
                    return@launch
                }

                val mediaItems = songs.map { song ->
                    MusicService.buildMediaItem(
                        id = song.id,
                        title = song.title,
                        artist = song.artist,
                        thumbnailUrl = song.thumbnailUrl,
                        streamUrl = if (song.id == startSong.id) streamUrl else ""
                    )
                }

                controller.setMediaItems(mediaItems, startIndex, 0)
                controller.prepare()
                controller.play()

                val isLiked = likedSongDao.isSongLikedSync(startSong.id)
                _playerState.value = _playerState.value.copy(
                    currentSong = startSong.copy(streamUrl = streamUrl),
                    queue = songs,
                    currentIndex = startIndex,
                    isLoading = false,
                    isLiked = isLiked
                )
            } catch (e: Exception) {
                Log.e(TAG, "playQueue failed: ${e.message}", e)
                _playerState.value = _playerState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to play queue"
                )
            }
        }
    }

    fun addToQueue(song: Song) {
        val currentQueue = _playerState.value.queue.toMutableList()
        currentQueue.add(song)
        _playerState.value = _playerState.value.copy(queue = currentQueue)

        mediaController?.let { controller ->
            val mediaItem = MusicService.buildMediaItem(
                id = song.id,
                title = song.title,
                artist = song.artist,
                thumbnailUrl = song.thumbnailUrl,
                streamUrl = ""
            )
            controller.addMediaItem(mediaItem)
        }
    }

    fun playPause() {
        mediaController?.let { controller ->
            if (controller.isPlaying) controller.pause() else controller.play()
        }
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
        _playerState.value = _playerState.value.copy(currentPosition = position)
    }

    fun skipNext() {
        mediaController?.let { controller ->
            if (controller.hasNextMediaItem()) {
                controller.seekToNextMediaItem()
            }
        }
    }

    fun skipPrevious() {
        mediaController?.let { controller ->
            if (controller.currentPosition > 3000) {
                controller.seekTo(0)
            } else if (controller.hasPreviousMediaItem()) {
                controller.seekToPreviousMediaItem()
            }
        }
    }

    fun seekForward() {
        mediaController?.let { controller ->
            controller.seekTo((controller.currentPosition + 10000).coerceAtMost(controller.duration))
        }
    }

    fun seekBackward() {
        mediaController?.let { controller ->
            controller.seekTo((controller.currentPosition - 10000).coerceAtLeast(0))
        }
    }

    fun toggleShuffle() {
        mediaController?.let { controller ->
            controller.shuffleModeEnabled = !controller.shuffleModeEnabled
        }
    }

    fun toggleRepeat() {
        mediaController?.let { controller ->
            controller.repeatMode = when (controller.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
        }
    }

    fun toggleLike() {
        val currentSong = _playerState.value.currentSong ?: return
        viewModelScope.launch {
            val isCurrentlyLiked = likedSongDao.isSongLikedSync(currentSong.id)
            if (isCurrentlyLiked) {
                likedSongDao.unlikeSong(currentSong.id)
                _playerState.value = _playerState.value.copy(isLiked = false)
            } else {
                songDao.insertSong(currentSong.toEntity())
                likedSongDao.likeSong(LikedSongEntity(songId = currentSong.id))
                _playerState.value = _playerState.value.copy(isLiked = true)
            }
        }
    }

    fun removeFromQueue(index: Int) {
        val currentQueue = _playerState.value.queue.toMutableList()
        if (index in currentQueue.indices) {
            currentQueue.removeAt(index)
            _playerState.value = _playerState.value.copy(queue = currentQueue)
            mediaController?.removeMediaItem(index)
        }
    }

    fun clearQueue() {
        _playerState.value = _playerState.value.copy(queue = emptyList(), currentIndex = -1)
        mediaController?.clearMediaItems()
    }

    private fun recordHistory(song: Song) {
        viewModelScope.launch {
            songDao.insertSong(song.toEntity())
            historyDao.insertHistory(HistoryEntity(songId = song.id))
        }
    }

    fun clearError() {
        _playerState.value = _playerState.value.copy(error = null)
    }

    override fun onCleared() {
        positionUpdateJob?.cancel()
        mediaController?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        super.onCleared()
    }
}
