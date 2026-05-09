package com.kuro.music.data.mapper

import com.kuro.music.data.local.entity.SongEntity
import com.kuro.music.data.remote.dto.PipedSearchItem
import com.kuro.music.data.remote.dto.PipedStreamResponse
import com.kuro.music.data.remote.dto.PipedTrendingItem
import com.kuro.music.domain.model.Song

/**
 * Extract video ID from a Piped URL like "/watch?v=abc123"
 */
fun extractVideoId(url: String): String {
    return url.substringAfter("v=", "")
        .substringBefore("&")
        .ifEmpty { url.substringAfterLast("/") }
}

fun PipedSearchItem.toSong(): Song {
    val videoId = extractVideoId(url)
    return Song(
        id = videoId,
        title = title,
        artist = uploaderName,
        thumbnailUrl = thumbnail,
        duration = duration * 1000 // convert seconds to ms
    )
}

fun PipedTrendingItem.toSong(): Song {
    val videoId = extractVideoId(url)
    return Song(
        id = videoId,
        title = title,
        artist = uploaderName,
        thumbnailUrl = thumbnail,
        duration = duration * 1000
    )
}

fun PipedStreamResponse.toSong(videoId: String): Song {
    return Song(
        id = videoId,
        title = title,
        artist = uploader,
        thumbnailUrl = thumbnailUrl,
        duration = duration * 1000
    )
}

fun SongEntity.toSong(): Song {
    return Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        thumbnailUrl = thumbnailUrl,
        duration = duration,
        isDownloaded = isDownloaded,
        localPath = localPath
    )
}

fun Song.toEntity(): SongEntity {
    return SongEntity(
        id = id,
        title = title,
        artist = artist,
        album = album,
        thumbnailUrl = thumbnailUrl,
        duration = duration,
        isDownloaded = isDownloaded,
        localPath = localPath
    )
}
