package com.jorge.mysound.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jorge.mysound.R

@Composable
fun SpotifyMiniPlayer(
    modifier: Modifier = Modifier
) {
    Surface(
        // Color oscuro con un pelín de transparencia
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp), // Esquinas redondeadas estilo Spoty
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp) // No llega a los bordes
            .height(60.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            // Carátula
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                // Aquí iría tu AsyncImage
            }

            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = "Título de la canción",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "Artista",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                // Controles
                Icon(
                    painter = painterResource(id = R.drawable.ic_rewind), // Usa tus iconos
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp).clickable {
                        /* Play/Pause */
                    }
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_play), // Usa tus iconos
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp).clickable {
                        /* Play/Pause */
                    }
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_forward), // Usa tus iconos
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp).clickable {
                        /* Play/Pause */
                    }
                )
            }
        }
    }
}