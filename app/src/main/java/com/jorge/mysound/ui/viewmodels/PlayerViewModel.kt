package com.jorge.mysound.ui.viewmodels


import androidx.lifecycle.ViewModel
import androidx.media3.session.MediaController
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class PlayerViewModel : ViewModel() {
    // El controlador es una "promesa" (Future) porque tarda un pelín en conectarse
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val controller: MediaController? get() = if (controllerFuture?.isDone == true) controllerFuture?.get() else null

    // Este Flow se actualizará cuando el controlador nos diga que el estado cambió
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    fun playPause() {
        val player = controller ?: return // Si no hay mando, no hacemos nada
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }
}