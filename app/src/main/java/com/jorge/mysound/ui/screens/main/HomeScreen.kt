package com.jorge.mysound.ui.screens.main

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jorge.mysound.R
import com.jorge.mysound.data.remote.Playlist
import com.jorge.mysound.data.remote.RetrofitClient
import com.jorge.mysound.ui.viewmodels.HomeViewModel

/**
 * HomeScreen: Pantalla principal del dashboard.
 * Muestra un resumen de las listas de reproducción destacadas y utiliza
 * algoritmos de coincidencia (Match) para sugerir perfiles de usuarios afines.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    userId: Long,
    onPlaylistClick: (Long) -> Unit
) {
    val state by viewModel.homeState.collectAsState()

    // Lógica de recuperación de datos vinculada al ciclo de vida del userId
    LaunchedEffect(userId) {
        viewModel.loadHomeData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Título internacionalizado
        Text(
            text = stringResource(R.string.home_playlists_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // GRID DE PLAYLISTS DESTACADAS
        // Renderizamos dos slots fijos para mantener la simetría del diseño
        Row(modifier = Modifier.fillMaxWidth()) {
            repeat(2) { index ->
                val playlist = state.playlists.getOrNull(index)
                Box(modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
                ) {
                    if (playlist != null) {
                        PlaylistCard(
                            playlist = playlist,
                            onClick = {
                                // Safe Call: Si el ID es nulo, no navegamos
                                playlist.id?.let { onPlaylistClick(it) }
                            }
                        )
                    } else {
                        EmptyPlaylistPlaceholder()
                    }
                }
            }
        }

        // GESTIÓN DE ERRORES (Feedback visual)
        if (state.errorMessageId != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(state.errorMessageId!!),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // SECCIÓN "MATCH" (Alma Gemela Musical)
        state.matchedUser?.let { user ->
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.home_match_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Ocupa el espacio restante verticalmente
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Construcción robusta de la URL del avatar
                    val avatarUrl = if (user.avatarUrl?.startsWith("http") == true) {
                        user.avatarUrl
                    } else {
                        val baseUrl = RetrofitClient.BASE_URL.removeSuffix("/")
                        val path = user.avatarUrl?.removePrefix("/") ?: ""
                        "$baseUrl/$path"
                    }

                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = null, // Decorativo
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.ic_music_note),
                        placeholder = painterResource(R.drawable.ic_music_note)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        // Uso de String Format (%1$s) para inyectar el género
                        Text(
                            text = stringResource(R.string.home_match_description, user.favoriteGenre),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Uso de String Format (%1$d) para inyectar el porcentaje
                    Text(
                        text = stringResource(R.string.home_match_compatibility, 98),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun PlaylistCard(playlist: Playlist, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable {
                Log.d("HomeScreen", "Navegando a playlist: ${playlist.name}")
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            // Imagen de fondo con placeholder local
            AsyncImage(
                model = playlist.imageUrl ?: R.drawable.ic_music_note,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.ic_music_note)
            )

            // Gradiente/Overlay para legibilidad del texto
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )

            Text(
                text = playlist.name,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun EmptyPlaylistPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp)
        )
    }
}