package com.kuro.music.data.remote

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.TimeUnit

/**
 * Direct YouTube Innertube API client for resolving audio stream URLs.
 * This bypasses Piped entirely and talks directly to YouTube's internal API,
 * similar to how InnerTune/ViMusic work.
 *
 * Uses the ANDROID_MUSIC client context for reliable audio stream access.
 */
@Singleton
class InnertubeClient @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val TAG = "InnertubeClient"

        // Innertube player endpoint
        private const val PLAYER_URL =
            "https://music.youtube.com/youtubei/v1/player?prettyPrint=false"

        // Android Music client — most reliable for audio streams
        private const val CLIENT_NAME = "ANDROID_MUSIC"
        private const val CLIENT_VERSION = "7.27.52"
        private const val API_KEY = "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8"

        // User agent matching the client
        private const val USER_AGENT =
            "com.google.android.apps.youtube.music/7.27.52 (Linux; U; Android 14; en_US) gzip"

        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }

    // Fast client with very short timeouts — bail quickly when Innertube is blocked
    private val fastClient = okHttpClient.newBuilder()
        .connectTimeout(2, TimeUnit.SECONDS)
        .readTimeout(2, TimeUnit.SECONDS)
        .writeTimeout(2, TimeUnit.SECONDS)
        .build()

    data class AudioStreamResult(
        val url: String,
        val mimeType: String,
        val bitrate: Int,
        val durationMs: Long
    )

    /**
     * Resolves the best audio stream URL for a given video ID using the Innertube API.
     */
    suspend fun getAudioStreamUrl(videoId: String): AudioStreamResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "Resolving audio for: $videoId")

        val requestBody = buildPlayerRequestBody(videoId)

        val request = Request.Builder()
            .url("$PLAYER_URL&key=$API_KEY")
            .header("User-Agent", USER_AGENT)
            .header("Content-Type", "application/json")
            .header("X-Goog-Api-Format-Version", "2")
            .post(requestBody.toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val response = fastClient.newCall(request).execute()

        if (!response.isSuccessful) {
            response.close()
            throw Exception("Innertube player request failed: ${response.code}")
        }

        val responseBody = response.body?.string() ?: throw Exception("Empty response body")
        response.close()

        parseAudioStream(responseBody, videoId)
    }

    /**
     * Tries to get an audio stream URL using multiple Innertube clients.
     * Falls through quickly (~2s per client) so the caller can move to WebView.
     */
    suspend fun getAudioStreamUrlWithFallback(videoId: String): AudioStreamResult {
        // Try ANDROID_MUSIC first (best for music content)
        try {
            return getAudioStreamUrl(videoId)
        } catch (e: Exception) {
            Log.d(TAG, "ANDROID_MUSIC failed: ${e.message}")
        }

        // Try ANDROID client
        try {
            return getAudioStreamUrlAndroid(videoId)
        } catch (e: Exception) {
            Log.d(TAG, "ANDROID failed: ${e.message}")
        }

        // Try iOS client (different CDN routing, sometimes works when Android is blocked)
        try {
            return getAudioStreamUrlIOS(videoId)
        } catch (e: Exception) {
            Log.d(TAG, "IOS failed: ${e.message}")
        }

        throw Exception("All Innertube clients failed for $videoId")
    }

    private suspend fun getAudioStreamUrlAndroid(videoId: String): AudioStreamResult =
        withContext(Dispatchers.IO) {
            val body = """
            {
                "context": {
                    "client": {
                        "clientName": "ANDROID",
                        "clientVersion": "19.29.37",
                        "androidSdkVersion": 34,
                        "hl": "en",
                        "gl": "US",
                        "userAgent": "com.google.android.youtube/19.29.37 (Linux; U; Android 14) gzip"
                    }
                },
                "videoId": "$videoId",
                "playbackContext": {
                    "contentPlaybackContext": {
                        "signatureTimestamp": 20073
                    }
                },
                "contentCheckOk": true,
                "racyCheckOk": true
            }
        """.trimIndent()

            val request = Request.Builder()
                .url("https://www.youtube.com/youtubei/v1/player?key=AIzaSyA8eiZmM1FaDVjRy-df2KTyQ_vz_yYM39w&prettyPrint=false")
                .header("User-Agent", "com.google.android.youtube/19.29.37 (Linux; U; Android 14) gzip")
                .header("Content-Type", "application/json")
                .post(body.toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = fastClient.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                throw Exception("Android client failed: ${response.code}")
            }
            val responseBody = response.body?.string() ?: throw Exception("Empty response")
            response.close()
            parseAudioStream(responseBody, videoId)
        }

    private suspend fun getAudioStreamUrlIOS(videoId: String): AudioStreamResult =
        withContext(Dispatchers.IO) {
            val body = """
            {
                "context": {
                    "client": {
                        "clientName": "IOS",
                        "clientVersion": "19.29.1",
                        "deviceMake": "Apple",
                        "deviceModel": "iPhone16,2",
                        "hl": "en",
                        "gl": "US",
                        "userAgent": "com.google.ios.youtube/19.29.1 (iPhone16,2; U; CPU iOS 17_5_1 like Mac OS X)"
                    }
                },
                "videoId": "$videoId",
                "playbackContext": {
                    "contentPlaybackContext": {
                        "signatureTimestamp": 20073
                    }
                },
                "contentCheckOk": true,
                "racyCheckOk": true
            }
        """.trimIndent()

            val request = Request.Builder()
                .url("https://www.youtube.com/youtubei/v1/player?key=AIzaSyB-63vPrdThhKuerbB2N_l7Kwwcxj6yUAc&prettyPrint=false")
                .header("User-Agent", "com.google.ios.youtube/19.29.1 (iPhone16,2; U; CPU iOS 17_5_1 like Mac OS X)")
                .header("Content-Type", "application/json")
                .post(body.toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = fastClient.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                throw Exception("iOS client failed: ${response.code}")
            }
            val responseBody = response.body?.string() ?: throw Exception("Empty response")
            response.close()
            parseAudioStream(responseBody, videoId)
        }

    private fun buildPlayerRequestBody(videoId: String): String {
        return """
        {
            "context": {
                "client": {
                    "clientName": "$CLIENT_NAME",
                    "clientVersion": "$CLIENT_VERSION",
                    "androidSdkVersion": 34,
                    "hl": "en",
                    "gl": "US",
                    "userAgent": "$USER_AGENT"
                }
            },
            "videoId": "$videoId",
            "playbackContext": {
                "contentPlaybackContext": {
                    "signatureTimestamp": 20073
                }
            },
            "contentCheckOk": true,
            "racyCheckOk": true
        }
    """.trimIndent()
    }

    private fun parseAudioStream(responseBody: String, videoId: String): AudioStreamResult {
        try {
            val json = JsonParser.parseString(responseBody).asJsonObject

            // Check for playability errors
            val playabilityStatus = json.getAsJsonObject("playabilityStatus")
            val status = playabilityStatus?.get("status")?.asString
            if (status != "OK") {
                val reason = playabilityStatus?.get("reason")?.asString
                    ?: playabilityStatus?.getAsJsonArray("messages")?.firstOrNull()?.asString
                    ?: "Unknown playability error"
                Log.e(TAG, "Playability status: $status, reason: $reason")
                throw Exception("Video not playable: $reason")
            }

            // Get streaming data
            val streamingData = json.getAsJsonObject("streamingData")
                ?: throw Exception("No streaming data in response")

            // Get adaptive formats (separate audio/video streams)
            val adaptiveFormats = streamingData.getAsJsonArray("adaptiveFormats")
                ?: throw Exception("No adaptive formats available")

            Log.d(TAG, "Found ${adaptiveFormats.size()} adaptive formats")

            // Find best audio stream
            var bestAudio: JsonObject? = null
            var bestBitrate = 0

            for (element in adaptiveFormats) {
                val format = element.asJsonObject
                val mimeType = format.get("mimeType")?.asString ?: continue

                // Only audio formats
                if (!mimeType.startsWith("audio/")) continue

                val bitrate = format.get("bitrate")?.asInt ?: 0
                val url = format.get("url")?.asString

                // Skip formats without direct URL (signature-encrypted)
                if (url == null) {
                    Log.d(TAG, "  Skipping format (no direct URL): $mimeType @ ${bitrate}bps")
                    continue
                }

                Log.d(TAG, "  Audio format: $mimeType @ ${bitrate}bps")

                if (bitrate > bestBitrate) {
                    bestBitrate = bitrate
                    bestAudio = format
                }
            }

            if (bestAudio == null) {
                // If no direct URLs, try formats with signatureCipher
                // These require deciphering which is complex, so we'll throw
                throw Exception("No playable audio streams found (all may require signature decryption)")
            }

            val url = bestAudio.get("url")!!.asString
            val mimeType = bestAudio.get("mimeType")?.asString ?: "audio/mp4"
            val bitrate = bestAudio.get("bitrate")?.asInt ?: 0
            val durationMs = bestAudio.get("approxDurationMs")?.asString?.toLongOrNull() ?: 0L

            Log.d(TAG, "Selected audio: $mimeType @ ${bitrate}bps, duration=${durationMs}ms")
            Log.d(TAG, "URL: ${url.take(100)}...")

            return AudioStreamResult(
                url = url,
                mimeType = mimeType,
                bitrate = bitrate,
                durationMs = durationMs
            )
        } catch (e: Exception) {
            if (e.message?.contains("Video not playable") == true ||
                e.message?.contains("No playable audio") == true ||
                e.message?.contains("No streaming data") == true
            ) {
                throw e
            }
            Log.e(TAG, "Failed to parse player response: ${e.message}")
            throw Exception("Failed to parse stream data: ${e.message}")
        }
    }
}
