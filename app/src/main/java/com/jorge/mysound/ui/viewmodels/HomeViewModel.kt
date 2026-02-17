package com.jorge.mysound.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jorge.mysound.R
import com.jorge.mysound.data.remote.Playlist
import com.jorge.mysound.data.remote.UserProfile
import com.jorge.mysound.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * HomeState: Clase de datos que representa el estado atómico de la pantalla de inicio.
 * Sigue el patrón de Unidirectional Data Flow (UDF) para facilitar la reactividad en Compose.
 * * @param playlists Lista de colecciones destacadas para el usuario.
 * @param matchedUser Información del perfil del usuario con gustos similares (Algoritmo de Match).
 * @param isLoading Indicador de estado para la gestión de feedback visual (Shimmers/Spinners).
 * @param errorMessageId ID del recurso de cadena para mensajes de error localizados.
 */
data class HomeState(
    val playlists: List<Playlist> = emptyList(),
    val matchedUser: UserProfile? = null,
    val isLoading: Boolean = false,
    val errorMessageId: Int? = null
)

/**
 * HomeViewModel: Orquestador de la lógica de negocio para la pantalla principal.
 * Gestiona la carga concurrente de datos del repositorio y la transformación del estado de la UI.
 */
class HomeViewModel(private val repository: MusicRepository) : ViewModel() {

    // Flujo de estado interno (mutable) para la gestión privada del ViewModel
    private val _homeState = MutableStateFlow(HomeState())

    // Flujo de estado público (inmutable) expuesto a la interfaz de usuario
    val homeState = _homeState.asStateFlow()

    /**
     * Carga de forma asíncrona la información requerida para la pantalla de inicio.
     * Utiliza viewModelScope para garantizar la cancelación de la petición si se destruye el ViewModel.
     */
    fun loadHomeData() {
        viewModelScope.launch {
            // Activación del estado de carga y limpieza de errores previos
            _homeState.update { it.copy(isLoading = true, errorMessageId = null) }

            try {
                // Petición al repositorio para obtener el consolidado de datos (Match + Playlists)
                val data = repository.getHomeData()

                if (data != null) {
                    // Actualización exitosa del estado con la información del servidor
                    _homeState.update {
                        it.copy(
                            playlists = data.playlists,
                            matchedUser = data.matchedUser,
                            isLoading = false
                        )
                    }
                } else {
                    // Gestión de respuesta vacía o nula por parte del servidor
                    _homeState.update {
                        it.copy(
                            isLoading = false,
                            errorMessageId = R.string.error_home_no_data
                        )
                    }
                }
            } catch (e: Exception) {
                // Captura de excepciones de red o parsing de datos
                Log.e("HomeViewModel", "Excepción durante la carga de datos: ${e.message}")
                _homeState.update {
                    it.copy(
                        isLoading = false,
                        errorMessageId = R.string.error_home_generic
                    )
                }
            }
        }
    }
}