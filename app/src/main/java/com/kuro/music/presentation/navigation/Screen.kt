package com.kuro.music.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    data object Search : Screen("search", "Search", Icons.Filled.Search, Icons.Outlined.Search)
    data object Library : Screen("library", "Library", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)

    // Detail screens
    data object NowPlaying : Screen("now_playing", "Now Playing", Icons.Filled.Home, Icons.Outlined.Home)
    data object ArtistDetail : Screen("artist/{channelId}", "Artist", Icons.Filled.Home, Icons.Outlined.Home)
    data object AlbumDetail : Screen("album/{playlistId}", "Album", Icons.Filled.Home, Icons.Outlined.Home)
    data object PlaylistDetail : Screen("playlist/{playlistId}", "Playlist", Icons.Filled.Home, Icons.Outlined.Home)
    data object LikedSongs : Screen("liked_songs", "Liked Songs", Icons.Filled.Home, Icons.Outlined.Home)
    data object History : Screen("history", "History", Icons.Filled.Home, Icons.Outlined.Home)
    data object Downloads : Screen("downloads", "Downloads", Icons.Filled.Home, Icons.Outlined.Home)
    data object Equalizer : Screen("equalizer", "Equalizer", Icons.Filled.Home, Icons.Outlined.Home)

    companion object {
        val bottomNavItems = listOf(Home, Search, Library, Settings)
    }
}
