package com.jorge.mysound.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jorge.mysound.R // IMPORTANTE: Verifica que este sea tu paquete real

@Composable
fun PlayerFullScreen(
    songTitle: String,
    artistName: String,
    artworkUri: String?,
    isPlaying: Boolean,
    backgroundColor: Color,
    onDismiss: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    // Degradado pro: del color de la carátula al negro profundo de Spotify
    val gradient = Brush.verticalGradient(
        colors = listOf(backgroundColor.copy(alpha = 0.8f), Color(0xFF121212))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botón de cerrar (Flecha hacia abajo)
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrowdown), // USA TU ICONO
                    contentDescription = "Cerrar",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // CARÁTULA GIGANTE
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 20.dp
            ) {
                AsyncImage(
                    model = artworkUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.ic_music_note)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // INFO DE LA CANCIÓN
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = songTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = artistName,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // CONTROLES DE REPRODUCCIÓN
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Anterior
                IconButton(onClick = onPrevious) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_rewind), // USA TU ICONO
                        contentDescription = "Anterior",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                // Play/Pause circular
                Surface(
                    modifier = Modifier
                        .size(85.dp)
                        .clickable { onPlayPause() },
                    shape = CircleShape,
                    color = Color.White
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(
                                id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                            ),
                            contentDescription = "Play/Pause",
                            tint = Color.Black,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }

                // Siguiente
                IconButton(onClick = onNext) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_forward), // USA TU ICONO
                        contentDescription = "Siguiente",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}