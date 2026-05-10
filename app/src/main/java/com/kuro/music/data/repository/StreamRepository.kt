package com.kuro.music.data.repository

import android.util.Log
import com.kuro.music.data.local.dao.SongDao
import com.kuro.music.data.remote.InnertubeClient
import com.kuro.music.data.remote.PipedApiService
import com.kuro.music.data.remote.PipedInstanceManager
import com.kuro.music.data.remote.WebViewStreamExtractor
import com.kuro.music.data.remote.YtDlpExtractor
import com.kuro.music.data.remote.dto.PipedStreamResponse
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamRepository @Inject constructor(
    private val pipedApi: PipedApiService,
    private val innertubeClient: InnertubeClient,
    private val webViewExtractor: WebViewStreamExtractor,
    private val instanceManager: PipedInstanceManager,
    private val okHttpClient: OkHttpClient,
    private val ytDlpExtractor: YtDlpExtractor,
    private val songDao: SongDao
) {
    companion object {
        private const val TAG = "StreamRepository"
        private const val CACHE_TTL_MS = 55 * 60 * 1000L // 55 minutes (URLs expire in ~60min)
    }

    private val urlCache = mutableMapOf<String, CachedUrl>()
    private val cacheMutex = Mutex()

    private data class CachedUrl(
        val url: String,
        val expiry: Long
    )

    private fun createApiForInstance(baseUrl: String): PipedApiService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PipedApiService::class.java)
    }

    /**
     * Resolves the best audio stream URL for a video.
     *
     * Strategy (ordered by current reliability):
     * 1. In-memory cache (instant)
     * 2. Room DB cache (instant)
     * 3. Piped API instances (most reliable — Innertube is blocked by YouTube)
     * 4. Innertube API (fast-fail fallback, currently returns LOGIN_REQUIRED)
     * 5. WebView extraction (slow but sometimes works)
     * 6. yt-dlp binary (last resort)
     */
    suspend fun resolveStreamUrl(videoId: String): String {
        // Global timeout to prevent the app from hanging forever
        val result = withTimeoutOrNull(15_000L) {
            resolveStreamUrlInternal(videoId)
        }
        return result ?: throw Exception("Stream resolution timed out — try again")
    }

    private suspend fun resolveStreamUrlInternal(videoId: String): String {
        // 1. Check in-memory cache
        cacheMutex.withLock {
            urlCache[videoId]?.let { cached ->
                if (cached.expiry > System.currentTimeMillis()) {
                    Log.d(TAG, "Cache hit (memory) for $videoId")
                    return cached.url
                }
                urlCache.remove(videoId)
            }
        }

        // 2. Check Room cache
        songDao.getCachedStreamUrl(videoId)?.let { cachedUrl ->
            Log.d(TAG, "Cache hit (Room) for $videoId")
            cacheUrl(videoId, cachedUrl)
            return cachedUrl
        }

        // 3. Try Piped API instances first (currently most reliable)
        for (instanceUrl in PipedInstanceManager.INSTANCES) {
            try {
                Log.d(TAG, "Trying Piped instance: $instanceUrl")
                val api = createApiForInstance(instanceUrl)
                val response = api.getStreams(videoId)
                val bestAudio = response.audioStreams
                    .filter { it.mimeType.contains("audio") }
                    .maxByOrNull { it.bitrate }
                    ?: continue

                Log.d(TAG, "Piped success ($instanceUrl): ${bestAudio.mimeType}")
                cacheUrl(videoId, bestAudio.url)
                return bestAudio.url
            } catch (e: Exception) {
                Log.d(TAG, "Piped failed ($instanceUrl): ${e.message}")
            }
        }

        // 4. Try Innertube API (currently blocked, fast-fails in ~2s per client)
        try {
            Log.d(TAG, "Trying Innertube for $videoId...")
            val result = innertubeClient.getAudioStreamUrlWithFallback(videoId)
            Log.d(TAG, "Innertube success: ${result.mimeType} @ ${result.bitrate}bps")
            cacheUrl(videoId, result.url)
            return result.url
        } catch (e: Exception) {
            Log.w(TAG, "Innertube failed for $videoId: ${e.message}")
        }

        // 5. Try WebView extraction (pre-warmed, reused)
        try {
            Log.d(TAG, "Trying WebView extraction for $videoId...")
            val url = webViewExtractor.extractAudioUrl(videoId)
            Log.d(TAG, "WebView extraction success!")
            cacheUrl(videoId, url)
            return url
        } catch (e: Exception) {
            Log.w(TAG, "WebView extraction failed for $videoId: ${e.message}")
        }

        // 6. Final fallback: yt-dlp
        try {
            Log.d(TAG, "Trying yt-dlp for $videoId...")
            val url = ytDlpExtractor.extractAudioUrl(videoId)
            Log.d(TAG, "yt-dlp success")
            cacheUrl(videoId, url)
            return url
        } catch (e: Exception) {
            Log.e(TAG, "yt-dlp failed: ${e.message}")
        }

        throw Exception("Could not resolve audio for this song — all sources failed")
    }

    /**
     * Pre-resolves and caches a stream URL in the background.
     * IMPORTANT: Only uses Innertube (fast API) — never touches the WebView.
     * This prevents background prefetching from blocking the shared WebView
     * when the user actually taps a song.
     */
    suspend fun prefetchStreamUrl(videoId: String) {
        // Skip if already cached
        cacheMutex.withLock {
            urlCache[videoId]?.let { cached ->
                if (cached.expiry > System.currentTimeMillis()) return
            }
        }
        songDao.getCachedStreamUrl(videoId)?.let { return }

        // Try Piped first for prefetch (Innertube is currently blocked)
        try {
            val instance = instanceManager.getCurrentInstance()
            val api = createApiForInstance(instance)
            val response = api.getStreams(videoId)
            val bestAudio = response.audioStreams
                .filter { it.mimeType.contains("audio") }
                .maxByOrNull { it.bitrate }
            if (bestAudio != null) {
                cacheUrl(videoId, bestAudio.url)
                Log.d(TAG, "Prefetch success (Piped) for $videoId")
                return
            }
        } catch (e: Exception) {
            Log.d(TAG, "Prefetch Piped failed for $videoId: ${e.message}")
        }

        // Fallback to Innertube for prefetch
        try {
            val result = innertubeClient.getAudioStreamUrlWithFallback(videoId)
            cacheUrl(videoId, result.url)
            Log.d(TAG, "Prefetch success (Innertube) for $videoId")
        } catch (e: Exception) {
            // Silently fail — prefetch is best-effort
            Log.d(TAG, "Prefetch skipped for $videoId (all sources unavailable)")
        }
    }

    suspend fun getStreamMetadata(videoId: String): PipedStreamResponse {
        val instance = instanceManager.getCurrentInstance()
        val api = createApiForInstance(instance)
        return api.getStreams(videoId)
    }

    private suspend fun cacheUrl(videoId: String, url: String) {
        val expiry = System.currentTimeMillis() + CACHE_TTL_MS
        cacheMutex.withLock {
            urlCache[videoId] = CachedUrl(url, expiry)
        }
        try {
            songDao.updateStreamCache(videoId, url, expiry)
        } catch (e: Exception) {
            // Ignore DB cache failures
        }
    }
}

