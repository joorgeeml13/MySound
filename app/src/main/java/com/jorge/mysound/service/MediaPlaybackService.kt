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

/**
 * MediaPlaybackService: Servicio en primer plano (Foreground Service) encargado de la
 * reproducción de audio persistente. Implementa Media3 para integrarse con los controles
 * del sistema y gestionar la sesión multimedia de forma independiente a la UI.
 */
class MediaPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    // Dependencias inyectadas para la gestión de datos y seguridad
    private lateinit var musicRepository: MusicRepository
    private lateinit var tokenManager: TokenManager

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        // Inicialización de componentes de red y persistencia
        val apiService = RetrofitClient.getInstance(this)
        musicRepository = MusicRepository(apiService)
        tokenManager = TokenManager(this)

        /**
         * Estrategia de Buffering (LoadControl):
         * Configuramos el buffer para mitigar micro-cortes en redes móviles inestables.
         * - Min Buffer: 30s | Max Buffer: 50s | Playback Buffer: 1s
         */
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                30_000, // Duración mínima de buffer
                50_000, // Duración máxima de buffer
                1_000,  // Buffer necesario para iniciar reproducción
                5_000   // Buffer necesario para reanudar tras pausa
            )
            .setBackBuffer(10_000, true)
            .build()

        /**
         * Configuración de Red con Autenticación JWT:
         * Inyectamos el token de seguridad directamente en las propiedades de la petición HTTP.
         * Esto previene errores 403 (Forbidden) al acceder a los recursos de streaming protegidos.
         */
        val token = tokenManager.getToken()
        val headers = if (token != null) {
            mapOf("Authorization" to "Bearer $token")
        } else {
            emptyMap()
        }

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(15_000)
            .setDefaultRequestProperties(headers)

        val extractorsFactory = DefaultExtractorsFactory()
        val renderersFactory = DefaultRenderersFactory(this)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)

        /**
         * Construcción de la instancia de ExoPlayer.
         * Se integra la factoría de fuentes de datos personalizada con soporte para JWT.
         */
        player = ExoPlayer.Builder(this)
            .setRenderersFactory(renderersFactory)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(this, extractorsFactory)
                    .setDataSourceFactory(dataSourceFactory)
            )
            .setLoadControl(loadControl)
            .build()

        // Vinculación del Player con la Sesión Multimedia
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(MediaSessionCallback())
            .build()
    }

    /**
     * El sistema invoca este método para obtener la sesión cuando un controlador externo se conecta.
     */
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    /**
     * Liberación de recursos críticos para evitar fugas de memoria (Memory Leaks).
     */
    override fun onDestroy() {
        mediaSession?.run {
            player.stop()
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    /**
     * MediaSessionCallback: Intercepta comandos y cambios en la lista de reproducción.
     */
    private inner class MediaSessionCallback : MediaSession.Callback {

        /**
         * Se dispara cuando se añaden nuevos elementos a la cola.
         * Aquí resolvemos la URL de streaming final basada en el mediaId de la canción.
         */
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<List<MediaItem>> {

            val updatedItems = mediaItems.map { item ->
                if (item.localConfiguration?.uri == null || item.localConfiguration?.uri == Uri.EMPTY) {
                    val songId = item.mediaId.toLongOrNull() ?: -1L
                    val streamUrl = musicRepository.getStreamUrl(songId)

                    item.buildUpon()
                        .setUri(Uri.parse(streamUrl))
                        .setMediaMetadata(item.mediaMetadata)
                        .build()
                } else {
                    item
                }
            }

            return Futures.immediateFuture(updatedItems)
        }

        @OptIn(UnstableApi::class)
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            // Aceptamos la conexión y otorgamos todos los comandos de control disponibles
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon().build()
            val playerCommands = Player.Commands.Builder().addAllCommands().build()
            return MediaSession.ConnectionResult.accept(sessionCommands, playerCommands)
        }
    }
}