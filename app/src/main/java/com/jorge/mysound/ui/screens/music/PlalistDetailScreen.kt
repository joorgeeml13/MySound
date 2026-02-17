package com.jorge.mysound.ui.screens.music

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jorge.mysound.R
import com.jorge.mysound.data.repository.MusicRepository
import com.jorge.mysound.ui.components.PlaylistHeader
import com.jorge.mysound.ui.components.SongRow
import com.jorge.mysound.ui.viewmodels.PlayerViewModel
import com.jorge.mysound.ui.viewmodels.PlaylistDetailViewModel
import com.jorge.mysound.util.SortOption

/**
 * PlaylistDetailScreen: Pantalla detallada que muestra el contenido de una lista de reproducción.
 * Permite la gestión de imágenes de portada, reproducción secuencial y ordenación dinámica
 * de las pistas mediante algoritmos de comparación en tiempo real.
 */
@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    repository: MusicRepository,
    playerViewModel: PlayerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    /**
     * Inicialización del ViewModel mediante una Factoría personalizada para inyectar
     * el ID de la playlist y el repositorio de datos.
     */
    val viewModel: PlaylistDetailViewModel = viewModel(
        factory = PlaylistDetailViewModel.Factory(repository, playlistId)
    )

    // Observación de estados reactivos desde el ViewModel y el Reproductor Global
    val playlist by viewModel.playlist.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentPlayingTitle by playerViewModel.currentSongTitle.collectAsState()
    val playingPlaylistId by playerViewModel.currentPlayingPlaylistId.collectAsState()

    // Gestión del estado de ordenación de la lista
    var sortOption by remember { mutableStateOf(SortOption.DEFAULT) }
    var showSortMenu by remember { mutableStateOf(false) }

    /**
     * sortedSongs: Lista calculada de forma optimizada.
     * Se recalcula únicamente si cambia la lista de canciones o el criterio de ordenación.
     */
    val sortedSongs = remember(playlist, sortOption) {
        val songs = playlist?.songs ?: emptyList()
        when (sortOption) {
            SortOption.TITLE -> songs.sortedBy { it.title.lowercase() }
            SortOption.ARTIST -> songs.sortedBy { it.artists.firstOrNull()?.name?.lowercase() ?: "" }
            SortOption.DEFAULT -> songs // Orden original de inserción
        }
    }

    // Launcher para la selección de imágenes desde la galería del dispositivo
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                Log.d("PlaylistDetail", "Imagen seleccionada para upload: $it")
                viewModel.uploadPlaylistImage(context, it)
            }
        }
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.playlist_back_desc),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        floatingActionButton = {
            // El botón de reproducción masiva solo se muestra si existen canciones
            if (sortedSongs.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        playerViewModel.playPlaylist(
                            songs = sortedSongs,
                            startIndex = 0,
                            playlistId = playlistId
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = stringResource(R.string.playlist_play_all),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        modifier = Modifier.padding(top = 16.dp)
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (playlist != null) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {

                    // Sección 1: Cabecera con metadatos y portada
                    item {
                        PlaylistHeader(
                            playlist = playlist!!,
                            onImageClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                    }

                    // Sección 2: Herramientas de ordenación
                    if (sortedSongs.isNotEmpty()) {
                        item {
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                val sortLabel = when(sortOption) {
                                    SortOption.DEFAULT -> stringResource(R.string.playlist_sort_added)
                                    SortOption.TITLE -> stringResource(R.string.playlist_sort_title)
                                    SortOption.ARTIST -> stringResource(R.string.playlist_sort_artist)
                                }

                                Row(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .clickable { showSortMenu = true },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.playlist_sort_label, sortLabel),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Sort,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp).padding(start = 4.dp)
                                    )
                                }

                                // Menú contextual de opciones de ordenación
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false },
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.playlist_sort_menu_recent)) },
                                        onClick = { sortOption = SortOption.DEFAULT; showSortMenu = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.playlist_sort_menu_title)) },
                                        onClick = { sortOption = SortOption.TITLE; showSortMenu = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.playlist_sort_menu_artist)) },
                                        onClick = { sortOption = SortOption.ARTIST; showSortMenu = false }
                                    )
                                }
                            }
                        }
                    }

                    // Sección 3: Listado de pistas o mensaje de lista vacía
                    if (sortedSongs.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.playlist_empty),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = sortedSongs,
                            key = { _, song -> song.id }
                        ) { index, song ->
                            // Identificación visual de la canción que se está reproduciendo actualmente
                            val isCurrentSong = currentPlayingTitle == song.title && playingPlaylistId == playlistId

                            SongRow(
                                song = song,
                                onClick = {
                                    playerViewModel.playPlaylist(
                                        songs = sortedSongs,
                                        startIndex = index,
                                        playlistId = playlistId
                                    )
                                },
                                titleColor = if (isCurrentSong) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else {
                // Estado de error en la carga de datos
                Text(
                    text = stringResource(R.string.playlist_error_id, playlistId),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}