package com.jorge.mysound.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jorge.mysound.data.remote.Playlist
import com.jorge.mysound.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * PlaylistViewModel: Gestiona el ciclo de vida de las listas de reproducción.
 * Se encarga de la sincronización entre el repositorio de datos y la interfaz de usuario,
 * manejando operaciones de creación, recuperación y edición de colecciones musicales.
 */
class PlaylistViewModel(private val repository: MusicRepository) : ViewModel() {

    // Flujo de estado para la colección de listas de reproducción
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists = _playlists.asStateFlow()

    // Estado de carga para proporcionar feedback visual al usuario
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    /**
     * Recupera todas las listas de reproducción disponibles de forma asíncrona.
     */
    fun loadPlaylists() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getPlaylists()
                _playlists.value = result
            } catch (e: Exception) {
                Log.e("PlaylistVM", "Error al recuperar colecciones globales: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Obtiene las listas de reproducción pertenecientes a un usuario específico.
     * @param userId Identificador del usuario propietario.
     */
    fun loadUserPlaylists(userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getUserPlaylists(userId)
                _playlists.value = result
            } catch (e: Exception) {
                Log.e("PlaylistVM", "Error al cargar listas del usuario $userId: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Crea una nueva lista de reproducción y actualiza el estado local tras el éxito.
     * @param name Nombre de la nueva lista.
     * @param description Breve descripción de la colección.
     * @param userId ID del usuario para disparar el refresco automático.
     */
    fun createNewPlaylist(name: String, description: String, userId: Long) {
        viewModelScope.launch {
            try {
                val result = repository.createPlaylist(name, description)
                    // Refrescamos la vista para incluir la nueva creación inmediatamente
                    loadUserPlaylists(userId)
                    Log.d("PlaylistVM", "Nueva lista creada satisfactoriamente: $name")

            } catch (e: Exception) {
                Log.e("PlaylistVM", "Fallo en la creación de la lista de reproducción: ${e.message}")
            }
        }
    }

    /**
     * Vincula una canción específica a una lista de reproducción existente.
     * @param playlistId Identificador de la lista de destino.
     * @param songId Identificador de la canción a añadir.
     */
    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.addSongToPlaylist(playlistId, songId)

                Log.d("PlaylistVM", "Canción $songId integrada con éxito en la lista $playlistId")
            } catch (e: Exception) {
                Log.e("PlaylistVM", "Error al vincular canción a la colección: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}