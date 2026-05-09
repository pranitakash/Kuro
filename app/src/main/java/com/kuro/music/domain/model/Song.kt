package com.kuro.music.domain.model

data class Song(
    val id: String,                 // YouTube video ID
    val title: String,
    val artist: String,
    val album: String? = null,
    val thumbnailUrl: String,
    val duration: Long,             // milliseconds
    val streamUrl: String? = null,  // resolved at playback time
    val isDownloaded: Boolean = false,
    val localPath: String? = null,
    val isExplicit: Boolean = false
)
