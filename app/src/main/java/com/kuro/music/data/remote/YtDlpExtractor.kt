package com.kuro.music.data.remote

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YtDlpExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val ytDlpFile: File
        get() = File(context.filesDir, "yt-dlp")

    suspend fun ensureInstalled() = withContext(Dispatchers.IO) {
        if (!ytDlpFile.exists()) {
            // Copy yt-dlp binary from assets
            try {
                val arch = System.getProperty("os.arch") ?: "arm64-v8a"
                val assetName = when {
                    arch.contains("aarch64") || arch.contains("arm64") -> "yt-dlp/arm64-v8a/yt-dlp"
                    arch.contains("x86_64") -> "yt-dlp/x86_64/yt-dlp"
                    else -> "yt-dlp/arm64-v8a/yt-dlp"
                }
                context.assets.open(assetName).use { input ->
                    ytDlpFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                ytDlpFile.setExecutable(true)
            } catch (e: Exception) {
                // yt-dlp binary not bundled yet — this is expected during development
            }
        }
    }

    suspend fun extractAudioUrl(videoId: String): String = withContext(Dispatchers.IO) {
        ensureInstalled()
        if (!ytDlpFile.exists()) {
            throw Exception("yt-dlp binary not found")
        }

        val process = ProcessBuilder(
            ytDlpFile.absolutePath,
            "-f", "bestaudio[ext=m4a]/bestaudio",
            "--get-url",
            "https://www.youtube.com/watch?v=$videoId"
        ).redirectErrorStream(true).start()

        val url = process.inputStream.bufferedReader().readLine()
        process.waitFor()

        url ?: throw Exception("yt-dlp failed to extract URL for $videoId")
    }
}
