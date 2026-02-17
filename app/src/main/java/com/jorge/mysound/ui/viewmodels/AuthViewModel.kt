package com.jorge.mysound.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jorge.mysound.R
import com.jorge.mysound.data.remote.LoginRequest
import com.jorge.mysound.data.remote.MusicApiService
import com.jorge.mysound.data.remote.RegisterRequest
import com.jorge.mysound.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * AuthViewModel: Gestiona el ciclo de vida de la autenticación del usuario.
 * Coordina las peticiones de Login y Registro con el backend y administra
 * la persistencia del token JWT mediante el [TokenManager].
 */
class AuthViewModel(
    private val api: MusicApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    // Estado reactivo de la autenticación expuesto como un flujo inmutable
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    // ID del recurso de cadena para el mensaje de error (soporte multi-idioma)
    private val _errorResId = MutableStateFlow<Int?>(null)
    val errorResId = _errorResId.asStateFlow()

    init {
        // Validación inicial del estado de la sesión al instanciar el ViewModel
        checkAuthStatus()
    }

    /**
     * Verifica si existe un token persistido para determinar el estado de acceso inicial.
     */
    private fun checkAuthStatus() {
        if (tokenManager.getToken() != null) {
            _authState.value = AuthState.Success
        }
    }

    /**
     * Ejecuta el proceso de inicio de sesión de forma asíncrona.
     * @param email Correo electrónico del usuario.
     * @param pass Contraseña.
     * @param rememberMe Indica si el token debe persistirse en disco.
     */
    fun login(email: String, pass: String, rememberMe: Boolean) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _errorResId.value = null

            try {
                val response = api.login(LoginRequest(email, pass))

                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.token
                    tokenManager.saveToken(token, rememberMe)
                    _authState.value = AuthState.Success
                } else {
                    // Gestión de errores de lógica de negocio (ej. credenciales incorrectas)
                    _errorResId.value = R.string.error_auth_invalid
                    _authState.value = AuthState.Error(R.string.error_auth_invalid)
                }
            } catch (e: Exception) {
                // Gestión de excepciones de red
                Log.e("AuthViewModel", "Error en login: ${e.message}")
                _errorResId.value = R.string.error_auth_network
                _authState.value = AuthState.Error(R.string.error_auth_network)
            }
        }
    }

    /**
     * Registra un nuevo usuario en la plataforma.
     */
    fun register(email: String, pass: String, username: String, birthDate: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _errorResId.value = null

            try {
                val request = RegisterRequest(email, pass, username, birthDate)
                val response = api.register(request)

                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.token
                    // Por defecto, persistimos el token tras un registro exitoso
                    tokenManager.saveToken(token, true)
                    _authState.value = AuthState.Success
                } else {
                    _errorResId.value = R.string.error_auth_unknown
                    _authState.value = AuthState.Error(R.string.error_auth_unknown)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error en registro: ${e.message}")
                _errorResId.value = R.string.error_auth_network
                _authState.value = AuthState.Error(R.string.error_auth_network)
            }
        }
    }

    /**
     * Finaliza la sesión del usuario eliminando el token y reseteando el estado.
     */
    fun logout() {
        tokenManager.clearToken()
        _authState.value = AuthState.Idle
    }

    /**
     * Intenta extraer el mensaje de error estructurado del cuerpo de la respuesta HTTP.
     */
    private fun parseError(response: retrofit2.Response<*>): String {
        return try {
            val errorJson = response.errorBody()?.string()
            val jsonObject = JSONObject(errorJson ?: "{}")
            jsonObject.optString("message", "Error desconocido")
        } catch (e: Exception) {
            "Error inesperado"
        }
    }
}

/**
 * AuthState: Representación sellada (Sealed Class) de los posibles estados de autenticación.
 * Facilita el manejo exhaustivo de estados en la interfaz de usuario mediante Compose.
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val messageResId: Int) : AuthState()
}