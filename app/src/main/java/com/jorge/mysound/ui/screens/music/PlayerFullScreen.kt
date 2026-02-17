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
import com.jorge.mysound.R
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SliderDefaults
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.shadow

@Composable
fun PlayerFullScreen(
    songTitle: String,
    artistName: String,
    artworkUri: String?,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    backgroundColor: Color,
    onDismiss: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit
) {
    val themeBackground = MaterialTheme.colorScheme.background
    val themeOnBackground = MaterialTheme.colorScheme.onBackground
    val themePrimary = MaterialTheme.colorScheme.primary

    // Degradado pro: del color de la carátula al negro profundo de Spotify
    val gradient = Brush.verticalGradient(
        colors = listOf(
            backgroundColor.copy(alpha = 0.8f),
            themeBackground)
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
                    tint = themeOnBackground,
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
                shadowElevation = 20.dp,
                color = Color.Transparent
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
                    color = themeOnBackground,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = artistName,
                    style = MaterialTheme.typography.titleLarge,
                    color = themeOnBackground.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(32.dp))


            Column(modifier = Modifier.fillMaxWidth()) {

                // Variable local para que el slider se mueva suave mientras arrastras
                // sin esperar a que ExoPlayer responda (evita saltos raros)
                var sliderPosition by remember(currentPosition) { mutableStateOf(currentPosition.toFloat()) }
                var isDragging by remember { mutableStateOf(false) }

                // Detectamos si el usuario está tocando para hacer la bolita un poco más grande
                val interactionSource = remember { MutableInteractionSource() }

                @OptIn(ExperimentalMaterial3Api::class) // Necesario para personalizar el Track
                Slider(
                    value = if (isDragging) sliderPosition else currentPosition.toFloat(),
                    onValueChange = { newValue ->
                        isDragging = true
                        sliderPosition = newValue
                    },
                    onValueChangeFinished = {
                        // 🔥 AQUÍ SE LANZA EL SEEK (Solo al soltar el dedo)
                        onSeek(sliderPosition.toLong())
                        isDragging = false
                    },
                    valueRange = 0f..(if (duration > 0) duration.toFloat() else 1f),
                    interactionSource = interactionSource,


                    thumb = {
                        val thumbSize = if (isDragging) 16.dp else 12.dp // Crece un poco al tocar
                        Box(
                            modifier = Modifier
                                .size(thumbSize)
                                .background(themeOnBackground, CircleShape)
                                // Sombra sutil para que destaque sobre portadas claras
                                .shadow(4.dp, CircleShape)
                        )
                    },

                    // 🎨 2. TRACK PERSONALIZADO (Barra fina)
                    track = { sliderState ->
                        SliderDefaults.Track(
                            colors = SliderDefaults.colors(
                                activeTrackColor = themePrimary,
                                inactiveTrackColor = themeOnBackground.copy(alpha = 0.2f)
                            ),
                            sliderState = sliderState,
                            modifier = Modifier.height(4.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Tiempos
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp), // Un pelín de margen para alinear con la bolita
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(if (isDragging) sliderPosition.toLong() else currentPosition),
                        style = MaterialTheme.typography.labelSmall, // Fuente más pequeña y fina
                        color = themeOnBackground.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatTime(duration),
                        style = MaterialTheme.typography.labelSmall,
                        color = themeOnBackground.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                }
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
                        tint = themeOnBackground,
                        modifier = Modifier.size(48.dp)
                    )
                }

                // Play/Pause circular
                Surface(
                    modifier = Modifier
                        .size(85.dp)
                        .clickable { onPlayPause() },
                    shape = CircleShape,
                    color = themePrimary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(
                                id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                            ),
                            contentDescription = "Play/Pause",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }

                // Siguiente
                IconButton(onClick = onNext) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_forward), // USA TU ICONO
                        contentDescription = "Siguiente",
                        tint = themeOnBackground,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}