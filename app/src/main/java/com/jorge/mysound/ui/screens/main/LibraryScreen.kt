package com.jorge.mysound.ui.screens.main

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jorge.mysound.R
import com.jorge.mysound.data.remote.Playlist
import com.jorge.mysound.data.remote.RetrofitClient
import com.jorge.mysound.ui.viewmodels.PlayerViewModel
import com.jorge.mysound.ui.viewmodels.PlaylistViewModel

/**
 * LibraryScreen: Pantalla de gestión de la biblioteca personal del usuario.
 * Permite visualizar, crear y acceder a las listas de reproducción.
 * Implementa estados de carga, vistas vacías (Empty States) y diálogos de creación.
 */
@Composable
fun LibraryScreen(
    userId: Long,
    onPlaylistClick: (Long) -> Unit,
    playerViewModel: PlayerViewModel,
    playlistViewModel: PlaylistViewModel
) {
    // Observación de estados reactivos
    val playlists by playlistViewModel.playlists.collectAsState()
    val isLoading by playlistViewModel.isLoading.collectAsState()

    // Estados locales para la gestión del diálogo de creación
    var showDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    // Carga inicial de datos vinculada al ID del usuario
    LaunchedEffect(userId) {
        playlistViewModel.loadUserPlaylists(userId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.library_new_playlist)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Cabecera
            Text(
                text = stringResource(R.string.library_title),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            // Indicador de carga no intrusivo
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // Contenido de la lista
            if (!isLoading && playlists.isEmpty()) {
                // EMPTY STATE: Mensaje amigable si no hay listas
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LibraryMusic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.playlist_empty),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp) // Espacio para el FAB y Player
                ) {
                    items(playlists) { playlist ->
                        PlaylistRow(
                            playlist = playlist,
                            onClick = {
                                val id = playlist.id ?: -1L
                                Log.d("LibraryScreen", "Navegando a detalle playlist: $id")
                                onPlaylistClick(id)
                            }
                        )
                    }
                }
            }

            // DIÁLOGO FLOTANTE DE CREACIÓN
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    containerColor = MaterialTheme.colorScheme.surface,
                    title = {
                        Text(
                            text = stringResource(R.string.library_new_playlist),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = stringResource(R.string.library_playlist_hint),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            TextField(
                                value = newPlaylistName,
                                onValueChange = { newPlaylistName = it },
                                placeholder = { Text(stringResource(R.string.library_placeholder_name)) },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (newPlaylistName.isNotBlank()) {
                                    playlistViewModel.createNewPlaylist(
                                        name = newPlaylistName,
                                        description = "Created via MySound Mobile",
                                        userId = userId
                                    )
                                    newPlaylistName = ""
                                    showDialog = false
                                }
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.btn_create),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text(
                                text = stringResource(R.string.btn_cancel),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PlaylistRow(playlist: Playlist, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Lógica de recuperación de imagen (Carátula de playlist > Primera canción > Fallback)
        // Corrección de URL si viene relativa
        val rawUrl = playlist.imageUrl ?: playlist.songs.firstOrNull()?.imageUrl
        val coverUrl = if (rawUrl?.startsWith("http") == true) {
            rawUrl
        } else if (rawUrl != null) {
            val baseUrl = RetrofitClient.BASE_URL.removeSuffix("/")
            val path = rawUrl.removePrefix("/")
            "$baseUrl/$path"
        } else {
            null
        }

        AsyncImage(
            model = coverUrl,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant), // Fondo mientras carga
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.ic_music_note),
            placeholder = painterResource(R.drawable.ic_music_note)
        )

        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        ) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                // Uso de Plurales/Format strings para internacionalización correcta
                text = stringResource(R.string.library_songs_count, playlist.songs.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}