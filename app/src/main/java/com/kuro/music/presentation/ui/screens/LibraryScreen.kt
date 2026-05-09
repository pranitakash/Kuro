package com.kuro.music.presentation.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kuro.music.presentation.ui.theme.GothamFontFamily
import com.kuro.music.presentation.ui.theme.KuroChipSelected
import com.kuro.music.presentation.ui.theme.KuroChipSelectedText
import com.kuro.music.presentation.ui.theme.KuroChipUnselected
import com.kuro.music.presentation.ui.theme.KuroChipUnselectedText
import com.kuro.music.presentation.ui.theme.KuroOnBackground
import com.kuro.music.presentation.ui.theme.KuroOnSurfaceVariant
import com.kuro.music.presentation.ui.theme.KuroPrimary
import com.kuro.music.presentation.ui.theme.KuroSurfaceVariant
import com.kuro.music.presentation.viewmodel.LibraryViewModel

private val libraryTabs = listOf("Playlists", "Albums", "Artists", "Podcasts")

@Composable
fun LibraryScreen(
    onNavigateToLikedSongs: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToDownloads: () -> Unit = {},
    onNavigateToPlaylist: (String) -> Unit = {},
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf("Playlists") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // ─── Header: "Library" + gear icon ───
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Library",
                fontSize = 32.sp,
                fontFamily = GothamFontFamily,
                fontWeight = FontWeight.Bold,
                color = KuroOnBackground
            )
            IconButton(onClick = { }, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = KuroOnBackground,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // ─── Tab chips row ───
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            libraryTabs.forEach { tab ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) KuroChipSelected else KuroChipUnselected)
                        .clickable { selectedTab = tab }
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = tab,
                        fontSize = 13.sp,
                        fontFamily = GothamFontFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) KuroChipSelectedText else KuroChipUnselectedText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ─── Library content ───
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // ─── Create playlist row ───
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.showCreateDialog() }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(KuroSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Create",
                            tint = KuroOnSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Create playlist",
                        fontSize = 16.sp,
                        fontFamily = GothamFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = KuroOnBackground
                    )
                }
            }

            // ─── Liked Songs ───
            item {
                LibraryListItem(
                    icon = Icons.Filled.Favorite,
                    title = "Liked Songs",
                    subtitle = "${state.likedSongCount} songs",
                    onClick = onNavigateToLikedSongs
                )
            }

            // ─── Downloads ───
            item {
                LibraryListItem(
                    icon = Icons.Filled.Download,
                    title = "Downloaded Songs",
                    subtitle = "${state.downloadCount} songs",
                    onClick = onNavigateToDownloads
                )
            }

            // ─── Playlists ───
            if (state.playlists.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.MusicNote,
                                contentDescription = null,
                                tint = KuroOnSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No playlists yet",
                                fontSize = 15.sp,
                                fontFamily = GothamFontFamily,
                                color = KuroOnSurfaceVariant
                            )
                            Text(
                                text = "Create one to organize your music",
                                fontSize = 13.sp,
                                fontFamily = GothamFontFamily,
                                color = KuroOnSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            items(state.playlists) { playlist ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToPlaylist(playlist.id) }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Playlist icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(KuroSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlaylistPlay,
                            contentDescription = null,
                            tint = KuroOnSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = playlist.name,
                            fontSize = 15.sp,
                            fontFamily = GothamFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = KuroOnBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Playlist",
                            fontSize = 12.sp,
                            fontFamily = GothamFontFamily,
                            color = KuroOnSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { viewModel.deletePlaylist(playlist.id) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "More",
                            tint = KuroOnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // Create playlist dialog
    if (state.showCreatePlaylistDialog) {
        var playlistName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.hideCreateDialog() },
            title = {
                Text(
                    "Create Playlist",
                    fontFamily = GothamFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    label = { Text("Playlist name", fontFamily = GothamFontFamily) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { if (playlistName.isNotBlank()) viewModel.createPlaylist(playlistName) },
                    enabled = playlistName.isNotBlank()
                ) {
                    Text("Create", fontFamily = GothamFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideCreateDialog() }) {
                    Text("Cancel", fontFamily = GothamFontFamily)
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun LibraryListItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rounded square icon container
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(KuroSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = KuroOnBackground,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontFamily = GothamFontFamily,
                fontWeight = FontWeight.Medium,
                color = KuroOnBackground
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                fontFamily = GothamFontFamily,
                color = KuroOnSurfaceVariant
            )
        }
        IconButton(
            onClick = { },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = "More",
                tint = KuroOnSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
