package com.jorge.mysound.ui.components


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.jorge.mysound.R
import com.jorge.mysound.data.remote.Playlist

@Composable
fun PlaylistHeader(playlist: Playlist,
                   onImageClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = playlist.imageUrl ?: R.drawable.ic_music_note,
            contentDescription = "Portada de la lista",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(200.dp) // Tamaño grande
                .clip(RoundedCornerShape(12.dp))
                // 🔥 2. HAZ QUE LA IMAGEN REACCIONE AL CLICK
                .clickable { onImageClick() }
        )
        Text(
            text = playlist.name,
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${playlist.songs.size} canciones",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}