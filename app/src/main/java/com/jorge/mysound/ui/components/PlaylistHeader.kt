package com.jorge.mysound.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jorge.mysound.R
import com.jorge.mysound.data.remote.Playlist

/**
 * PlaylistHeader: Componente de cabecera para la visualización detallada de una lista.
 * Presenta la carátula principal, el título de la colección y metadatos cuantitativos.
 * Permite la interacción con la imagen para disparar eventos de edición (Picker de fotos).
 */
@Composable
fun PlaylistHeader(
    playlist: Playlist,
    onImageClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /**
         * Imagen de portada con soporte para placeholders locales.
         * Implementa un modificador de click para permitir la actualización del recurso.
         */
        AsyncImage(
            model = playlist.imageUrl ?: R.drawable.ic_music_note,
            contentDescription = stringResource(R.string.playlist_back_desc), // Reutilizando para accesibilidad
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(240.dp) // Proporción áurea para dispositivos móviles
                .clip(RoundedCornerShape(16.dp))
                .clickable { onImageClick() },
            error = painterResource(R.drawable.ic_music_note),
            placeholder = painterResource(R.drawable.ic_music_note)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Título de la lista con tipografía de alto impacto
        Text(
            text = playlist.name,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold
        )

        /**
         * Metadatos de la lista (Número de pistas).
         * Utiliza recursos de cadena formateados para soportar la localización dinámica.
         */
        Text(
            text = stringResource(R.string.library_songs_count, playlist.songs.size),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}