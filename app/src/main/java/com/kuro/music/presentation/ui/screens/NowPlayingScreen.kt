package com.kuro.music.presentation.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.kuro.music.presentation.ui.theme.KuroError
import com.kuro.music.presentation.ui.theme.KuroNowPlayingBg
import com.kuro.music.presentation.ui.theme.KuroOnBackground
import com.kuro.music.presentation.ui.theme.KuroOnSurfaceVariant
import com.kuro.music.presentation.ui.theme.KuroPrimary
import com.kuro.music.presentation.viewmodel.PlayerState
import kotlin.math.sin

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
    modifier: Modifier = Modifier
) {
    val song = playerState.currentSong ?: return

    // Vinyl rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_angle"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KuroNowPlayingBg)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ─── Top bar: collapse chevron + artist name + queue ───
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCollapse, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = KuroOnBackground,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = song.artist,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = KuroOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = onShowQueue, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Default.QueueMusic,
                        contentDescription = "Queue",
                        tint = KuroOnBackground,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ─── Vinyl Disc with grooves ───
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .aspectRatio(1f)
                    .shadow(
                        elevation = 24.dp,
                        shape = CircleShape,
                        ambientColor = Color.Black.copy(alpha = 0.15f),
                        spotColor = Color.Black.copy(alpha = 0.25f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Rotating disc container
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(if (playerState.isPlaying) rotation else 0f)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Album art fills the disc
                    AsyncImage(
                        model = song.thumbnailUrl,
                        contentDescription = song.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    // Subtle dark overlay for vinyl look
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.12f))
                    )

                    // Vinyl grooves — concentric rings
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val maxRadius = size.minDimension / 2f
                        val centerHoleRadius = maxRadius * 0.11f
                        val grooveColor = Color.Black.copy(alpha = 0.08f)

                        // Draw many concentric groove rings
                        val ringCount = 25
                        val startR = centerHoleRadius + maxRadius * 0.08f
                        val endR = maxRadius - maxRadius * 0.04f
                        val step = (endR - startR) / ringCount

                        for (i in 0..ringCount) {
                            val r = startR + step * i
                            drawCircle(
                                color = grooveColor,
                                radius = r,
                                center = center,
                                style = Stroke(width = 0.6f)
                            )
                        }

                        // A few more prominent grooves
                        for (i in listOf(5, 12, 18, 23)) {
                            val r = startR + step * i
                            drawCircle(
                                color = Color.Black.copy(alpha = 0.12f),
                                radius = r,
                                center = center,
                                style = Stroke(width = 1.2f)
                            )
                        }
                    }

                    // Center hole (spindle)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(KuroNowPlayingBg)
                    )

                    // Inner ring around center hole
                    Canvas(modifier = Modifier.size(36.dp)) {
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.15f),
                            radius = size.minDimension / 2f,
                            center = Offset(size.width / 2f, size.height / 2f),
                            style = Stroke(width = 1.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ─── Song title ───
            Text(
                text = song.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = KuroOnBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ─── Artist ───
            Text(
                text = song.artist,
                fontSize = 14.sp,
                color = KuroOnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ─── Waveform-style seek bar ───
            var isSeeking by remember { mutableStateOf(false) }
            var seekPosition by remember { mutableStateOf(0f) }

            val progress = if (isSeeking) {
                seekPosition
            } else if (playerState.duration > 0) {
                (playerState.currentPosition.toFloat() / playerState.duration.toFloat()).coerceIn(0f, 1f)
            } else 0f

            // Waveform visualization
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { }
            ) {
                val barCount = 60
                val barWidth = size.width / (barCount * 2f)
                val maxBarHeight = size.height * 0.85f

                for (i in 0 until barCount) {
                    val x = (i * 2f + 0.5f) * barWidth + barWidth / 2f
                    // Generate pseudo-random waveform heights using sin
                    val heightFactor = (sin(i * 0.8f + 2.5f) * 0.4f + 0.5f +
                            sin(i * 1.7f + 1.2f) * 0.2f +
                            sin(i * 3.1f) * 0.15f).coerceIn(0.15f, 1f)
                    val barHeight = maxBarHeight * heightFactor
                    val barX = x
                    val barProgress = barX / size.width

                    val color = if (barProgress <= progress) {
                        KuroPrimary.copy(alpha = 0.8f)
                    } else {
                        KuroOnSurfaceVariant.copy(alpha = 0.25f)
                    }

                    // Draw bar centered vertically
                    drawLine(
                        color = color,
                        start = Offset(barX, (size.height - barHeight) / 2f),
                        end = Offset(barX, (size.height + barHeight) / 2f),
                        strokeWidth = barWidth * 0.8f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // Interactive seek overlay (invisible but handles touch)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Simple click to seek (approximate)
                    }
            )

            // Time labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDuration(
                        if (isSeeking) (seekPosition * playerState.duration).toLong()
                        else playerState.currentPosition
                    ),
                    fontSize = 11.sp,
                    color = KuroOnSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatDuration(playerState.duration),
                    fontSize = 11.sp,
                    color = KuroOnSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── Playback Controls ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(onClick = onToggleShuffle, modifier = Modifier.size(44.dp)) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (playerState.shuffleEnabled) KuroPrimary
                        else KuroOnSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Previous
                IconButton(onClick = onSkipPrevious, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = KuroOnBackground,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Play/Pause — large black circle
                Box(
                    modifier = Modifier
                        .size(62.dp)
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
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Next
                IconButton(onClick = onSkipNext, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = KuroOnBackground,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Repeat
                IconButton(onClick = onToggleRepeat, modifier = Modifier.size(44.dp)) {
                    Icon(
                        imageVector = when (playerState.repeatMode) {
                            Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        },
                        contentDescription = "Repeat",
                        tint = if (playerState.repeatMode != Player.REPEAT_MODE_OFF) KuroPrimary
                        else KuroOnSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── Bottom icons row: like + share ───
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = KuroOnSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        tint = KuroOnSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onToggleLike, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = if (playerState.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (playerState.isLiked) "Unlike" else "Like",
                        tint = if (playerState.isLiked) KuroError else KuroOnSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onShowQueue, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Default.QueueMusic,
                        contentDescription = "Queue",
                        tint = KuroOnSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onToggleShuffle, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = KuroOnSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
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
