package com.kuro.music.domain.model

data class Playlist(
    val id: String,
    val name: String,
    val songs: List<Song> = emptyList(),
    val thumbnailUrl: String? = null,
    val isLocal: Boolean = true,
    val source: PlaylistSource = PlaylistSource.LOCAL
)

enum class PlaylistSource {
    LOCAL, PIPED, YT
}
