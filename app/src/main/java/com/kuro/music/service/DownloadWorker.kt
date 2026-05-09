package com.kuro.music.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kuro.music.data.local.KuroDatabase
import com.kuro.music.data.local.entity.SongEntity
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "DownloadWorker"
        private const val NOTIFICATION_CHANNEL_ID = "kuro_downloads"
        private const val NOTIFICATION_ID = 2000
    }

    override suspend fun doWork(): Result {
        val songId = inputData.getString("song_id") ?: return Result.failure()
        val title = inputData.getString("song_title") ?: "Unknown"
        val artist = inputData.getString("song_artist") ?: "Unknown"
        val thumbnail = inputData.getString("song_thumbnail") ?: ""
        val duration = inputData.getLong("song_duration", 0L)
        val quality = inputData.getString("quality") ?: "medium"
        val preResolvedUrl = inputData.getString("stream_url")

        Log.d(TAG, "Starting download: $title by $artist (id=$songId)")
        Log.d(TAG, "Pre-resolved URL present: ${!preResolvedUrl.isNullOrBlank()}")

        createNotificationChannel()
        showProgressNotification(title, 0)

        return try {
            val db = KuroDatabase.getInstance(context)
            val songDao = db.songDao()

            // Use pre-resolved URL from DownloadRepository
            val streamUrl = if (!preResolvedUrl.isNullOrBlank()) {
                Log.d(TAG, "Using pre-resolved stream URL")
                preResolvedUrl
            } else {
                Log.d(TAG, "No pre-resolved URL, resolving via Innertube...")
                val resolved = resolveStreamUrl(songId)
                if (resolved.isNullOrBlank()) {
                    Log.e(TAG, "Failed to resolve stream URL for: $songId")
                    showErrorNotification(title, "Could not resolve audio stream")
                    return Result.failure()
                }
                resolved
            }

            Log.d(TAG, "Stream URL ready, starting file download...")

            // Create download directory
            val downloadDir = File(context.getExternalFilesDir(null), "Music/Kuro")
            val artistDir = File(downloadDir, sanitizeFilename(artist))
            artistDir.mkdirs()

            val fileName = "${sanitizeFilename(title)}.m4a"
            val outputFile = File(artistDir, fileName)

            // Download the file
            downloadFile(streamUrl, outputFile, title)

            if (!outputFile.exists() || outputFile.length() == 0L) {
                Log.e(TAG, "Download resulted in empty or missing file")
                showErrorNotification(title, "Download failed - empty file")
                return Result.failure()
            }

            Log.d(TAG, "File downloaded: ${outputFile.absolutePath} (${outputFile.length()} bytes)")

            // Update database
            val existingSong = songDao.getSongById(songId)
            val songEntity = existingSong?.copy(
                isDownloaded = true,
                localPath = outputFile.absolutePath,
                downloadQuality = quality
            ) ?: SongEntity(
                id = songId,
                title = title,
                artist = artist,
                thumbnailUrl = thumbnail,
                duration = duration,
                isDownloaded = true,
                localPath = outputFile.absolutePath,
                downloadQuality = quality
            )
            songDao.insertSong(songEntity)

            Log.d(TAG, "Download complete: $title → ${outputFile.absolutePath}")
            showCompleteNotification(title)
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Download failed: ${e.message}", e)
            showErrorNotification(title, e.message ?: "Unknown error")
            if (runAttemptCount < 2) Result.retry() else Result.failure()
        }
    }

    private suspend fun resolveStreamUrl(videoId: String): String? {
        return try {
            val client = com.kuro.music.data.remote.InnertubeClient(
                okhttp3.OkHttpClient.Builder()
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .build()
            )
            val result = client.getAudioStreamUrlWithFallback(videoId)
            Log.d(TAG, "Resolved stream URL via Innertube")
            result.url
        } catch (e: Exception) {
            Log.e(TAG, "Innertube resolution failed: ${e.message}", e)
            null
        }
    }

    private fun downloadFile(url: String, outputFile: File, title: String) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 15000
        connection.readTimeout = 60000
        connection.instanceFollowRedirects = true
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")

        try {
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                throw java.io.IOException("HTTP $responseCode: ${connection.responseMessage}")
            }

            val totalBytes = connection.contentLengthLong.coerceAtLeast(0)
            Log.d(TAG, "Download response: HTTP $responseCode, content-length: $totalBytes")

            val inputStream = connection.inputStream
            val outputStream = FileOutputStream(outputFile)

            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalRead = 0L
            var lastProgressUpdate = 0

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalRead += bytesRead

                if (totalBytes > 0) {
                    val progress = ((totalRead * 100) / totalBytes).toInt()
                    setProgressAsync(
                        androidx.work.Data.Builder()
                            .putInt("progress", progress)
                            .build()
                    )
                    if (progress >= lastProgressUpdate + 10) {
                        lastProgressUpdate = progress
                        showProgressNotification(title, progress)
                    }
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            Log.d(TAG, "File write complete: $totalRead bytes written")
        } finally {
            connection.disconnect()
        }
    }

    private fun sanitizeFilename(name: String): String =
        name.replace(Regex("[^a-zA-Z0-9._\\- ]"), "").trim().take(100)

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music download progress"
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun showProgressNotification(title: String, progress: Int) {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle("Downloading")
                .setContentText(title)
                .setProgress(100, progress, progress == 0)
                .setOngoing(true)
                .setSilent(true)
                .build()
            nm.notify(NOTIFICATION_ID, notification)
        } catch (_: Exception) {}
    }

    private fun showCompleteNotification(title: String) {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // Dismiss progress notification
            nm.cancel(NOTIFICATION_ID)
            val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("Download complete")
                .setContentText(title)
                .setAutoCancel(true)
                .build()
            nm.notify(NOTIFICATION_ID + 1, notification)
        } catch (_: Exception) {}
    }

    private fun showErrorNotification(title: String, error: String) {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(NOTIFICATION_ID)
            val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle("Download failed")
                .setContentText("$title: $error")
                .setAutoCancel(true)
                .build()
            nm.notify(NOTIFICATION_ID + 2, notification)
        } catch (_: Exception) {}
    }
}
