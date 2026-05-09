package com.kuro.music.presentation.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kuro.music.domain.model.Song
import com.kuro.music.presentation.ui.components.AddToPlaylistSheet
import com.kuro.music.presentation.ui.components.SongListItem
import com.kuro.music.presentation.ui.theme.KuroError
import com.kuro.music.presentation.ui.theme.KuroOnBackground
import com.kuro.music.presentation.ui.theme.KuroOnSurfaceVariant
import com.kuro.music.presentation.viewmodel.DownloadsViewModel
import com.kuro.music.presentation.viewmodel.LibraryViewModel
import com.kuro.music.presentation.viewmodel.PlayerViewModel
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    recentlyPlayed: StateFlow<List<Song>>,
    playerViewModel: PlayerViewModel,
    onClearHistory: () -> Unit,
    onBack: () -> Unit,
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    downloadsViewModel: DownloadsViewModel = hiltViewModel()
) {
    val songs by recentlyPlayed.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }
    val libraryState by libraryViewModel.state.collectAsState()
    val context = LocalContext.current
    var songForPlaylist by remember { mutableStateOf<Song?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = KuroOnBackground
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = KuroOnBackground
                    )
                }
            },
            actions = {
                if (songs.isNotEmpty()) {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(
                            Icons.Filled.DeleteSweep,
                            contentDescription = "Clear History",
                            tint = KuroOnSurfaceVariant
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        if (songs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        tint = KuroOnSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No history yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = KuroOnSurfaceVariant
                    )
                    Text(
                        text = "Songs you play will appear here",
                        fontSize = 13.sp,
                        color = KuroOnSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(songs) { song ->
                    SongListItem(
                        song = song,
                        onClick = { playerViewModel.playSong(song) },
                        onAddToQueue = { playerViewModel.addToQueue(it) },
                        onDownload = {
                            downloadsViewModel.downloadSong(it)
                            Toast.makeText(context, "Downloading ${it.title}", Toast.LENGTH_SHORT).show()
                        },
                        onAddToPlaylist = { songForPlaylist = it }
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear History?", fontWeight = FontWeight.Bold) },
            text = { Text("This will remove all your playback history.") },
            confirmButton = {
                TextButton(onClick = {
                    onClearHistory()
                    showClearDialog = false
                }) {
                    Text("Clear", color = KuroError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            },
            containerColor = Color.White
        )
    }

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
