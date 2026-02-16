package com.jorge.mysound.ui.screens.main

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.jorge.mysound.R
import com.jorge.mysound.data.remote.Playlist
import com.jorge.mysound.ui.viewmodels.PlayerViewModel
import com.jorge.mysound.ui.viewmodels.PlaylistViewModel



@Composable
fun LibraryScreen(
    onPlaylistClick: (Long) -> Unit,
    playerViewModel: PlayerViewModel, // Por si quieres reproducir una lista entera
    playlistViewModel: PlaylistViewModel
) {
    val playlists by playlistViewModel.playlists.collectAsState()
    val isLoading by playlistViewModel.isLoading.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(painter = painterResource(R.drawable.ic_add), contentDescription = null, tint = Color.Black)
            }
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            Text(
                stringResource(R.string.library),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1DB954))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // 🚩 Asegúrate de importar: import androidx.compose.foundation.lazy.items
                items(playlists) { playlist ->
                    PlaylistRow(
                        playlist = playlist,
                        onClick = {
                            val id = playlist.id ?: -1L
                            Log.d("DEBUG_NAV", "Click en playlist $id, avisando al jefe...")

                            // 🔥 AVISAMOS AL PADRE, NO NAVEGAMOS DIRECTAMENTE
                            onPlaylistClick(id)
                        }
                    )
                }
            }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Nueva Playlist", color = Color.White) },
                    text = {
                        Column {
                            Text("Dale un nombre a tu lista, locotrón:", color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(
                                value = newPlaylistName,
                                onValueChange = { newPlaylistName = it },
                                placeholder = { Text("Mi playlist #1") },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (newPlaylistName.isNotBlank()) {
                                    playlistViewModel.createNewPlaylist(newPlaylistName, "Descripción opcional")
                                    newPlaylistName = ""
                                    showDialog = false
                                }
                            }
                        ) {
                            Text("CREAR", color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("CANCELAR", color = Color.Gray)
                        }
                    },
                    containerColor = Color(0xFF282828) // Gris oscuro estilo Spotify
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
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Imagen de la playlist (usamos la de la primera canción)
        val coverUrl = playlist.imageUrl ?: playlist.songs.firstOrNull()?.imageUrl

        AsyncImage(
            model = coverUrl, // Si es null, Coil usa el error placeholder
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.ic_music_note) // Tu icono por defecto
        )

        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${playlist.songs.size} canciones",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}