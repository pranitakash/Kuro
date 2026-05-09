package com.kuro.music.data.remote.dto

import com.google.gson.annotations.SerializedName

// Search Response
data class PipedSearchResponse(
    val items: List<PipedSearchItem> = emptyList(),
    val nextpage: String? = null
)

data class PipedSearchItem(
    val url: String = "",
    val type: String = "",         // "stream", "channel", "playlist"
    val title: String = "",
    val thumbnail: String = "",
    val uploaderName: String = "",
    val uploaderUrl: String = "",
    val uploaderAvatar: String = "",
    val duration: Long = 0,        // seconds
    val views: Long = 0,
    val uploaded: Long = 0,
    @SerializedName("uploaderVerified")
    val uploaderVerified: Boolean = false,
    val isShort: Boolean = false
)

// Stream Response
data class PipedStreamResponse(
    val title: String = "",
    val uploader: String = "",
    val uploaderUrl: String = "",
    val uploaderAvatar: String = "",
    val thumbnailUrl: String = "",
    val duration: Long = 0,
    val description: String = "",
    val views: Long = 0,
    val likes: Long = 0,
    val dislikes: Long = 0,
    val audioStreams: List<PipedAudioStream> = emptyList(),
    val relatedStreams: List<PipedSearchItem> = emptyList(),
    val chapters: List<PipedChapter> = emptyList()
)

data class PipedAudioStream(
    val url: String = "",
    val format: String = "",
    val quality: String = "",
    val mimeType: String = "",
    val codec: String = "",
    val bitrate: Int = 0,
    val contentLength: Long = 0
)

data class PipedChapter(
    val title: String = "",
    val image: String = "",
    val start: Long = 0
)

// Trending
data class PipedTrendingItem(
    val url: String = "",
    val type: String = "",
    val title: String = "",
    val thumbnail: String = "",
    val uploaderName: String = "",
    val uploaderUrl: String = "",
    val uploaderAvatar: String = "",
    val duration: Long = 0,
    val views: Long = 0,
    val uploaded: Long = 0
)

// Channel
data class PipedChannelResponse(
    val id: String = "",
    val name: String = "",
    val avatarUrl: String = "",
    val bannerUrl: String = "",
    val description: String = "",
    val subscriberCount: Long = 0,
    val verified: Boolean = false,
    val relatedStreams: List<PipedSearchItem> = emptyList(),
    val nextpage: String? = null
)

// Playlist
data class PipedPlaylistResponse(
    val name: String = "",
    val thumbnailUrl: String = "",
    val uploaderName: String = "",
    val uploaderUrl: String = "",
    val uploaderAvatar: String = "",
    val videos: Int = 0,
    val relatedStreams: List<PipedSearchItem> = emptyList(),
    val nextpage: String? = null
)
