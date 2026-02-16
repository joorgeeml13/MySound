package com.jorge.mysound.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jorge.mysound.data.repository.MusicRepository
import com.jorge.mysound.ui.components.PlayerFullScreen
import com.jorge.mysound.ui.components.SpotifyMiniPlayer
import com.jorge.mysound.ui.navigation.Screen
import com.jorge.mysound.ui.navigation.SpotifyNavBar
import com.jorge.mysound.ui.screens.main.HomeScreen
import com.jorge.mysound.ui.screens.main.LibraryScreen
import com.jorge.mysound.ui.screens.main.ProfileScreen
import com.jorge.mysound.ui.screens.main.SearchScreen
import com.jorge.mysound.ui.viewmodels.PlayerViewModel
import com.jorge.mysound.ui.viewmodels.PlaylistViewModel
import com.jorge.mysound.ui.viewmodels.SearchViewModel

@Composable
fun MainScreen(
    playerViewModel: PlayerViewModel,
    searchViewModel: SearchViewModel,
    repository: MusicRepository,
    onNavigateToPlaylist: (Long) -> Unit
){
    val navController = rememberNavController()

    // Observando el estado del reproductor
    val currentTitle by playerViewModel.currentSongTitle.collectAsState()
    val currentArtist by playerViewModel.currentArtist.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val artworkUri by playerViewModel.currentArtworkUri.collectAsState()
    val bgColor by playerViewModel.miniPlayerColor.collectAsState()

    val playlistViewModel: PlaylistViewModel = viewModel(
        factory = PlaylistViewModel.Factory(repository)
    )

    // Estado para controlar la visibilidad del reproductor completo
    var isFullScreenVisible by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            // Solo mostramos el miniplayer y la nav bar si NO estamos en pantalla completa
            if (!isFullScreenVisible) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (currentTitle != null) {
                        SpotifyMiniPlayer(
                            songTitle = currentTitle ?: "Canción",
                            artistName = currentArtist ?: "Artista",
                            isPlaying = isPlaying,
                            backgroundColor = bgColor,
                            artworkUri = artworkUri?.toString(),
                            // 🔥 Abrimos el reproductor completo al hacer click
                            onPlayerClick = { isFullScreenVisible = true },
                            onPlayPauseClick = { playerViewModel.togglePlayPause() },
                            onNextSong = { playerViewModel.skipToNext() },
                            onPreviousSong = { playerViewModel.skipToPrevious() }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    SpotifyNavBar(navController)
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            // OJO: Si ocultamos el bottom bar, el padding cambia, tenlo en cuenta
            modifier = Modifier.padding(bottom = if (isFullScreenVisible) 0.dp else innerPadding.calculateBottomPadding())
                .statusBarsPadding()
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Search.route) { SearchScreen(searchViewModel, playerViewModel) }
            composable(Screen.Library.route) {
                LibraryScreen(

                    onPlaylistClick = onNavigateToPlaylist,
                    playerViewModel = playerViewModel,
                    playlistViewModel = playlistViewModel
                )
            }
            composable(Screen.Profile.route) { ProfileScreen() }
        }
    }

    // 🎭 REPRODUCTOR PANTALLA COMPLETA CON ANIMACIÓN
    AnimatedVisibility(
        visible = isFullScreenVisible,
        enter = slideInVertically(initialOffsetY = { it }), // Sube desde abajo
        exit = slideOutVertically(targetOffsetY = { it })   // Baja al cerrar
    ) {
        PlayerFullScreen(

            songTitle = currentTitle ?: "",
            artistName = currentArtist ?: "",
            artworkUri = artworkUri?.toString(),
            isPlaying = isPlaying,
            backgroundColor = bgColor,
            onDismiss = { isFullScreenVisible = false }, // Volver al miniplayer
            onPlayPause = { playerViewModel.togglePlayPause() },
            onNext = { playerViewModel.skipToNext() },
            onPrevious = { playerViewModel.skipToPrevious() }
        )
    }
}