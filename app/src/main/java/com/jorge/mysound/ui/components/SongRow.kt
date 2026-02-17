package com.jorge.mysound.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jorge.mysound.R
import com.jorge.mysound.data.remote.RetrofitClient
import com.jorge.mysound.data.remote.SongResponse

/**
 * SongRow: Componente de lista optimizado para la visualización de pistas musicales.
 * Implementa carga de imágenes asíncrona mediante Coil y gestión de estados de selección
 * a través de la personalización de colores de texto.
 */
@Composable
fun SongRow(
    song: SongResponse,
    onClick: () -> Unit,
    titleColor: Color = MaterialTheme.colorScheme.onSurface
) {
    /**
     * Reconstrucción dinámica de la URL de la carátula.
     * Gestiona la concatenación de la dirección base con el path relativo del servidor,
     * asegurando que no existan inconsistencias en los separadores de ruta.
     */
    val fullUrl = if (song.imageUrl != null) {
        val baseUrl = RetrofitClient.BASE_URL.removeSuffix("/")
        val imagePath = song.imageUrl.removePrefix("/")
        "$baseUrl/$imagePath"
    } else null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Renderizado de la imagen con gestión de estados de carga y error
        AsyncImage(
            model = fullUrl,
            contentDescription = null, // Accesibilidad gestionada en el contenedor padre si es necesario
            modifier = Modifier.size(50.dp),
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.ic_music_note),
            placeholder = painterResource(R.drawable.ic_music_note)
        )

        /**
         * Contenedor de metadatos de la canción.
         * Utiliza elipsis para evitar desbordamientos visuales en dispositivos con pantallas pequeñas.
         */
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = song.title,
                color = titleColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )

            // Transformación de la lista de artistas en una cadena legible
            val artistsNames = song.artists.joinToString(", ") { it.name }.ifEmpty {
                stringResource(R.string.player_unknown_artist)
            }

            Text(
                text = artistsNames,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}