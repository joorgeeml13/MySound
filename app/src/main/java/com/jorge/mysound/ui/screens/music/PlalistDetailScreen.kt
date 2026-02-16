package com.jorge.mysound.ui.screens.music


import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jorge.mysound.data.repository.MusicRepository
import com.jorge.mysound.ui.components.PlaylistHeader
import com.jorge.mysound.ui.components.SongRow
import com.jorge.mysound.ui.viewmodels.PlayerViewModel
import com.jorge.mysound.ui.viewmodels.PlaylistDetailViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext


@Composable
fun PlaylistDetailScreen(
    playlistId: Long, // <--- ¿Qué número llega aquí?
    repository: MusicRepository,
    playerViewModel: PlayerViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    // 🔥 CHIVATO 1: ¿Llega el ID?
    Log.d("DEBUG_DETAIL", "Abriendo pantalla con Playlist ID: $playlistId")

    val viewModel: PlaylistDetailViewModel = viewModel(
        factory = PlaylistDetailViewModel.Factory(repository, playlistId)
    )
    val playlist by viewModel.playlist.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                Log.d("DEBUG_IMAGE", "URI seleccionada: $uri")
                // Llamamos a la función del ViewModel para subirla (Asegúrate de haberla creado)
                viewModel.uploadPlaylistImage(context, uri)
            }
        }
    )

    Scaffold(
        containerColor = Color.Black, // 🔥 FORZAMOS NEGRO PARA VER SI HAY TEXTO
        topBar = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {

            // CASO 1: CARGANDO
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF1DB954)
                )
            }
            // CASO 2: HAY DATOS
            else if (playlist != null) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // 1. Cabecera
                    item {
                        PlaylistHeader(
                            playlist = playlist!!,
                            onImageClick = {
                                // ✅ 2. DISPARAMOS (DENTRO DEL CLICK)
                                // Aquí solo damos la orden de "Fuego"
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                    }

                    // 2. Canciones (o mensaje de vacío)
                    if (playlist!!.songs.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Esta playlist está más vacía que mi nevera 🧊\n¡Añade algo!",
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(playlist!!.songs) { song ->
                            SongRow(song = song, onClick = { /* ... */ })
                        }
                    }
                }
            }
            // CASO 3:
            else {
                Text(
                    text = "ERROR: No se cargó la playlist (ID: $playlistId)",
                    color = Color.Red, // Rojo para que cante
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}