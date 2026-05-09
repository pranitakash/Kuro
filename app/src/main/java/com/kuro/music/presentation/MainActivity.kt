package com.kuro.music.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kuro.music.presentation.navigation.KuroNavGraph
import com.kuro.music.presentation.navigation.Screen
import com.kuro.music.presentation.ui.components.MiniPlayer
import com.kuro.music.presentation.ui.components.QueueBottomSheet
import com.kuro.music.presentation.ui.screens.NowPlayingScreen
import com.kuro.music.presentation.ui.theme.KuroMiniPlayerBg
import com.kuro.music.presentation.ui.theme.KuroTheme
import com.kuro.music.presentation.viewmodel.LibraryViewModel
import com.kuro.music.presentation.viewmodel.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KuroTheme {
                KuroApp()
            }
        }
    }
}

@Composable
fun KuroApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val libraryViewModel: LibraryViewModel = hiltViewModel()
    val playerState by playerViewModel.playerState.collectAsState()

    var showNowPlaying by remember { mutableStateOf(false) }
    var showQueue by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show Snackbar on player errors
    LaunchedEffect(playerState.error) {
        playerState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            playerViewModel.clearError()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState) { data ->
                        Snackbar(
                            snackbarData = data,
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(bottom = 72.dp)
                        )
                    }
                },
                bottomBar = {
                    Column(
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        // Mini Player
                        if (playerState.currentSong != null && !showNowPlaying) {
                            MiniPlayer(
                                playerState = playerState,
                                onExpand = { showNowPlaying = true },
                                onPlayPause = { playerViewModel.playPause() },
                                onSkipNext = { playerViewModel.skipNext() }
                            )
                        }

                        // ─── Dark Floating Capsule Bottom Navigation ───
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(KuroMiniPlayerBg),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Screen.bottomNavItems.forEach { screen ->
                                    val isSelected = currentDestination?.hierarchy?.any {
                                        it.route == screen.route
                                    } == true

                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                navController.navigate(screen.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                            contentDescription = screen.title,
                                            tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                KuroNavGraph(
                    navController = navController,
                    innerPadding = innerPadding,
                    playerViewModel = playerViewModel,
                    libraryViewModel = libraryViewModel
                )
            }

            // Full-screen Now Playing overlay
            AnimatedVisibility(
                visible = showNowPlaying,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NowPlayingScreen(
                    playerState = playerState,
                    onCollapse = { showNowPlaying = false },
                    onPlayPause = { playerViewModel.playPause() },
                    onSkipNext = { playerViewModel.skipNext() },
                    onSkipPrevious = { playerViewModel.skipPrevious() },
                    onSeekForward = { playerViewModel.seekForward() },
                    onSeekBackward = { playerViewModel.seekBackward() },
                    onSeekTo = { playerViewModel.seekTo(it) },
                    onToggleShuffle = { playerViewModel.toggleShuffle() },
                    onToggleRepeat = { playerViewModel.toggleRepeat() },
                    onToggleLike = { playerViewModel.toggleLike() },
                    onShowQueue = { showQueue = true }
                )
            }

            // Queue bottom sheet
            if (showQueue) {
                QueueBottomSheet(
                    queue = playerState.queue,
                    currentIndex = playerState.currentIndex,
                    onDismiss = { showQueue = false },
                    onSongClick = { index ->
                        playerViewModel.playQueue(playerState.queue, index)
                    },
                    onRemove = { index ->
                        playerViewModel.removeFromQueue(index)
                    },
                    onClearQueue = {
                        playerViewModel.clearQueue()
                        showQueue = false
                    }
                )
            }
        }
    }
}
