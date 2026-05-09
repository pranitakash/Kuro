package com.kuro.music.data.repository

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.kuro.music.data.local.dao.SongDao
import com.kuro.music.data.mapper.toEntity
import com.kuro.music.data.mapper.toSong
import com.kuro.music.domain.model.Song
import com.kuro.music.service.DownloadWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songDao: SongDao,
    private val streamRepository: StreamRepository
) {
    companion object {
        private const val TAG = "DownloadRepository"
    }

    private val workManager = WorkManager.getInstance(context)

    // Supervised scope so exceptions don't crash the app
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Download coroutine failed: ${throwable.message}", throwable)
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

    fun getDownloadedSongs(): Flow<List<Song>> =
        songDao.getDownloadedSongs().map { entities -> entities.map { it.toSong() } }

    fun getDownloadWorkInfo(songId: String): Flow<List<WorkInfo>> =
        workManager.getWorkInfosForUniqueWorkFlow("download_${songId}")

    fun downloadSong(song: Song, quality: String = "medium") {
        Log.d(TAG, "Queueing download: ${song.title} ($quality)")

        scope.launch {
            try {
                // Ensure the song is saved in DB first
                songDao.insertSong(song.toEntity())

                // Resolve stream URL using the full fallback chain
                val streamUrl = streamRepository.resolveStreamUrl(song.id)
                Log.d(TAG, "Stream URL resolved for download: ${streamUrl.take(80)}...")

                val inputData = Data.Builder()
                    .putString("song_id", song.id)
                    .putString("song_title", song.title)
                    .putString("song_artist", song.artist)
                    .putString("song_thumbnail", song.thumbnailUrl)
                    .putLong("song_duration", song.duration)
                    .putString("quality", quality)
                    .putString("stream_url", streamUrl)
                    .build()

                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val request = OneTimeWorkRequestBuilder<DownloadWorker>()
                    .setInputData(inputData)
                    .setConstraints(constraints)
                    .addTag("kuro_download")
                    .build()

                workManager.enqueueUniqueWork(
                    "download_${song.id}",
                    ExistingWorkPolicy.KEEP,
                    request
                )
                Log.d(TAG, "Download work enqueued for: ${song.title}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start download for ${song.title}: ${e.message}", e)
                // Show toast on main thread
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun cancelDownload(songId: String) {
        workManager.cancelUniqueWork("download_$songId")
    }

    suspend fun deleteDownload(songId: String) {
        val song = songDao.getSongById(songId) ?: return
        song.localPath?.let { path ->
            val file = File(path)
            if (file.exists()) file.delete()
        }
        songDao.updateSong(
            song.copy(
                isDownloaded = false,
                localPath = null,
                downloadQuality = null
            )
        )
    }

    fun getDownloadDir(): File {
        val dir = File(context.getExternalFilesDir(null), "Music/Kuro")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getStorageUsed(): Long {
        val dir = getDownloadDir()
        return if (dir.exists()) {
            dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        } else 0L
    }
}
