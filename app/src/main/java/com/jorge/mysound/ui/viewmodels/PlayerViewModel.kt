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

        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setArtworkUri(coverUrl?.let { Uri.parse(it) })
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaId(songId.toString())
            .setMediaMetadata(metadata)
            .setUri(repository.getStreamUrl(songId)) // Asumo que tienes este método en el repo
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
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
}