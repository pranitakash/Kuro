package com.kuro.music.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LrcLibResponse(
    val id: Int = 0,
    val name: String = "",
    @SerializedName("track_name")
    val trackName: String = "",
    @SerializedName("artist_name")
    val artistName: String = "",
    @SerializedName("album_name")
    val albumName: String = "",
    val duration: Double = 0.0,
    val instrumental: Boolean = false,
    @SerializedName("plainLyrics")
    val plainLyrics: String? = null,
    @SerializedName("syncedLyrics")
    val syncedLyrics: String? = null
)

/**
 * Represents a single synced lyrics line parsed from LRC format.
 * Example LRC line: [00:12.34] Hello world
 */
data class LyricsLine(
    val timestamp: Long, // milliseconds
    val text: String
)
