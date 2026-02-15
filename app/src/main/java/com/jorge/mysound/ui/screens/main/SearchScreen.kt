package com.jorge.mysound.ui.screens.main

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.jorge.mysound.data.remote.RetrofitClient
import com.jorge.mysound.data.remote.SongResponse

// Estos son los de tu propio proyecto, gordo.
// Asegúrate de que los paquetes coincidan con tu estructura real.
import com.jorge.mysound.ui.viewmodels.SearchViewModel
import com.jorge.mysound.ui.viewmodels.PlayerViewModel

@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel,
    playerViewModel: PlayerViewModel // Inyectamos el del reproductor para darle al play
) {
    val query by searchViewModel.query.collectAsState()
    val results by searchViewModel.searchResults.collectAsState()
    val isSearching by searchViewModel.isSearching.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Barra de búsqueda nivel pro
        OutlinedTextField(
            value = query,
            onValueChange = { searchViewModel.onQueryChange(it) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Busca una cancion...") },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    // 3. Aquí es donde ejecutas la búsqueda si quieres
                    // O simplemente escondes el teclado
                    defaultKeyboardAction(ImeAction.Search)
                }
            ),
            trailingIcon = {
                if (isSearching) CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(results) { song ->
                SongItem(
                    song = song,
                    onClick = {
                        val artistNames = song.artists.joinToString(", ") { it.name }
                        val finalArtist = artistNames.ifBlank { "Artista Desconocido" }

                        playerViewModel.playSong(
                            songId = song.id,
                            title = song.title,
                            artist = finalArtist,
                            coverUrl = RetrofitClient.BASE_URL.removeSuffix("/") + song.imageUrl
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun SongItem(song: SongResponse, onClick: () -> Unit) {
    val finalUrl = RetrofitClient.BASE_URL.removeSuffix("/") + song.imageUrl
    Log.d("DEBUG_URL", "Cargando imagen desde: $finalUrl")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = finalUrl,
            contentDescription = null,
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop,
            onState = { state ->
                if (state is AsyncImagePainter.State.Error) {
                    Log.e("COIL_ERROR", "Error cargando: ${state.result.throwable}")
                }
            },
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(text = song.title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = song.artists.joinToString { it.name },
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}