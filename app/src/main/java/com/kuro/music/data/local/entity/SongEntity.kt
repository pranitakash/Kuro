package com.kuro.music.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val artist: String,
    val album: String? = null,
    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUrl: String,
    val duration: Long,
    @ColumnInfo(name = "is_downloaded")
    val isDownloaded: Boolean = false,
    @ColumnInfo(name = "local_path")
    val localPath: String? = null,
    @ColumnInfo(name = "download_quality")
    val downloadQuality: String? = null,
    @ColumnInfo(name = "cached_stream_url")
    val cachedStreamUrl: String? = null,
    @ColumnInfo(name = "cache_expiry")
    val cacheExpiry: Long? = null
)
