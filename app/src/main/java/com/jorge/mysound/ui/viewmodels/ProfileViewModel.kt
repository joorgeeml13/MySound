package com.jorge.mysound.ui.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jorge.mysound.data.remote.UserProfile
import com.jorge.mysound.data.repository.MusicRepository
import com.jorge.mysound.util.uriToFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ProfileViewModel: Gestiona la lógica de negocio relacionada con el perfil del usuario.
 * Se encarga de la recuperación de datos biográficos, estadísticas y la actualización
 * del avatar mediante peticiones multipart.
 */
class ProfileViewModel(private val repository: MusicRepository) : ViewModel() {

    // Estado reactivo que contiene la información del perfil del usuario
    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile = _profile.asStateFlow()

    // Indicador de carga para gestionar el feedback visual en la interfaz (Shimmers/Spinners)
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    /**
     * Recupera la información completa del perfil desde el repositorio.
     * @param userId Identificador único del usuario.
     */
    fun fetchProfile(userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true

            // Ejecución de la petición asíncrona mediante el repositorio
            repository.getUserProfile(userId).onSuccess { data ->
                _profile.value = data
                Log.d("ProfileVM", "Perfil cargado exitosamente para el usuario: ${data.username}")
            }.onFailure { exception ->
                Log.e("ProfileVM", "Error al recuperar el perfil: ${exception.message}")
            }

            _isLoading.value = false
        }
    }

    /**
     * Gestiona el proceso de actualización de la imagen de perfil del usuario.
     * Realiza la conversión de URI a archivo físico y coordina la subida al servidor.
     * * @param context Contexto necesario para acceder al ContentResolver.
     * @param uri URI de la imagen seleccionada desde el proveedor de contenidos.
     * @param userId Identificador del usuario que actualiza su imagen.
     */
    fun updateUserAvatar(context: Context, uri: Uri, userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                /**
                 * Delegamos la conversión de la URI a un archivo físico en la utilidad FileUtils.
                 * Esto mantiene el ViewModel limpio de lógica de manipulación de archivos.
                 */
                val file = uriToFile(context, uri)

                if (file != null) {
                    // Procedemos con la subida a través del repositorio
                    val result = repository.updateAvatar(userId, file)

                    result.onSuccess { newUrl ->
                        // Actualización atómica del estado local para reflejar el cambio inmediatamente
                        _profile.update { currentProfile ->
                            currentProfile?.copy(avatarUrl = newUrl)
                        }
                        Log.d("ProfileVM", "Avatar actualizado correctamente. Nueva URL: $newUrl")
                    }.onFailure { exception ->
                        Log.e("ProfileVM", "Error en la subida al repositorio: ${exception.message}")
                    }
                } else {
                    Log.e("ProfileVM", "No se pudo procesar el archivo de imagen a partir de la URI")
                }

            } catch (e: Exception) {
                Log.e("ProfileVM", "Excepción inesperada durante la actualización del avatar: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}