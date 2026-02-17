package com.jorge.mysound.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jorge.mysound.R
import com.jorge.mysound.data.remote.RetrofitClient
import com.jorge.mysound.data.remote.SongResponse
import com.jorge.mysound.ui.viewmodels.PlayerViewModel
import com.jorge.mysound.ui.viewmodels.PlaylistViewModel
import com.jorge.mysound.ui.viewmodels.SearchViewModel

@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel,
    playerViewModel: PlayerViewModel,
    playlistViewModel: PlaylistViewModel
) {
    // Observación de estados reactivos
    val query by searchViewModel.query.collectAsState()
    val results by searchViewModel.searchResults.collectAsState()
    val isSearching by searchViewModel.isSearching.collectAsState()
    val myPlaylists by playlistViewModel.playlists.collectAsState()

    // Gestión del foco para ocultar teclado
    val focusManager = LocalFocusManager.current

    // Estado local para controlar el diálogo de "Añadir a Playlist"
    var songToAdd by remember { mutableStateOf<SongResponse?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .statusBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // BARRA DE BÚSQUEDA PROFESIONAL
        OutlinedTextField(
            value = query,
            onValueChange = { searchViewModel.onQueryChange(it) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(stringResource(R.string.search_hint)) },
            shape = RoundedCornerShape(12.dp), // Bordes más suaves
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { focusManager.clearFocus() } // Ocultar teclado al buscar
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // LISTADO DE RESULTADOS
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 80.dp) // Espacio para el MiniPlayer
        ) {
            items(results) { song ->
                SongSearchItem(
                    song = song,
                    onClick = {
                        // Construcción de metadatos para el reproductor
                        val artistNames = song.artists.joinToString(", ") { it.name }
                        val finalArtist = artistNames.ifBlank { "Unknown Artist" }

                        // Lógica de URL segura
                        val coverUrl = if (song.imageUrl?.startsWith("http") == true) {
                            song.imageUrl
                        } else {
                            "${RetrofitClient.BASE_URL.removeSuffix("/")}/${song.imageUrl?.removePrefix("/")}"
                        }

                        playerViewModel.playSong(
                            songId = song.id,
                            title = song.title,
                            artist = finalArtist,
                            coverUrl = coverUrl
                        )
                    },
                    onMoreClick = { songToAdd = song }
                )
            }
        }

        // DIÁLOGO MODAL PARA AÑADIR A PLAYLIST
        if (songToAdd != null) {
            AlertDialog(
                onDismissRequest = { songToAdd = null },
                containerColor = MaterialTheme.colorScheme.surface,
                title = {
                    Text(
                        stringResource(R.string.search_add_title),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Column {
                        Text(
                            text = stringResource(R.string.search_add_message, songToAdd?.title ?: ""),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (myPlaylists.isEmpty()) {
                            Text(
                                text = stringResource(R.string.search_no_playlists),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                                items(myPlaylists) { playlist ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                // Acción: Vincular canción a la lista
                                                playlistViewModel.addSongToPlaylist(
                                                    playlistId = playlist.id!!,
                                                    songId = songToAdd!!.id
                                                )
                                                songToAdd = null // Cerrar diálogo tras éxito
                                            }
                                            .padding(vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = playlist.name,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { songToAdd = null }) {
                        Text(
                            stringResource(R.string.btn_cancel),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun SongSearchItem(
    song: SongResponse,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    // Construcción de URL robusta para la vista de lista
    val finalUrl = remember(song.imageUrl) {
        if (song.imageUrl?.startsWith("http") == true) {
            song.imageUrl
        } else {
            "${RetrofitClient.BASE_URL.removeSuffix("/")}/${song.imageUrl?.removePrefix("/")}"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        AsyncImage(
            model = finalUrl,
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp)),
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
                text = song.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Gestión de artista desconocido
            val artistText = song.artists.joinToString { it.name }
                .ifBlank { stringResource(R.string.search_artist_unknown) }

            Text(
                text = artistText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onMoreClick) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.playlist_play_all), // Reusando string genérico o crea uno "Opciones"
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}