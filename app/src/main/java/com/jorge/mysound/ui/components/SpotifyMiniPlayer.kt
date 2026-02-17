package com.jorge.mysound.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jorge.mysound.R
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * SpotifyFloatingPlayer: Componente de reproducción persistente con diseño flotante.
 * Implementa gestos horizontales para el cambio de pistas y transiciones de color
 * dinámicas basadas en la carátula del contenido actual.
 */
@Composable
fun SpotifyFloatingPlayer(
    songTitle: String,
    artistName: String,
    isPlaying: Boolean,
    backgroundColor: Color,
    artworkUri: String?,
    onPlayPauseClick: () -> Unit,
    onPlayerClick: () -> Unit,
    onNextSong: () -> Unit,
    onPreviousSong: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Control de desplazamiento horizontal para gestos de cambio de pista
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    // Animación de transición de color entre diferentes carátulas
    val animatedBgColor by animateColorAsState(
        targetValue = if (backgroundColor == Color.Black) Color(0xFF282828) else backgroundColor,
        animationSpec = tween(500),
        label = "ColorTransition"
    )

    /**
     * Contenedor raíz con transparencia total para evitar solapamientos visuales
     * en las esquinas del componente redondeado.
     */
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        /**
         * Superficie del reproductor. Se utiliza drawBehind para renderizar el fondo
         * de forma eficiente sin interrumpir la jerarquía de layouts.
         */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(bottom = 8.dp)
                .height(60.dp)
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .drawBehind {
                    drawRoundRect(
                        color = animatedBgColor,
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                }
                .clickable { onPlayerClick() }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        scope.launch { offsetX.snapTo(offsetX.value + delta) }
                    },
                    onDragStopped = {
                        val threshold = 150f
                        scope.launch {
                            when {
                                offsetX.value > threshold -> onPreviousSong()
                                offsetX.value < -threshold -> onNextSong()
                            }
                            // Retorno al centro con efecto de muelle (Spring Physics)
                            offsetX.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    }
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                // Imagen de carátula con carga asíncrona
                AsyncImage(
                    model = artworkUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.ic_music_note)
                )

                // Información textual (Título y Artista)
                Column(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = songTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = artistName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Control de reproducción (Play/Pause) con accesibilidad multi-idioma
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onPlayPauseClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                        ),
                        contentDescription = if (isPlaying)
                            stringResource(R.string.btn_cancel) // Usando recursos existentes
                        else stringResource(R.string.btn_create),
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}