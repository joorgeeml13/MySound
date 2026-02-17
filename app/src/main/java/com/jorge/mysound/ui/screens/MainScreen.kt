package com.jorge.mysound.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jorge.mysound.R
import com.jorge.mysound.ui.AppViewModelFactory
import com.jorge.mysound.ui.components.PlayerFullScreen
import com.jorge.mysound.ui.components.SpotifyFloatingPlayer
import com.jorge.mysound.ui.navigation.AppNavigation
import com.jorge.mysound.ui.navigation.SpotifyNavBar
import com.jorge.mysound.ui.viewmodels.PlayerViewModel
import androidx.navigation.compose.rememberNavController

/**
 * MainScreen: Orquestador principal de la interfaz de usuario.
 * Gestiona el Scaffold global, el Host de navegación y la lógica de visibilidad
 * del reproductor (Mini Player vs FullScreen Player).
 */
@Composable
fun MainScreen(
    playerViewModel: PlayerViewModel,
    factory: AppViewModelFactory,
    onLogout: () -> Unit
) {
    // Controlador de navegación para gestionar el flujo entre pantallas
    val navController = rememberNavController()

    // Observación de estados reactivos del reproductor multimedia
    val currentTitle by playerViewModel.currentSongTitle.collectAsState()
    val currentArtist by playerViewModel.currentArtist.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val artworkUri by playerViewModel.currentArtworkUri.collectAsState()
    val bgColor by playerViewModel.miniPlayerColor.collectAsState()
    val currentPos by playerViewModel.currentPosition.collectAsState()
    val totalDuration by playerViewModel.duration.collectAsState()

    // Estado persistente para controlar la transición al reproductor de pantalla completa
    var isFullScreenVisible by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // La barra de navegación solo es visible si no estamos en pantalla completa
            if (!isFullScreenVisible) {
                SpotifyNavBar(navController)
            }
        },
    ) { innerPadding ->
        /**
         * Estructura de capas:
         * 1. Contenedor de navegación (Pantallas de contenido).
         * 2. Mini Player (Superpuesto en la parte inferior).
         */
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // Host principal de navegación
            AppNavigation(
                factory = factory,
                playerViewModel = playerViewModel,
                onLogout = onLogout
            )

            // Lógica de visualización del MiniPlayer flotante
            if (!isFullScreenVisible && currentTitle != null) {
                SpotifyFloatingPlayer(
                    songTitle = currentTitle ?: stringResource(R.string.player_unknown_title),
                    artistName = currentArtist ?: stringResource(R.string.player_unknown_artist),
                    isPlaying = isPlaying,
                    backgroundColor = bgColor,
                    artworkUri = artworkUri?.toString(),
                    onPlayerClick = { isFullScreenVisible = true },
                    onPlayPauseClick = { playerViewModel.togglePlayPause() },
                    onNextSong = { playerViewModel.skipToNext() },
                    onPreviousSong = { playerViewModel.skipToPrevious() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                )
            }
        }
    }

    /**
     * Capa Superior: Reproductor Pantalla Completa.
     * Implementa animaciones de entrada y salida vertical para mejorar la experiencia de usuario (UX).
     */

    AnimatedVisibility(
        visible = isFullScreenVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        PlayerFullScreen(
            songTitle = currentTitle ?: stringResource(R.string.player_unknown_title),
            artistName = currentArtist ?: stringResource(R.string.player_unknown_artist),
            artworkUri = artworkUri?.toString(),
            isPlaying = isPlaying,
            backgroundColor = bgColor,
            onDismiss = { isFullScreenVisible = false },
            onPlayPause = { playerViewModel.togglePlayPause() },
            onNext = { playerViewModel.skipToNext() },
            onPrevious = { playerViewModel.skipToPrevious() },
            currentPosition = currentPos,
            duration = totalDuration,
            onSeek = { newPos -> playerViewModel.seekTo(newPos) }
        )
    }
}