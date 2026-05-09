package com.kuro.music.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages multiple Piped API instances with automatic failover.
 * If the current instance fails, it automatically tries the next one.
 */
@Singleton
class PipedInstanceManager @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val mutex = Mutex()
    private var currentIndex = 0
    private var lastHealthCheck = 0L
    private var healthyInstance: String? = null

    companion object {
        private const val TAG = "PipedInstanceManager"
        private const val HEALTH_CHECK_INTERVAL_MS = 5 * 60 * 1000L // 5 minutes

        // Ordered by reliability — tested instances first
        val INSTANCES = listOf(
            "https://api.piped.private.coffee",
            "https://pipedapi.kavin.rocks",
            "https://pipedapi-libre.kavin.rocks",
            "https://pipedapi.leptons.xyz",
            "https://pipedapi.adminforge.de",
            "https://piped-api.privacy.com.de",
            "https://pipedapi.reallyaweso.me",
            "https://pipedapi.nosebs.ru",
            "https://api.piped.yt",
            "https://pipedapi.drgns.space",
            "https://pipedapi.owo.si",
            "https://pipedapi.ducks.party",
            "https://piped-api.codespace.cz",
            "https://pipedapi.orangenet.cc",
            "https://pipedapi.darkness.services"
        )
    }

    /**
     * Returns the current best working instance base URL.
     * Will probe instances if none has been validated recently.
     */
    suspend fun getCurrentInstance(): String = mutex.withLock {
        // Return cached healthy instance if still within check interval
        val now = System.currentTimeMillis()
        healthyInstance?.let { instance ->
            if (now - lastHealthCheck < HEALTH_CHECK_INTERVAL_MS) {
                return instance
            }
        }

        // Probe instances to find a working one
        val working = findWorkingInstance()
        if (working != null) {
            healthyInstance = working
            lastHealthCheck = now
            Log.d(TAG, "Using Piped instance: $working")
            return working
        }

        // Fall back to default if all probes fail
        val fallback = INSTANCES[0]
        Log.w(TAG, "All Piped instances failed health check, using fallback: $fallback")
        healthyInstance = fallback
        lastHealthCheck = now
        return fallback
    }

    /**
     * Mark the current instance as failed and try the next one.
     */
    suspend fun reportFailure(failedInstance: String) = mutex.withLock {
        if (healthyInstance == failedInstance) {
            Log.w(TAG, "Instance reported failed: $failedInstance")
            healthyInstance = null
            lastHealthCheck = 0
        }
    }

    private suspend fun findWorkingInstance(): String? = withContext(Dispatchers.IO) {
        for (instance in INSTANCES) {
            try {
                val request = Request.Builder()
                    .url("$instance/trending?region=US")
                    .head()
                    .build()

                val response = okHttpClient.newBuilder()
                    .callTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                    .newCall(request)
                    .execute()

                if (response.isSuccessful) {
                    response.close()
                    Log.d(TAG, "Health check passed: $instance")
                    return@withContext instance
                }
                response.close()
                Log.d(TAG, "Health check failed (${response.code}): $instance")
            } catch (e: Exception) {
                Log.d(TAG, "Health check error for $instance: ${e.message}")
            }
        }
        null
    }
}
