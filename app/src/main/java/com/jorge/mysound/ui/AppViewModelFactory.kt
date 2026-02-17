package com.jorge.mysound.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jorge.mysound.util.TokenManager
import com.jorge.mysound.data.remote.MusicApiService
import com.jorge.mysound.data.repository.MusicRepository
import com.jorge.mysound.ui.viewmodels.AuthViewModel
import com.jorge.mysound.ui.viewmodels.HomeViewModel
import com.jorge.mysound.ui.viewmodels.PlayerViewModel
import com.jorge.mysound.ui.viewmodels.PlaylistViewModel
import com.jorge.mysound.ui.viewmodels.ProfileViewModel
import com.jorge.mysound.ui.viewmodels.SearchViewModel
import com.jorge.mysound.ui.viewmodels.SettingsViewModel

/**
 * AppViewModelFactory: Clase encargada de la inyección de dependencias manual.
 * Implementa [ViewModelProvider.Factory] para permitir la creación de ViewModels
 * que requieren parámetros específicos en su constructor.
 */
class AppViewModelFactory(
     val repository: MusicRepository,
     val tokenManager: TokenManager,
     val api: MusicApiService
) : ViewModelProvider.Factory {

    /**
     * Método principal que instancia el ViewModel solicitado basándose en su clase.
     * Implementa un patrón de "Service Locator" para resolver las dependencias necesarias.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            // Gestión de Autenticación (Login/Registro)
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(api, tokenManager) as T
            }

            // Lógica de Reproducción Multimedia
            modelClass.isAssignableFrom(PlayerViewModel::class.java) -> {
                PlayerViewModel(repository) as T
            }

            // Módulo de Búsqueda de Canciones
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                SearchViewModel(repository) as T
            }

            // Gestión de Listas de Reproducción
            modelClass.isAssignableFrom(PlaylistViewModel::class.java) -> {
                PlaylistViewModel(repository) as T
            }

            // Perfil de Usuario y Estadísticas
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(repository) as T
            }

            // Lógica de la Pantalla Principal (Match y Playlists sugeridas)
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository) as T
            }

            // Configuración Global y Preferencias (Modo Oscuro, Idioma)
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(repository) as T
            }

            // Excepción en caso de solicitar un ViewModel no registrado en la Factory
            else -> throw IllegalArgumentException("Error: Clase ViewModel desconocida: ${modelClass.name}")
        }
    }
}