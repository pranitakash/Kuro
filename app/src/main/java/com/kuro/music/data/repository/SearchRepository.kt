package com.kuro.music.data.repository

import android.util.Log
import com.kuro.music.data.mapper.toSong
import com.kuro.music.data.remote.PipedApiService
import com.kuro.music.data.remote.PipedInstanceManager
import com.kuro.music.domain.model.Song
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val pipedApi: PipedApiService,
    private val instanceManager: PipedInstanceManager,
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val TAG = "SearchRepository"
    }

    /**
     * Creates a PipedApiService pointing to a specific base URL.
     */
    private fun createApiForInstance(baseUrl: String): PipedApiService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PipedApiService::class.java)
    }

    /**
     * Executes an API call with automatic instance failover.
     */
    private suspend fun <T> withFallback(call: suspend (PipedApiService) -> T): T {
        // Try with the currently healthy instance first
        val instance = instanceManager.getCurrentInstance()
        val api = createApiForInstance(instance)
        return try {
            call(api)
        } catch (e: Exception) {
            Log.w(TAG, "Primary instance failed ($instance): ${e.message}")
            instanceManager.reportFailure(instance)

            // Try all remaining instances
            for (fallbackUrl in PipedInstanceManager.INSTANCES) {
                if (fallbackUrl == instance) continue
                try {
                    val fallbackApi = createApiForInstance(fallbackUrl)
                    val result = call(fallbackApi)
                    Log.d(TAG, "Fallback succeeded: $fallbackUrl")
                    return result
                } catch (ex: Exception) {
                    Log.d(TAG, "Fallback failed ($fallbackUrl): ${ex.message}")
                }
            }
            throw Exception("All Piped instances failed. Last error: ${e.message}")
        }
    }

    suspend fun search(query: String, filter: String = "videos"): List<Song> {
        return try {
            val response = withFallback { api -> api.search(query, filter) }
            response.items
                .filter { it.type == "stream" && !it.isShort && it.duration > 30 }
                .map { it.toSong() }
        } catch (e: Exception) {
            Log.e(TAG, "Search failed: ${e.message}")
            emptyList()
        }
    }

    suspend fun getSuggestions(query: String): List<String> {
        return try {
            withFallback { api -> api.getSuggestions(query) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTrending(region: String = "US"): List<Song> {
        return try {
            val items = withFallback { api -> api.getTrending(region) }
            items.filter { it.type == "stream" }
                .map { it.toSong() }
        } catch (e: Exception) {
            Log.e(TAG, "Trending failed: ${e.message}")
            emptyList()
        }
    }

    /**
     * Gets music-focused content by searching for popular music terms.
     * Used when trending doesn't return music content.
     */
    suspend fun getMusicRecommendations(): List<Song> {
        val queries = listOf(
            "trending music hits 2025 official audio",
            "new music official audio",
            "popular songs official audio",
            "bollywood hits songs official audio",
            "top songs this week official audio"
        )
        return try {
            val query = queries.random()
            search(query, "videos")
        } catch (e: Exception) {
            emptyList()
        }
    }
}
