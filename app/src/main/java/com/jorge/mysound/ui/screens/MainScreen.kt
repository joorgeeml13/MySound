package com.jorge.mysound.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jorge.mysound.ui.components.SpotifyMiniPlayer
import com.jorge.mysound.ui.navigation.Screen
import com.jorge.mysound.ui.navigation.SpotifyNavBar
import com.jorge.mysound.ui.screens.main.HomeScreen
import com.jorge.mysound.ui.screens.main.LibraryScreen
import com.jorge.mysound.ui.screens.main.ProfileScreen
import com.jorge.mysound.ui.screens.main.SearchScreen
import com.jorge.mysound.ui.viewmodels.PlayerViewModel
import com.jorge.mysound.ui.viewmodels.SearchViewModel

@Composable
fun MainScreen(
    playerViewModel: PlayerViewModel,
    searchViewModel: SearchViewModel
){
    val navController = rememberNavController()

    val currentTitle by playerViewModel.currentSongTitle.collectAsState()
    val currentArtist by playerViewModel.currentArtist.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val artworkUri by playerViewModel.currentArtworkUri.collectAsState()
    val bgColor by playerViewModel.miniPlayerColor.collectAsState()

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentTitle != null) {
                    SpotifyMiniPlayer(
                        songTitle = currentTitle ?: "Cancion",
                        artistName = currentArtist ?: "Artista", // Cámbialo por el estado real del VM
                        isPlaying = isPlaying,
                        backgroundColor = bgColor,
                        artworkUri = artworkUri?.toString(), // Cámbialo por el artworkUri real
                        onPlayPauseClick = { playerViewModel.togglePlayPause() },
                        onNextSong = { /* playerViewModel.next() */ },
                        onPreviousSong = { /* playerViewModel.previous() */ }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                SpotifyNavBar(navController) // La barra abajo
            }
                    },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Search.route) { SearchScreen(searchViewModel, playerViewModel) }
            composable(Screen.Library.route) { LibraryScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
        }
    }
}