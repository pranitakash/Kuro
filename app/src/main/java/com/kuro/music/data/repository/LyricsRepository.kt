package com.kuro.music.data.repository

import android.util.Log
import com.kuro.music.data.remote.LrcLibService
import com.kuro.music.data.remote.dto.LrcLibResponse
import com.kuro.music.data.remote.dto.LyricsLine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsRepository @Inject constructor(
    private val lrcLibService: LrcLibService
) {
    companion object {
        private const val TAG = "LyricsRepository"
    }

    // In-memory cache
    private val cache = mutableMapOf<String, LyricsResult>()

    suspend fun getLyrics(artist: String, track: String, durationSec: Int? = null): LyricsResult {
        val cacheKey = "$artist|$track"
        cache[cacheKey]?.let { return it }

        return try {
            // Try exact match first
            val response = lrcLibService.getLyrics(
                artistName = artist,
                trackName = track,
                duration = durationSec
            )
            val result = parseLyricsResponse(response)
            cache[cacheKey] = result
            result
        } catch (e: Exception) {
            Log.d(TAG, "Exact match failed, trying search: ${e.message}")
            try {
                // Fallback to search
                val searchResults = lrcLibService.searchLyrics("$artist $track")
                val bestMatch = searchResults.firstOrNull()
                if (bestMatch != null) {
                    val result = parseLyricsResponse(bestMatch)
                    cache[cacheKey] = result
                    result
                } else {
                    LyricsResult.NotFound
                }
            } catch (e2: Exception) {
                Log.e(TAG, "Lyrics search failed: ${e2.message}")
                LyricsResult.Error(e2.message ?: "Failed to fetch lyrics")
            }
        }
    }

    private fun parseLyricsResponse(response: LrcLibResponse): LyricsResult {
        if (response.instrumental) {
            return LyricsResult.Instrumental
        }

        val syncedLyrics = response.syncedLyrics
        if (!syncedLyrics.isNullOrBlank()) {
            val lines = parseLrcFormat(syncedLyrics)
            if (lines.isNotEmpty()) {
                return LyricsResult.Synced(lines, response.plainLyrics ?: "")
            }
        }

        val plainLyrics = response.plainLyrics
        if (!plainLyrics.isNullOrBlank()) {
            return LyricsResult.Plain(plainLyrics)
        }

        return LyricsResult.NotFound
    }

    /**
     * Parse LRC format lyrics.
     * Format: [mm:ss.xx] Lyrics text
     */
    private fun parseLrcFormat(lrc: String): List<LyricsLine> {
        val regex = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})](.*)""")
        return lrc.lines()
            .mapNotNull { line ->
                regex.matchEntire(line.trim())?.let { match ->
                    val min = match.groupValues[1].toLongOrNull() ?: 0
                    val sec = match.groupValues[2].toLongOrNull() ?: 0
                    val ms = match.groupValues[3].let { msStr ->
                        val value = msStr.toLongOrNull() ?: 0
                        if (msStr.length == 2) value * 10 else value
                    }
                    val timestamp = min * 60_000 + sec * 1000 + ms
                    val text = match.groupValues[4].trim()
                    LyricsLine(timestamp, text)
                }
            }
            .sortedBy { it.timestamp }
    }
}

sealed class LyricsResult {
    data class Synced(val lines: List<LyricsLine>, val plainText: String) : LyricsResult()
    data class Plain(val text: String) : LyricsResult()
    data object Instrumental : LyricsResult()
    data object NotFound : LyricsResult()
    data class Error(val message: String) : LyricsResult()
}
