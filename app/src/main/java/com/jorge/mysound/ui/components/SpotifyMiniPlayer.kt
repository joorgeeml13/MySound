package com.jorge.mysound.ui.components

import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.animation.Animatable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jorge.mysound.R
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SpotifyMiniPlayer(
    songTitle: String,
    artistName: String,
    isPlaying: Boolean,
    backgroundColor: Color, // <-- El color que viene del Palette
    artworkUri: String?,   // <-- La URL de la carátula
    onPlayPauseClick: () -> Unit,
    onNextSong: () -> Unit,
    onPreviousSong: () -> Unit,
    modifier: Modifier = Modifier
) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    // ANIMACIÓN: Transición suave de 500ms entre colores
    val animatedBgColor by animateColorAsState(
        targetValue = backgroundColor,
        animationSpec = tween(durationMillis = 500),
        label = "ColorAnimation"
    )

    Surface(
        color = animatedBgColor, // ¡USAMOS EL COLOR ANIMADO!
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(64.dp)
            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    scope.launch { offsetX.snapTo(offsetX.value + delta) }
                },
                onDragStopped = {
                    scope.launch {
                        if (offsetX.value > 150) onPreviousSong()
                        else if (offsetX.value < -150) onNextSong()

                        offsetX.animateTo(
                            targetValue = 0f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        )
                    }
                }
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            // CARÁTULA REAL CON COIL
            AsyncImage(
                model = artworkUri,
                contentDescription = "Carátula",
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(4.dp)), // Bordes suaves
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.ic_music_note)
            )

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
                    color = Color.White.copy(alpha = 0.7f), // Un poco traslúcido queda más pro
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // BOTÓN PLAY/PAUSE con area click propia
            Box(
                modifier = Modifier
                    .size(48.dp) // Área de toque estándar (48dp)
                    .clip(RoundedCornerShape(24.dp)) // Para que el ripple sea circular
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null // O déjalo en null si odias el brillo
                    ) {
                        onPlayPauseClick()
                        Log.d("DEBUG_PLAYER", "¡CLICK en el icono de Play/Pause!")
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp) // El icono sigue midiendo 32, pero el click es de 48
                )
            }
        }
    }
}