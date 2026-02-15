package com.jorge.mysound.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jorge.mysound.util.TokenManager
import com.jorge.mysound.data.remote.LoginRequest
import com.jorge.mysound.data.remote.MusicApiService
import com.jorge.mysound.data.remote.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val api: MusicApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    // Usamos NUESTRA propia clase AuthState (definida abajo)
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        if (tokenManager.getToken() != null) {
            _authState.value = AuthState.Success
        }
    }

    fun login(email: String, pass: String, rememberMe: Boolean) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = api.login(LoginRequest(email, pass))

                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.token
                    tokenManager.saveToken(token, rememberMe)
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error("Credenciales incorrectas, bro.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error de red: ${e.message}")
            }
        }
    }

    fun register(email: String, pass: String, username: String, birthDate: String) {
        Log.d("DEBUG_AUTH", "ViewModel recibió: Email=$email, User=$username, Date=$birthDate")

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                Log.d("DEBUG_AUTH", "Llamando a la API de Retrofit...")
                val request = RegisterRequest(email, pass, username, birthDate)
                val response = api.register(request)

                Log.d("DEBUG_AUTH", "Respuesta recibida. Código: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.token
                    tokenManager.saveToken(token, true)
                    _authState.value = AuthState.Success
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("DEBUG_AUTH", "Fallo en servidor: $errorMsg")
                    _authState.value = AuthState.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("DEBUG_AUTH", "EXCEPTION lanzada: ${e.message}")
                e.printStackTrace()
                _authState.value = AuthState.Error("Error de red: ${e.message}")
            }
        }
    }

    fun logout() {
        tokenManager.clearToken()
        _authState.value = AuthState.Idle
    }
}

// --- ESTO ES LO QUE TE FALTABA O TENÍAS MAL IMPORTADO ---
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}