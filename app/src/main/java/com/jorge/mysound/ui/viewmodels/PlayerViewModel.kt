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
import com.jorge.mysound.data.remote.RetrofitClient
import com.jorge.mysound.data.remote.SongResponse
import com.jorge.mysound.data.repository.MusicRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * PlayerViewModel: Componente central para la gestión de la reproducción multimedia.
 * Coordina la comunicación entre la interfaz de Compose y el MediaController de Media3.
 * Incluye lógica para extracción dinámica de colores, gestión de colas y recomendaciones automáticas.
 */
class PlayerViewModel(private val repository: MusicRepository) : ViewModel() {

    private var controller: MediaController? = null
    private var appContext: android.content.Context? = null

    // Estados reactivos de reproducción
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentSongTitle = MutableStateFlow<String?>(null)
    val currentSongTitle = _currentSongTitle.asStateFlow()

    private val _currentArtist = MutableStateFlow<String?>(null)
    val currentArtist = _currentArtist.asStateFlow()

    private val _currentArtworkUri = MutableStateFlow<Uri?>(null)
    val currentArtworkUri = _currentArtworkUri.asStateFlow()

    // Color extraído dinámicamente de la carátula para la interfaz de usuario
    private val _miniPlayerColor = MutableStateFlow(Color(0xFF282828))
    val miniPlayerColor = _miniPlayerColor.asStateFlow()

    private val _queue = MutableStateFlow<List<MediaItem>>(emptyList())
    val queue = _queue.asStateFlow()

    // Estados para la gestión del tiempo y barra de progreso
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private val _currentPlayingPlaylistId = MutableStateFlow<Long?>(null)
    val currentPlayingPlaylistId = _currentPlayingPlaylistId.asStateFlow()

    // Tarea asíncrona para la actualización del progreso (Timer)
    private var progressJob: Job? = null

    /**
     * Listener de Media3 para reaccionar a cambios en el estado del reproductor.
     */
    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            if (isPlaying) startProgressUpdate() else progressJob?.cancel()
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            _currentSongTitle.value = mediaMetadata.title?.toString()
            _currentArtist.value = mediaMetadata.artist?.toString()
            _currentArtworkUri.value = mediaMetadata.artworkUri

            // Ejecución de Palette API si existe una carátula válida
            mediaMetadata.artworkUri?.let { uri ->
                appContext?.let { ctx -> extractColorsFromArt(uri, ctx) }
            } ?: run {
                _miniPlayerColor.value = Color(0xFF282828)
            }
        }

        override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
            updateQueueState()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val player = controller ?: return
            // Lógica de "Infinite Play": Solicita recomendaciones antes de finalizar la cola
            if (player.mediaItemCount > 0 && player.currentMediaItemIndex >= player.mediaItemCount - 2) {
                val currentId = mediaItem?.mediaId?.toLongOrNull() ?: return
                fetchRecommendations(currentId)
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _isPlaying.value = controller?.isPlaying ?: false
            if (playbackState == Player.STATE_ENDED) handlePlaybackEnded()
        }
    }

    /**
     * Sincroniza el controlador de medios con el ViewModel y registra los listeners.
     */
    fun setController(mediaController: MediaController, context: android.content.Context) {
        this.controller = mediaController
        this.appContext = context.applicationContext

        mediaController.addListener(playerListener)

        // Inicialización de estados con los valores actuales del controlador
        _isPlaying.value = mediaController.isPlaying
        _currentSongTitle.value = mediaController.mediaMetadata.title?.toString()
        _currentArtist.value = mediaController.mediaMetadata.artist?.toString()
        _currentArtworkUri.value = mediaController.mediaMetadata.artworkUri

        updateQueueState()
        if (mediaController.isPlaying) startProgressUpdate()
    }

    /**
     * Inicia la reproducción de una canción individual, limpiando la cola previa.
     */
    fun playSong(songId: Long, title: String, artist: String, coverUrl: String?) {
        _currentPlayingPlaylistId.value = null
        val player = controller ?: return

        player.clearMediaItems()
        val mediaItem = createMediaItem(songId, title, artist, coverUrl)

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        fetchRecommendations(songId)
    }

    /**
     * Alterna entre los estados de reproducción y pausa.
     */
    fun togglePlayPause() {
        val player = controller ?: return
        if (player.playbackState == Player.STATE_IDLE) player.prepare()

        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    /**
     * Implementación de Palette API para generar una experiencia visual inmersiva
     * basada en los colores predominantes de la carátula actual.
     */
    private fun extractColorsFromArt(uri: Uri, context: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context).data(uri).allowHardware(false).build()
                val result = loader.execute(request)
                val bitmap = (result.drawable as? BitmapDrawable)?.bitmap

                bitmap?.let { bmp ->
                    val palette = Palette.from(bmp).generate()
                    val colorInt = palette.getDarkVibrantColor(0xFF282828.toInt())
                    withContext(Dispatchers.Main) {
                        _miniPlayerColor.value = Color(colorInt)
                    }
                }
            } catch (e: Exception) {
                Log.e("PlayerVM_Palette", "Error al extraer colores: ${e.message}")
            }
        }
    }

    /**
     * Carga y reproduce una lista completa de canciones.
     */
    fun playPlaylist(songs: List<SongResponse>, startIndex: Int = 0, playlistId: Long) {
        _currentPlayingPlaylistId.value = playlistId
        val player = controller ?: return

        val mediaItems = songs.map { songToMediaItem(it) }

        player.clearMediaItems()
        player.setMediaItems(mediaItems)
        player.seekTo(startIndex, 0L)
        player.prepare()
        player.play()
    }

    /**
     * Mapea un objeto SongResponse del dominio a un MediaItem de Media3,
     * gestionando la reconstrucción de URLs para imágenes alojadas en el servidor.
     */
    private fun songToMediaItem(song: SongResponse): MediaItem {
        val artistString = song.artists.joinToString(", ") { it.name }
        val baseUrl = RetrofitClient.BASE_URL.removeSuffix("/")

        val fullCoverUrl = when {
            song.imageUrl == null -> null
            song.imageUrl.startsWith("http") -> song.imageUrl
            else -> "$baseUrl/${song.imageUrl.removePrefix("/")}"
        }

        return createMediaItem(song.id, song.title, artistString, fullCoverUrl)
    }

    private fun createMediaItem(id: Long, title: String, artist: String, cover: String?): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setArtworkUri(cover?.let { Uri.parse(it) })
            .build()

        return MediaItem.Builder()
            .setMediaId(id.toString())
            .setMediaMetadata(metadata)
            .setUri(repository.getStreamUrl(id))
            .build()
    }

    /**
     * Algoritmo de recomendaciones: Solicita nuevas canciones basadas en la actual
     * y filtra duplicados para evitar ciclos de reproducción infinitos sobre los mismos temas.
     */
    private fun fetchRecommendations(currentSongId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val recommendations = repository.getRecommendations(currentSongId)
                if (recommendations.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        val player = controller ?: return@withContext

                        // Filtrado de seguridad: solo añadimos canciones que no estén ya en la cola
                        val existingIds = (0 until player.mediaItemCount).map { player.getMediaItemAt(it).mediaId }.toSet()
                        val uniqueItems = recommendations.filter { it.id.toString() !in existingIds }
                            .map { songToMediaItem(it) }

                        if (uniqueItems.isNotEmpty()) {
                            player.addMediaItems(uniqueItems)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("PlayerVM_Recs", "Error en el motor de recomendaciones: ${e.message}")
            }
        }
    }

    /**
     * Gestiona el temporizador de progreso mediante una corrutina vinculada al ciclo de vida del ViewModel.
     */
    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                controller?.let {
                    _currentPosition.value = it.currentPosition
                    _duration.value = it.duration.coerceAtLeast(0L)
                }
                delay(500)
            }
        }
    }

    private fun updateQueueState() {
        val player = controller ?: return
        val currentQueue = (0 until player.mediaItemCount).map { player.getMediaItemAt(it) }
        _queue.value = currentQueue
    }

    private fun handlePlaybackEnded() {
        val player = controller ?: return
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
            player.play()
        }
    }

    // En PlayerViewModel.kt

    fun loadAndPlayPlaylist(playlistId: Long) {
        viewModelScope.launch {
            Log.d("DEBUG_PLAYER", "3. ViewModel iniciando carga para playlist: $playlistId")
            try {
                // 1. Obtenemos la playlist completa del repositorio
                val playlist = repository.getPlaylistById(playlistId)

                // 2. Sacamos las canciones
                val songs = playlist.songs

                if (songs.isNotEmpty()) {
                    Log.d("DEBUG_PLAYER", "✅ Playlist cargada con ${songs.size} canciones. Reproduciendo...")
                    // 3. Usamos la función que ya tienes para tocar la lista
                    playPlaylist(songs = songs, startIndex = 0, playlistId = playlistId)
                } else {
                    Log.e("DEBUG_PLAYER", "⚠️ La playlist está vacía.")
                }
            } catch (e: Exception) {
                Log.e("PLAYER_ERROR", "Error al cargar playlist: ${e.message}")
            }
        }
    }

    fun skipToNext() = controller?.seekToNext()
    fun skipToPrevious() = controller?.seekToPrevious()
    fun seekTo(position: Long) {
        controller?.seekTo(position)
        _currentPosition.value = position
    }
}