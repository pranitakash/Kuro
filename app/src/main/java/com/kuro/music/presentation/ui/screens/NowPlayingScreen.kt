package com.kuro.music.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.kuro.music.data.repository.LyricsRepository
import com.kuro.music.data.repository.LyricsResult
import com.kuro.music.presentation.ui.components.LyricsView
import com.kuro.music.presentation.ui.theme.GothamFontFamily
import com.kuro.music.presentation.ui.theme.KuroError
import com.kuro.music.presentation.ui.theme.KuroNowPlayingBg
import com.kuro.music.presentation.ui.theme.KuroOnBackground
import com.kuro.music.presentation.ui.theme.KuroOnSurfaceVariant
import com.kuro.music.presentation.ui.theme.KuroPrimary
import com.kuro.music.presentation.viewmodel.PlayerState

// Accent color for the seek bar
private val SeekBarAccent = Color(0xFF1A1A1A)

// Bottom tab options inspired by YT Music
private enum class NowPlayingTab { UP_NEXT, LYRICS, RELATED }

@Composable
fun NowPlayingScreen(
    playerState: PlayerState,
    onCollapse: () -> Unit,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleLike: () -> Unit,
    onShowQueue: () -> Unit,
    lyricsRepository: LyricsRepository? = null,
    modifier: Modifier = Modifier
) {
    val song = playerState.currentSong ?: return

    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableStateOf(0f) }
    var selectedTab by remember { mutableStateOf(NowPlayingTab.UP_NEXT) }

    // Lyrics state
    var lyricsResult by remember { mutableStateOf<LyricsResult?>(null) }
    var isLoadingLyrics by remember { mutableStateOf(false) }

    // Fetch lyrics when song changes or lyrics tab is selected
    LaunchedEffect(song.id, selectedTab) {
        if (selectedTab == NowPlayingTab.LYRICS && lyricsRepository != null) {
            isLoadingLyrics = true
            lyricsResult = try {
                lyricsRepository.getLyrics(
                    artist = song.artist,
                    track = song.title,
                    durationSec = if (playerState.duration > 0) (playerState.duration / 1000).toInt() else null
                )
            } catch (e: Exception) {
                LyricsResult.Error(e.message ?: "Failed to load lyrics")
            }
            isLoadingLyrics = false
        }
    }

    // Reset lyrics when song changes
    LaunchedEffect(song.id) {
        lyricsResult = null
    }

    val progress = if (playerState.duration > 0) {
        (playerState.currentPosition.toFloat() / playerState.duration.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KuroNowPlayingBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ─── Top bar: collapse chevron + PLAYING FROM + three-dot ───
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCollapse, modifier = Modifier.size(44.dp)) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = KuroOnBackground,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PLAYING FROM",
                        fontSize = 10.sp,
                        fontFamily = GothamFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = KuroOnSurfaceVariant,
                        letterSpacing = 1.5.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = song.artist,
                        fontSize = 13.sp,
                        fontFamily = GothamFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = KuroOnBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
                IconButton(onClick = { }, modifier = Modifier.size(44.dp)) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = KuroOnBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ─── Album art — fills available vertical space ───
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = song.thumbnailUrl,
                    contentDescription = song.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = Color.Black.copy(alpha = 0.1f),
                            spotColor = Color.Black.copy(alpha = 0.2f)
                        ),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── Song title + like icon ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        fontSize = 22.sp,
                        fontFamily = GothamFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = KuroOnBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 28.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = song.artist,
                        fontSize = 15.sp,
                        fontFamily = GothamFontFamily,
                        color = KuroOnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onToggleLike, modifier = Modifier.size(44.dp)) {
                    Icon(
                        imageVector = if (playerState.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (playerState.isLiked) "Unlike" else "Like",
                        tint = if (playerState.isLiked) KuroError else KuroOnSurfaceVariant,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ─── Interactive seek slider ───
            Slider(
                value = if (isSeeking) seekPosition else progress,
                onValueChange = { value ->
                    isSeeking = true
                    seekPosition = value
                },
                onValueChangeFinished = {
                    onSeekTo((seekPosition * playerState.duration).toLong())
                    isSeeking = false
                },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = SeekBarAccent,
                    activeTrackColor = SeekBarAccent,
                    inactiveTrackColor = KuroOnSurfaceVariant.copy(alpha = 0.2f)
                )
            )

            // ─── Time labels ───
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDuration(
                        if (isSeeking) (seekPosition * playerState.duration).toLong()
                        else playerState.currentPosition
                    ),
                    fontSize = 12.sp,
                    fontFamily = GothamFontFamily,
                    color = KuroOnSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatDuration(playerState.duration),
                    fontSize = 12.sp,
                    fontFamily = GothamFontFamily,
                    color = KuroOnSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ─── Playback Controls ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(onClick = onToggleShuffle, modifier = Modifier.size(52.dp)) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (playerState.shuffleEnabled) KuroPrimary
                        else KuroOnSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Previous
                IconButton(onClick = onSkipPrevious, modifier = Modifier.size(56.dp)) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = KuroOnBackground,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Play/Pause — large black circle
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(KuroPrimary)
                        .clickable { onPlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }

                // Next
                IconButton(onClick = onSkipNext, modifier = Modifier.size(56.dp)) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = KuroOnBackground,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Repeat
                IconButton(onClick = onToggleRepeat, modifier = Modifier.size(52.dp)) {
                    Icon(
                        imageVector = when (playerState.repeatMode) {
                            Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        },
                        contentDescription = "Repeat",
                        tint = if (playerState.repeatMode != Player.REPEAT_MODE_OFF) KuroPrimary
                        else KuroOnSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─── YT Music-style bottom tab bar ───
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NowPlayingTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    Column(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (tab == NowPlayingTab.UP_NEXT) {
                                    onShowQueue()
                                } else {
                                    selectedTab = tab
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when (tab) {
                                NowPlayingTab.UP_NEXT -> "UP NEXT"
                                NowPlayingTab.LYRICS -> "LYRICS"
                                NowPlayingTab.RELATED -> "RELATED"
                            },
                            fontSize = 12.sp,
                            fontFamily = GothamFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) KuroOnBackground else KuroOnSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(2.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(KuroPrimary)
                            )
                        }
                    }
                }
            }
        }

        // ─── Lyrics overlay (covers album art area when LYRICS tab selected) ───
        if (selectedTab == NowPlayingTab.LYRICS) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(KuroNowPlayingBg.copy(alpha = 0.97f))
                    .statusBarsPadding()
                    .padding(top = 56.dp, bottom = 56.dp)
            ) {
                if (isLoadingLyrics) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = KuroPrimary,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Loading lyrics...",
                                fontSize = 14.sp,
                                fontFamily = GothamFontFamily,
                                color = KuroOnSurfaceVariant
                            )
                        }
                    }
                } else {
                    LyricsView(
                        lyricsResult = lyricsResult,
                        currentPosition = playerState.currentPosition,
                        isLoading = false,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Close lyrics header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = song.title,
                        fontSize = 16.sp,
                        fontFamily = GothamFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = KuroOnBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { selectedTab = NowPlayingTab.UP_NEXT },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Close lyrics",
                            tint = KuroOnBackground,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

fun formatDuration(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
