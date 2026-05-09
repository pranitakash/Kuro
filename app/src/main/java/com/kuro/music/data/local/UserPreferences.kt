package com.kuro.music.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kuro_settings")

@Singleton
class UserPreferences @Inject constructor(
    private val context: Context
) {
    companion object {
        // Playback
        val STREAMING_QUALITY = stringPreferencesKey("streaming_quality")      // "low", "medium", "high"
        val DOWNLOAD_QUALITY = stringPreferencesKey("download_quality")        // "low", "medium", "high"
        val NORMALIZE_VOLUME = booleanPreferencesKey("normalize_volume")
        val CROSSFADE_DURATION = intPreferencesKey("crossfade_duration")       // 0-12 seconds
        val GAPLESS_PLAYBACK = booleanPreferencesKey("gapless_playback")

        // Interface
        val THEME_MODE = stringPreferencesKey("theme_mode")                    // "system", "light", "dark", "amoled"
        val ACCENT_COLOR = stringPreferencesKey("accent_color")                // hex color or "dynamic"
        val GRID_VIEW = booleanPreferencesKey("grid_view")

        // Source
        val PIPED_INSTANCE = stringPreferencesKey("piped_instance")
        val YT_DLP_FALLBACK = booleanPreferencesKey("yt_dlp_fallback")
        val PROXY_URL = stringPreferencesKey("proxy_url")

        // Storage
        val CACHE_SIZE_MB = intPreferencesKey("cache_size_mb")
        val AUTO_DELETE_DAYS = intPreferencesKey("auto_delete_days")

        // Privacy
        val SAVE_SEARCH_HISTORY = booleanPreferencesKey("save_search_history")
        val SAVE_PLAY_HISTORY = booleanPreferencesKey("save_play_history")
    }

    private val dataStore = context.dataStore

    // ─── Read preferences ───

    val streamingQuality: Flow<String> = dataStore.data.map { prefs ->
        prefs[STREAMING_QUALITY] ?: "medium"
    }

    val downloadQuality: Flow<String> = dataStore.data.map { prefs ->
        prefs[DOWNLOAD_QUALITY] ?: "medium"
    }

    val normalizeVolume: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[NORMALIZE_VOLUME] ?: false
    }

    val crossfadeDuration: Flow<Int> = dataStore.data.map { prefs ->
        prefs[CROSSFADE_DURATION] ?: 0
    }

    val gaplessPlayback: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[GAPLESS_PLAYBACK] ?: true
    }

    val themeMode: Flow<String> = dataStore.data.map { prefs ->
        prefs[THEME_MODE] ?: "system"
    }

    val accentColor: Flow<String> = dataStore.data.map { prefs ->
        prefs[ACCENT_COLOR] ?: "dynamic"
    }

    val gridView: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[GRID_VIEW] ?: false
    }

    val pipedInstance: Flow<String> = dataStore.data.map { prefs ->
        prefs[PIPED_INSTANCE] ?: "https://api.piped.private.coffee"
    }

    val ytDlpFallback: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[YT_DLP_FALLBACK] ?: true
    }

    val saveSearchHistory: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[SAVE_SEARCH_HISTORY] ?: true
    }

    val savePlayHistory: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[SAVE_PLAY_HISTORY] ?: true
    }

    // ─── Write preferences ───

    suspend fun setStreamingQuality(quality: String) {
        dataStore.edit { prefs -> prefs[STREAMING_QUALITY] = quality }
    }

    suspend fun setDownloadQuality(quality: String) {
        dataStore.edit { prefs -> prefs[DOWNLOAD_QUALITY] = quality }
    }

    suspend fun setNormalizeVolume(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[NORMALIZE_VOLUME] = enabled }
    }

    suspend fun setCrossfadeDuration(seconds: Int) {
        dataStore.edit { prefs -> prefs[CROSSFADE_DURATION] = seconds }
    }

    suspend fun setGaplessPlayback(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[GAPLESS_PLAYBACK] = enabled }
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { prefs -> prefs[THEME_MODE] = mode }
    }

    suspend fun setAccentColor(color: String) {
        dataStore.edit { prefs -> prefs[ACCENT_COLOR] = color }
    }

    suspend fun setGridView(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[GRID_VIEW] = enabled }
    }

    suspend fun setPipedInstance(url: String) {
        dataStore.edit { prefs -> prefs[PIPED_INSTANCE] = url }
    }

    suspend fun setYtDlpFallback(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[YT_DLP_FALLBACK] = enabled }
    }

    suspend fun setSaveSearchHistory(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[SAVE_SEARCH_HISTORY] = enabled }
    }

    suspend fun setSavePlayHistory(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[SAVE_PLAY_HISTORY] = enabled }
    }

    suspend fun clearAllCaches() {
        // Clear stream URL cache, etc.
        dataStore.edit { prefs ->
            prefs.remove(CACHE_SIZE_MB)
        }
    }
}
