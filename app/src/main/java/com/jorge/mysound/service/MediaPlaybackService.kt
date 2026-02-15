package com.jorge.mysound.service

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.jorge.mysound.util.TokenManager
import com.jorge.mysound.data.remote.RetrofitClient
import com.jorge.mysound.data.repository.MusicRepository

class MediaPlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    // Inyectamos Repo y TokenManager
    private lateinit var musicRepository: MusicRepository
    private lateinit var tokenManager: TokenManager

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        // 1. Inicializamos dependencias (El Service es un Context)
        val apiService = RetrofitClient.getInstance(this)
        musicRepository = MusicRepository(apiService)
        tokenManager = TokenManager(this) // ¡Necesitamos esto para el streaming!

        // 2. Buffer para que no se corte con mal internet
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                20_000, 60_000, 1_000, 2_000
            ).build()

        // 3. LA MAGIA DEL TOKEN: Configuramos el DataSource con la cabecera Auth
        // Si no haces esto, te comerás un 403 Forbidden
        val token = tokenManager.getToken()
        val headers = if (token != null) {
            mapOf("Authorization" to "Bearer $token")
        } else {
            emptyMap()
        }

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(10_000)
            .setReadTimeoutMs(10_000)
            .setDefaultRequestProperties(headers) // <--- ¡AQUÍ ESTÁ LA CLAVE!

        // 4. Extractores (Quitamos configuraciones raras que pueden dar error de versión)
        val extractorsFactory = DefaultExtractorsFactory()

        val renderersFactory = DefaultRenderersFactory(this)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)

        // 5. Construimos el Player
        player = ExoPlayer.Builder(this)
            .setRenderersFactory(renderersFactory)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(this, extractorsFactory)
                    .setDataSourceFactory(dataSourceFactory)
            )
            .setLoadControl(loadControl)
            .build()

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(MediaSessionCallback())
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    // --- CALLBACK INTERNO ---
    private inner class MediaSessionCallback : MediaSession.Callback {

        // Cuidado con los imports de Futures y ListenableFuture
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<List<MediaItem>> { // <--- OJO: Devuelve List, no MutableList

            val updatedItems = mediaItems.map { item ->
                if (item.localConfiguration?.uri == null || item.localConfiguration?.uri == Uri.EMPTY) {
                    val songId = item.mediaId.toLong()

                    // Usamos la variable correcta: musicRepository
                    // Asegúrate de que getStreamUrl devuelva un String, NO una llamada a Retrofit suspend
                    val streamUrl = musicRepository.getStreamUrl(songId)

                    item.buildUpon()
                        .setUri(Uri.parse(streamUrl)) // Parseamos el String a Uri
                        .setMediaMetadata(item.mediaMetadata)
                        .build()
                } else {
                    item
                }
            }

            // Devolvemos la lista actualizada (Futures.immediateFuture espera un List)
            return Futures.immediateFuture(updatedItems)
        }

        @OptIn(UnstableApi::class)
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon().build()
            val playerCommands = Player.Commands.Builder().addAllCommands().build()
            return MediaSession.ConnectionResult.accept(sessionCommands, playerCommands)
        }
    }
}