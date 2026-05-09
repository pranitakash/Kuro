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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kuro.music.domain.model.Song
import com.kuro.music.presentation.ui.components.AddToPlaylistSheet
import com.kuro.music.presentation.ui.components.SongListItem
import com.kuro.music.presentation.ui.theme.KuroChipSelected
import com.kuro.music.presentation.ui.theme.KuroChipSelectedText
import com.kuro.music.presentation.ui.theme.KuroChipUnselected
import com.kuro.music.presentation.ui.theme.KuroChipUnselectedText
import com.kuro.music.presentation.ui.theme.KuroOnBackground
import com.kuro.music.presentation.ui.theme.KuroOnSurfaceVariant
import com.kuro.music.presentation.ui.theme.KuroPrimary
import com.kuro.music.presentation.ui.theme.KuroSurfaceVariant
import com.kuro.music.presentation.viewmodel.DownloadsViewModel
import com.kuro.music.presentation.viewmodel.LibraryViewModel
import com.kuro.music.presentation.viewmodel.PlayerViewModel
import com.kuro.music.presentation.viewmodel.SearchFilter
import com.kuro.music.presentation.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    playerViewModel: PlayerViewModel,
    searchViewModel: SearchViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    downloadsViewModel: DownloadsViewModel = hiltViewModel()
) {
    val searchState by searchViewModel.searchState.collectAsState()
    val playerState by playerViewModel.playerState.collectAsState()
    val libraryState by libraryViewModel.state.collectAsState()
    val context = LocalContext.current

    var songForPlaylist by remember { mutableStateOf<Song?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // ─── Search Header ───
        Text(
            text = "Search",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = KuroOnBackground,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )

        // ─── Search Bar ───
        TextField(
            value = searchState.query,
            onValueChange = { searchViewModel.onQueryChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(14.dp)),
            placeholder = {
                Text(
                    "Songs, artists, albums...",
                    color = KuroOnSurfaceVariant,
                    fontSize = 15.sp
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = KuroOnSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchState.query.isNotEmpty()) {
                    IconButton(onClick = { searchViewModel.clearSearch() }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = KuroOnSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = KuroSurfaceVariant,
                unfocusedContainerColor = KuroSurfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = KuroPrimary
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { searchViewModel.search() }
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ─── Filter Chips ───
        if (searchState.query.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchFilter.entries.forEach { filter ->
                    val isSelected = searchState.selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) KuroChipSelected else KuroChipUnselected)
                            .clickable { searchViewModel.onFilterSelected(filter) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter.label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) KuroChipSelectedText else KuroChipUnselectedText
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ─── Content ───
        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Suggestions
            if (searchState.suggestions.isNotEmpty() && searchState.results.isEmpty()) {
                items(searchState.suggestions) { suggestion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                searchViewModel.onQueryChanged(suggestion)
                                searchViewModel.search(suggestion)
                            }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = KuroOnSurfaceVariant,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 0.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = suggestion,
                            fontSize = 15.sp,
                            color = KuroOnBackground
                        )
                    }
                }
            }

            // Loading
            if (searchState.isSearching) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = KuroPrimary,
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 2.5.dp
                        )
                    }
                }
            }

            // Results
            items(searchState.results) { song ->
                SongListItem(
                    song = song,
                    isPlaying = playerState.currentSong?.id == song.id,
                    onClick = { playerViewModel.playSong(song) },
                    onAddToQueue = { playerViewModel.addToQueue(it) },
                    onDownload = {
                        downloadsViewModel.downloadSong(it)
                        Toast.makeText(context, "Downloading ${it.title}", Toast.LENGTH_SHORT).show()
                    },
                    onAddToPlaylist = { songForPlaylist = it }
                )
            }

            // Empty state
            if (!searchState.isSearching && searchState.results.isEmpty() &&
                searchState.query.isNotBlank() && searchState.suggestions.isEmpty()
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
                                text = "No results found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = KuroOnBackground
                            )
                            Text(
                                text = "Try different keywords",
                                fontSize = 13.sp,
                                color = KuroOnSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // ─── Initial state: search history ───
            if (searchState.query.isBlank() && searchState.results.isEmpty()) {
                if (searchState.searchHistory.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Searches",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = KuroOnBackground
                            )
                            TextButton(onClick = { searchViewModel.clearSearchHistory() }) {
                                Text(
                                    "Clear all",
                                    fontSize = 13.sp,
                                    color = KuroOnSurfaceVariant
                                )
                            }
                        }
                    }

                    items(searchState.searchHistory) { historyItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    searchViewModel.onQueryChanged(historyItem)
                                    searchViewModel.search(historyItem)
                                }
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = KuroOnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = historyItem,
                                fontSize = 15.sp,
                                color = KuroOnBackground,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { searchViewModel.removeFromHistory(historyItem) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = KuroOnSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = KuroOnSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(bottom = 12.dp)
                                )
                                Text(
                                    text = "Search for music",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = KuroOnSurfaceVariant
                                )
                                Text(
                                    text = "Find songs, artists, albums and playlists",
                                    fontSize = 13.sp,
                                    color = KuroOnSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
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
