package com.kuro.music.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kuro.music.presentation.ui.screens.DownloadsScreen
import com.kuro.music.presentation.ui.screens.EqualizerScreen
import com.kuro.music.presentation.ui.screens.HistoryScreen
import com.kuro.music.presentation.ui.screens.HomeScreen
import com.kuro.music.presentation.ui.screens.LibraryScreen
import com.kuro.music.presentation.ui.screens.LikedSongsScreen
import com.kuro.music.presentation.ui.screens.PlaylistDetailScreen
import com.kuro.music.presentation.ui.screens.SearchScreen
import com.kuro.music.presentation.ui.screens.SettingsScreen
import com.kuro.music.presentation.viewmodel.LibraryViewModel
import com.kuro.music.presentation.viewmodel.PlayerViewModel

@Composable
fun KuroNavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    playerViewModel: PlayerViewModel,
    libraryViewModel: LibraryViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier.padding(innerPadding)
    ) {
        composable(Screen.Home.route) {
            HomeScreen(playerViewModel = playerViewModel)
        }
        composable(Screen.Search.route) {
            SearchScreen(playerViewModel = playerViewModel)
        }
        composable(Screen.Library.route) {
            LibraryScreen(
                onNavigateToLikedSongs = { navController.navigate(Screen.LikedSongs.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToDownloads = { navController.navigate(Screen.Downloads.route) },
                onNavigateToPlaylist = { id -> navController.navigate("playlist/$id") },
                viewModel = libraryViewModel
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        composable(Screen.LikedSongs.route) {
            LikedSongsScreen(
                likedSongs = libraryViewModel.likedSongs,
                playerViewModel = playerViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.History.route) {
            HistoryScreen(
                recentlyPlayed = libraryViewModel.recentlyPlayed,
                playerViewModel = playerViewModel,
                onClearHistory = { libraryViewModel.clearHistory() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.PlaylistDetail.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
        ) {
            PlaylistDetailScreen(
                playerViewModel = playerViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Downloads.route) {
            DownloadsScreen(
                playerViewModel = playerViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Equalizer.route) {
            EqualizerScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
