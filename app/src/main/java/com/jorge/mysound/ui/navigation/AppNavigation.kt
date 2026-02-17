package com.jorge.mysound.ui.navigation

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jorge.mysound.ui.AppViewModelFactory
import com.jorge.mysound.ui.components.PlayerFullScreen
import com.jorge.mysound.ui.components.SpotifyFloatingPlayer
import com.jorge.mysound.ui.screens.main.HomeScreen
import com.jorge.mysound.ui.screens.main.LibraryScreen
import com.jorge.mysound.ui.screens.main.ProfileScreen
import com.jorge.mysound.util.TokenManager
import com.jorge.mysound.ui.screens.main.SearchScreen
import com.jorge.mysound.ui.screens.main.SettingsScreen
import com.jorge.mysound.ui.screens.music.PlaylistDetailScreen
import com.jorge.mysound.ui.viewmodels.HomeViewModel
import com.jorge.mysound.ui.viewmodels.PlayerViewModel
import com.jorge.mysound.ui.viewmodels.PlaylistViewModel
import com.jorge.mysound.ui.viewmodels.SearchViewModel
import com.jorge.mysound.ui.viewmodels.SettingsViewModel

@Composable
fun AppNavigation(
    factory: AppViewModelFactory,
    playerViewModel: PlayerViewModel,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    // 1. ESTADOS DEL REPRODUCTOR
    val currentTitle by playerViewModel.currentSongTitle.collectAsState()
    val currentArtist by playerViewModel.currentArtist.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val artworkUri by playerViewModel.currentArtworkUri.collectAsState()
    val bgColor by playerViewModel.miniPlayerColor.collectAsState()

    // Datos para el Full Screen Player
    val currentPos by playerViewModel.currentPosition.collectAsState()
    val totalDuration by playerViewModel.duration.collectAsState()

    // 2. ESTADO PARA ABRIR/CERRAR EL PLAYER GRANDE
    var isFullScreenVisible by remember { mutableStateOf(false) }

    // ViewModels secundarios (Usando la Factory)
    val searchViewModel: SearchViewModel = viewModel(factory = factory)
    val playlistViewModel: PlaylistViewModel = viewModel(factory = factory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
    val homeViewModel: HomeViewModel = viewModel(factory = factory)
    val tokenManager = factory.tokenManager


    Scaffold(
        // Le decimos al Scaffold que NO gestione los insets automáticamente arriba,
        // para que las imágenes puedan ir detrás de la barra de estado.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),

        bottomBar = {
            // Solo mostramos la barra inferior si el Player GIGANTE está cerrado
            if (!isFullScreenVisible) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background) // 🔥 Fondo negro para evitar transparencias raras
                        .navigationBarsPadding() // 👈 CLAVE: Evita que se solape con la barra de gestos de Android
                ) {
                    // A. MINI PLAYER (Siempre visible si hay canción cargada)
                    if (currentTitle != null) {
                        SpotifyFloatingPlayer(
                            songTitle = currentTitle ?: "Canción",
                            artistName = currentArtist ?: "Artista",
                            isPlaying = isPlaying,
                            backgroundColor = bgColor,
                            artworkUri = artworkUri?.toString(),
                            onPlayerClick = { isFullScreenVisible = true }, // Abre el grande
                            onPlayPauseClick = { playerViewModel.togglePlayPause() },
                            onNextSong = { playerViewModel.skipToNext() },
                            onPreviousSong = { playerViewModel.skipToPrevious() },
                            // Un poco de padding para que "flote" bonito sobre la navbar
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // B. BARRA DE NAVEGACIÓN (SIEMPRE VISIBLE)
                    // Al quitar el "if", sale en todas las pantallas (incluida Playlist),
                    // facilitando el layout y evitando saltos.
                    SpotifyNavBar(navController)
                }
            }
        },

    ) { innerPadding ->

        // 🔥 AQUÍ OCURRE LA MAGIA DE LA NAVEGACIÓN
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                // 🔥 TRUCO DEL ALMENDRUCO:
                // Solo aplicamos padding ABAJO. Arriba dejamos 0 para que el contenido
                // suba hasta el borde (status bar transparente).
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // PANTALLAS PRINCIPALES
            composable(Screen.Home.route) {
                val userId = tokenManager.getUserIdFromToken()
                HomeScreen(
                    viewModel = homeViewModel,
                    userId = userId,
                    onPlaylistClick = { playListId ->
                            Log.d("DEBUG_CLICK", "2. AppNavigation recibe click para ID: $id")
                            playerViewModel.loadAndPlayPlaylist(playListId)
                    }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(searchViewModel, playerViewModel, playlistViewModel)
            }

            composable(Screen.Library.route) {
                val userId = tokenManager.getUserIdFromToken()

                LibraryScreen(
                    onPlaylistClick = { playlistId ->
                        navController.navigate("playlist_detail/$playlistId")
                    },
                    playerViewModel = playerViewModel,
                    playlistViewModel = playlistViewModel,
                    userId = userId
                )
            }

            composable(Screen.Profile.route) {
                val userId = tokenManager.getUserIdFromToken()
                ProfileScreen(
                    userId = userId,
                    onSettingsClick = {
                        navController.navigate("settings")
                    },
                    onLogoutClick = {
                        // Aquí limpias el token y mandas al usuario al Login
                        tokenManager.clearToken()
                        onLogout()
                    },
                    factory = factory
                )
            }

            composable("settings") {
                SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 🔥 PANTALLA DE DETALLE
            composable(
                route = "playlist_detail/{playlistId}",
                arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("playlistId") ?: -1L

                PlaylistDetailScreen(
                    playlistId = id,
                    repository = factory.repository, // O factory.repository según como lo llamaste
                    playerViewModel = playerViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    AnimatedVisibility(
        visible = isFullScreenVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        PlayerFullScreen(
            songTitle = currentTitle ?: "",
            artistName = currentArtist ?: "",
            artworkUri = artworkUri?.toString(),
            isPlaying = isPlaying,
            backgroundColor = bgColor,
            onDismiss = { isFullScreenVisible = false },
            onPlayPause = { playerViewModel.togglePlayPause() },
            onNext = { playerViewModel.skipToNext() },
            onPrevious = { playerViewModel.skipToPrevious() },
            currentPosition = currentPos,
            duration = totalDuration,
            onSeek = { pos -> playerViewModel.seekTo(pos) }
        )
    }
}