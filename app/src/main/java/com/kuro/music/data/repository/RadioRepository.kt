package com.kuro.music.data.repository

import android.util.Log
import com.kuro.music.data.mapper.toSong
import com.kuro.music.data.remote.PipedApiService
import com.kuro.music.data.remote.PipedInstanceManager
import com.kuro.music.domain.model.Song
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RadioRepository @Inject constructor(
    private val pipedApiService: PipedApiService,
    private val pipedInstanceManager: PipedInstanceManager
) {
    companion object {
        private const val TAG = "RadioRepository"
    }

    /**
     * Get related songs for a given video ID (song radio).
     * Uses Piped's related streams endpoint.
     */
    suspend fun getRelatedSongs(videoId: String): List<Song> {
        return try {
            val response = pipedApiService.getStreams(videoId)
            response.relatedStreams.mapNotNull { item ->
                try {
                    item.toSong()
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get related songs: ${e.message}")
            emptyList()
        }
    }

    /**
     * Get songs by mood/genre query (mood radio).
     */
    suspend fun getMoodRadio(mood: String): List<Song> {
        return try {
            val results = pipedApiService.search(
                query = "$mood music",
                filter = "music_songs"
            )
            results.items?.mapNotNull { item ->
                try {
                    item.toSong()
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get mood radio: ${e.message}")
            emptyList()
        }
    }
}
