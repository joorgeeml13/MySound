package com.jorge.mysound.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jorge.mysound.util.TokenManager
import com.jorge.mysound.data.remote.MusicApiService
import com.jorge.mysound.data.repository.MusicRepository
import com.jorge.mysound.ui.viewmodels.AuthViewModel
import com.jorge.mysound.ui.viewmodels.PlayerViewModel
import com.jorge.mysound.ui.viewmodels.SearchViewModel

class AppViewModelFactory(
    val repository: MusicRepository,
    private val tokenManager: TokenManager,
    private val api: MusicApiService
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            // 1. Para el Login y Registro
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(api, tokenManager) as T
            }
            // 2. Para el Reproductor (Play, Pause, etc)
            modelClass.isAssignableFrom(PlayerViewModel::class.java) -> {
                PlayerViewModel(repository) as T
            }
            // 3. Para buscar canciones
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                SearchViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}