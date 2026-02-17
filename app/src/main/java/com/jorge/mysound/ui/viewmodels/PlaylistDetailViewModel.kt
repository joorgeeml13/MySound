package com.jorge.mysound.ui.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jorge.mysound.data.remote.Playlist
import com.jorge.mysound.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import androidx.lifecycle.ViewModelProvider

class PlaylistDetailViewModelFactory(
    private val repository: MusicRepository,
    private val playlistId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PlaylistDetailViewModel(repository, playlistId) as T
    }
}

class PlaylistDetailViewModel(
    private val repository: MusicRepository,
    private val playlistId: Long
) : ViewModel() {

    private val _playlist = MutableStateFlow<Playlist?>(null)
    val playlist = _playlist.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadPlaylist()
    }

    fun loadPlaylist() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _playlist.value = repository.getPlaylistById(playlistId)

            } catch (e: Exception) {
                Log.e("DEBUG_DETAIL", "Error: ${e.message}")
            }finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadPlaylistImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Convertimos URI a File usando el helper
                val file = com.jorge.mysound.util.uriToFile(context, uri)

                if (file != null) {
                    Log.d("DEBUG_UPLOAD", "Archivo creado: ${file.path}")

                    // 2. Subimos al servidor
                    repository.uploadPlaylistImage(playlistId, file)

                    // 3. Recargamos la playlist para ver la foto nueva
                    loadPlaylist()

                } else {
                    Log.e("DEBUG_UPLOAD", "No se pudo convertir la URI a File")
                }
            } catch (e: Exception) {
                Log.e("DEBUG_UPLOAD", "Error subiendo: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    class Factory(
        private val repository: MusicRepository,
        private val playlistId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlaylistDetailViewModel(repository, playlistId) as T
        }
    }
}