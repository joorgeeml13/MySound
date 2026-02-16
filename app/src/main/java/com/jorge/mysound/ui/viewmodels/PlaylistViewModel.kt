package com.jorge.mysound.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jorge.mysound.data.remote.Playlist
import com.jorge.mysound.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaylistViewModel(private val repository: MusicRepository) : ViewModel() {
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists = _playlists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadPlaylists()
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _playlists.value = repository.getPlaylists()
            } catch (e: Exception) {
                Log.e("DEBUG_PLAYLIST", "Error cargando listas: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createNewPlaylist(name: String, description: String) {
        viewModelScope.launch {
            try {
                repository.createPlaylist(name, description)
                loadPlaylists() // Refrescamos
            } catch (e: Exception) {
                Log.e("DEBUG_PLAYLIST", "Error creando lista: ${e.message}")
            }
        }
    }

    class Factory(private val repository: MusicRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlaylistViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PlaylistViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}