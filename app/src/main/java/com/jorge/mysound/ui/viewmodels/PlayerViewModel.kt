package com.jorge.mysound.ui.viewmodels

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import com.jorge.mysound.data.remote.SongResponse
import com.jorge.mysound.data.repository.MusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerViewModel(private val repository: MusicRepository) : ViewModel() {
    private var controller: MediaController? = null
    private var appContext: android.content.Context? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentSongTitle = MutableStateFlow<String?>(null)
    val currentSongTitle = _currentSongTitle.asStateFlow()

    private val _currentArtist = MutableStateFlow<String?>(null)
    val currentArtist = _currentArtist.asStateFlow()

    private val _currentArtworkUri = MutableStateFlow<Uri?>(null)
    val currentArtworkUri = _currentArtworkUri.asStateFlow()

    private val _miniPlayerColor = MutableStateFlow(Color(0xFF282828))
    val miniPlayerColor = _miniPlayerColor.asStateFlow()

    private val _queue = MutableStateFlow<List<MediaItem>>(emptyList())
    val queue = _queue.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            _currentSongTitle.value = mediaMetadata.title?.toString()
            _currentArtist.value = mediaMetadata.artist?.toString()
            _currentArtworkUri.value = mediaMetadata.artworkUri

            mediaMetadata.artworkUri?.let { uri ->
                appContext?.let { ctx -> extractColorsFromArt(uri, ctx) }
            } ?: run {
                _miniPlayerColor.value = Color(0xFF282828)
            }
        }

        override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
            // Actualizamos la lista visible en la UI
            val player = controller ?: return
            val currentQueue = mutableListOf<MediaItem>()
            for (i in 0 until player.mediaItemCount) {
                currentQueue.add(player.getMediaItemAt(i))
            }
            _queue.value = currentQueue
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val player = controller ?: return

            // Si estamos en la penúltima canción o última... ¡PIDE REFUERZOS!
            if (player.mediaItemCount > 0 && player.currentMediaItemIndex >= player.mediaItemCount - 2) {
                Log.d("DEBUG_PLAYER", "🚨 La cola se acaba. Buscando recomendaciones...")
                val currentId = mediaItem?.mediaId?.toLongOrNull() ?: return
                fetchRecommendations(currentId)
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _isPlaying.value = controller?.isPlaying ?: false

            if (playbackState == Player.STATE_ENDED) {
                Log.d("DEBUG_PLAYER", "🏁 La canción terminó. Forzando paso a la siguiente...")
                val player = controller ?: return

                if (player.hasNextMediaItem()) {
                    player.seekToNextMediaItem()
                    player.prepare() // Por si acaso se quedó IDLE
                    player.play()
                } else {
                    Log.w("DEBUG_PLAYER", "⚠️ Se acabó la música y no hay nada más en la cola.")
                    // Aquí es donde tu algoritmo de recomendaciones debería haber actuado antes
                }
            }
        }
    }

    // AHORA RECIBE EL CONTEXTO DE LA ACTIVITY
    fun setController(mediaController: MediaController, context: android.content.Context) {
        this.controller = mediaController
        this.appContext = context.applicationContext


        mediaController.addListener(playerListener) // ¡CRUCIAL!

        _isPlaying.value = mediaController.isPlaying
        _currentSongTitle.value = mediaController.mediaMetadata.title?.toString()
        _currentArtist.value = mediaController.mediaMetadata.artist?.toString()
        _currentArtworkUri.value = mediaController.mediaMetadata.artworkUri
    }

    fun playSong(songId: Long, title: String, artist: String, coverUrl: String?) {

        val player = controller ?: return

        player.clearMediaItems()

        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setArtworkUri(coverUrl?.let { Uri.parse(it) })
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaId(songId.toString())
            .setMediaMetadata(metadata)
            .setUri(repository.getStreamUrl(songId))
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        fetchRecommendations(songId)
    }



    fun togglePlayPause() {
        val player = controller ?: return
        if (player.playbackState == Player.STATE_IDLE) player.prepare()
        if (player.isPlaying) player.pause() else player.play()
    }

    private fun extractColorsFromArt(uri: Uri, context: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context).data(uri).allowHardware(false).build()
                val result = loader.execute(request)
                val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                bitmap?.let { bmp ->
                    withContext(Dispatchers.Default) {
                        val palette = Palette.from(bmp).generate()
                        val colorInt = palette.getDarkVibrantColor(0xFF282828.toInt())
                        withContext(Dispatchers.Main) {
                            _miniPlayerColor.value = Color(colorInt)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DEBUG_PLAYER", "Error Palette: ${e.message}")
            }
        }
    }

    fun playPlaylist(songs: List<SongResponse>, startIndex: Int = 0) {
        val player = controller ?: return

        // Convertimos tus Songs a MediaItems
        val mediaItems = songs.map { songToMediaItem(it) }

        player.clearMediaItems() // Aquí sí limpiamos porque es una lista nueva
        player.setMediaItems(mediaItems)
        player.seekToDefaultPosition(startIndex)
        player.prepare()
        player.play()
    }

    fun addToQueue(song: SongResponse) {
        val player = controller ?: return
        player.addMediaItem(songToMediaItem(song))
        Log.d("DEBUG_PLAYER", "Añadida a la cola: ${song.title}")
    }

    fun playSingleSong(song: SongResponse) {
        playPlaylist(listOf(song))
        // Al reproducir una sola, forzamos la carga de recomendaciones inmediata
        fetchRecommendations(song.id)
    }

    private fun songToMediaItem(song: SongResponse): MediaItem {

        val artistString = song.artists.joinToString(", ") { it.name }

        // 👇👇👇 EL FIX: Detectamos si la URL viene "cortada" 👇👇👇
        val BASE_URL = "http://98.85.49.80:8080" // Tu IP de AWS

        val fullCoverUrl = when {
            song.imageUrl == null -> null
            song.imageUrl.startsWith("http") -> song.imageUrl // Ya está bien
            else -> {
                // Si viene "/covers/x.jpg" le pegamos el dominio
                val path = if (song.imageUrl.startsWith("/")) song.imageUrl else "/${song.imageUrl}"
                "$BASE_URL$path"
            }
        }

        Log.d("DEBUG_PLAYER", "Cover procesada: $fullCoverUrl") // Para que lo veas en el log

        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(artistString)
            .setArtworkUri(fullCoverUrl?.let { Uri.parse(it) }) // Usamos la URL arreglada
            .build()

        return MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setMediaMetadata(metadata)
            .setUri(repository.getStreamUrl(song.id))
            .build()
    }

    private fun fetchRecommendations(currentSongId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val recommendations = repository.getRecommendations(currentSongId)

                if (recommendations.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        val player = controller ?: return@withContext

                        // 1. 🕵️‍♂️ EL PORTERO: Escaneamos quién está ya en la fiesta (la cola)
                        val existingIds = mutableSetOf<String>()
                        for (i in 0 until player.mediaItemCount) {
                            val item = player.getMediaItemAt(i)
                            existingIds.add(item.mediaId)
                        }

                        // 2. 🛡️ FILTRO: Solo dejamos pasar a los que NO estén en la lista VIP
                        val uniqueSongs = recommendations.filter { song ->
                            !existingIds.contains(song.id.toString())
                        }

                        if (uniqueSongs.isNotEmpty()) {
                            val mediaItems = uniqueSongs.map { songToMediaItem(it) }
                            player.addMediaItems(mediaItems)
                            Log.d("DEBUG_PLAYER", "✅ Añadidas ${mediaItems.size} canciones NUEVAS (Duplicados eliminados: ${recommendations.size - uniqueSongs.size})")
                        } else {
                            Log.w("DEBUG_PLAYER", "⚠️ El backend envió canciones, pero ya las tenemos todas en la cola. (Efecto Bucle Evitado)")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DEBUG_PLAYER", "Error trayendo recomendaciones: ${e.message}")
            }
        }
    }

    fun skipToNext() {
        val player = controller ?: return

        Log.d("DEBUG_PLAYER", "⏭️ Intentando saltar a siguiente. ¿Hay más?: ${player.hasNextMediaItem()}")

        if (player.hasNextMediaItem()) {
            player.seekToNext()
        } else {
            Log.w("DEBUG_PLAYER", "⚠️ No se puede saltar: La cola está vacía o es el final.")
            // Opcional: Si no hay siguiente, fuerza una recarga de recomendaciones aquí también
            val currentId = player.currentMediaItem?.mediaId?.toLongOrNull()
            if (currentId != null) fetchRecommendations(currentId)
        }
    }

    fun skipToPrevious() {
        val player = controller ?: return
        Log.d("DEBUG_PLAYER", "⏮️ Volviendo atrás/Reiniciando")
        player.seekToPrevious()
    }
}