package com.kuro.music.domain.model

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val year: Int? = null,
    val thumbnailUrl: String,
    val songs: List<Song> = emptyList()
)
