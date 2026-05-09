package com.kuro.music.presentation.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kuro.music.domain.model.Song
import com.kuro.music.presentation.ui.components.AddToPlaylistSheet
import com.kuro.music.presentation.ui.components.SongListItem
import com.kuro.music.presentation.ui.theme.GothamFontFamily
import com.kuro.music.presentation.ui.theme.KuroOnBackground
import com.kuro.music.presentation.ui.theme.KuroOnSurfaceVariant
import com.kuro.music.presentation.ui.theme.KuroPrimary
import com.kuro.music.presentation.ui.theme.KuroSurfaceVariant
import com.kuro.music.presentation.viewmodel.DownloadsViewModel
import com.kuro.music.presentation.viewmodel.HomeViewModel
import com.kuro.music.presentation.viewmodel.LibraryViewModel
import com.kuro.music.presentation.viewmodel.PlayerViewModel

@Composable
fun HomeScreen(
    playerViewModel: PlayerViewModel,
    homeViewModel: HomeViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    downloadsViewModel: DownloadsViewModel = hiltViewModel()
) {
    val homeState by homeViewModel.homeState.collectAsState()
    val playerState by playerViewModel.playerState.collectAsState()
    val libraryState by libraryViewModel.state.collectAsState()
    val context = LocalContext.current

    var songForPlaylist by remember { mutableStateOf<Song?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // ─── Greeting Header with bell + profile avatar ───
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Good ${getGreeting()}",
                    fontSize = 28.sp,
                    fontFamily = GothamFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = KuroOnBackground
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Notification bell
                    IconButton(
                        onClick = { },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = KuroOnBackground,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    // Profile avatar
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(KuroSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = KuroOnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // ─── Search Bar ───
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(KuroSurfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = KuroOnSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Search",
                        fontSize = 15.sp,
                        fontFamily = GothamFontFamily,
                        color = KuroOnSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // ─── New music for you (featured cards) ───
        if (homeState.recentlyPlayed.isNotEmpty()) {
            item {
                SectionHeaderWithArrow("New music for you")
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(homeState.recentlyPlayed) { song ->
                        FeaturedCard(
                            song = song,
                            isPlaying = playerState.currentSong?.id == song.id,
                            onClick = { playerViewModel.playSong(song) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        // ─── Today's biggest hits (smaller cards) ───
        if (homeState.quickPicks.isNotEmpty()) {
            item {
                SectionHeaderWithArrow("Today's biggest hits")
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(homeState.quickPicks) { song ->
                        CompactSongCard(
                            song = song,
                            isPlaying = playerState.currentSong?.id == song.id,
                            onClick = { playerViewModel.playSong(song) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        // ─── Suggested artists (circular avatars) ───
        if (homeState.trending.isNotEmpty()) {
            item {
                SectionHeaderWithArrow("Suggested artists")
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(homeState.trending.take(10)) { song ->
                        ArtistCircle(
                            song = song,
                            onClick = { playerViewModel.playSong(song) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        // ─── Loading ───
        if (homeState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = KuroPrimary,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.5.dp
                    )
                }
            }
        }

        // ─── Trending Music (list view) ───
        if (homeState.trending.isNotEmpty()) {
            item {
                SectionHeaderWithArrow(
                    if (homeState.selectedMood != null) "${homeState.selectedMood} Vibes"
                    else "Trending now"
                )
            }
            items(homeState.trending) { song ->
                SongListItem(
                    song = song,
                    isPlaying = playerState.currentSong?.id == song.id,
                    onClick = {
                        playerViewModel.playSong(song)
                    },
                    onAddToQueue = { playerViewModel.addToQueue(it) },
                    onDownload = {
                        downloadsViewModel.downloadSong(it)
                        Toast.makeText(context, "Downloading ${it.title}", Toast.LENGTH_SHORT).show()
                    },
                    onAddToPlaylist = { songForPlaylist = it }
                )
            }
        }

        // ─── Error ───
        if (homeState.error != null && !homeState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Couldn't load music",
                            fontSize = 16.sp,
                            fontFamily = GothamFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = KuroOnBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = homeState.error ?: "",
                            fontSize = 13.sp,
                            fontFamily = GothamFontFamily,
                            color = KuroOnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(KuroPrimary)
                                .clickable { homeViewModel.loadHome() }
                                .padding(horizontal = 24.dp, vertical = 10.dp)
                        ) {
                            Text(
                                "Retry",
                                color = Color.White,
                                fontFamily = GothamFontFamily,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // ─── Empty state ───
        if (homeState.trending.isEmpty() && homeState.quickPicks.isEmpty() &&
            !homeState.isLoading && homeState.error == null
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Discovering music...",
                            fontSize = 16.sp,
                            fontFamily = GothamFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = KuroOnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        CircularProgressIndicator(
                            color = KuroPrimary,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    }

    // ─── Add to Playlist Sheet ───
    songForPlaylist?.let { song ->
        AddToPlaylistSheet(
            song = song,
            playlists = libraryState.playlists,
            onDismiss = { songForPlaylist = null },
            onAddToPlaylist = { playlistId, s ->
                libraryViewModel.addSongToPlaylist(playlistId, s)
                Toast.makeText(context, "Added to playlist", Toast.LENGTH_SHORT).show()
                songForPlaylist = null
            },
            onCreatePlaylist = { name ->
                libraryViewModel.createPlaylist(name)
            }
        )
    }
}

@Composable
private fun SectionHeaderWithArrow(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontFamily = GothamFontFamily,
            fontWeight = FontWeight.Bold,
            color = KuroOnBackground
        )
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = "See more",
            tint = KuroOnSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun FeaturedCard(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
    ) {
        // Album art with overlay title
        Box {
            AsyncImage(
                model = song.thumbnailUrl,
                contentDescription = song.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop
            )

            // Gradient overlay at bottom for text readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // Title + song count at bottom-left
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Text(
                    text = song.title,
                    fontSize = 15.sp,
                    fontFamily = GothamFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    fontSize = 12.sp,
                    fontFamily = GothamFontFamily,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Play button at bottom-right
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = KuroPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun CompactSongCard(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
    ) {
        Box {
            AsyncImage(
                model = song.thumbnailUrl,
                contentDescription = song.title,
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Playing",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = song.title,
            fontSize = 13.sp,
            fontFamily = GothamFontFamily,
            fontWeight = FontWeight.Medium,
            color = KuroOnBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist,
            fontSize = 12.sp,
            fontFamily = GothamFontFamily,
            color = KuroOnSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ArtistCircle(
    song: Song,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = song.thumbnailUrl,
            contentDescription = song.artist,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = song.artist,
            fontSize = 11.sp,
            fontFamily = GothamFontFamily,
            fontWeight = FontWeight.Medium,
            color = KuroOnBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun getGreeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "morning"
        hour < 17 -> "afternoon"
        else -> "evening"
    }
}
