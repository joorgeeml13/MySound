package com.jorge.mysound

import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.jorge.mysound.util.TokenManager
import com.jorge.mysound.data.remote.RetrofitClient
import com.jorge.mysound.data.repository.MusicRepository
import com.jorge.mysound.service.MediaPlaybackService
import com.jorge.mysound.ui.AppViewModelFactory
import com.jorge.mysound.ui.navigation.AppNavigation
import com.jorge.mysound.ui.screens.auth.LoginScreen
import com.jorge.mysound.ui.screens.auth.RegisterScreen
import com.jorge.mysound.ui.theme.MySoundTheme
import com.jorge.mysound.ui.viewmodels.AuthState
import com.jorge.mysound.ui.viewmodels.AuthViewModel
import com.jorge.mysound.ui.viewmodels.PlayerViewModel
import com.jorge.mysound.ui.viewmodels.SettingsViewModel

/**
 * MainActivity: Punto de entrada principal de la aplicación.
 * Gestiona el ciclo de vida de la sesión de medios, la autenticación y el tema global.
 */
class MainActivity : AppCompatActivity() {

    // Gestión de tokens y controladores para la reproducción multimedia (Media3)
    private lateinit var sessionToken: SessionToken
    private lateinit var controllerFuture: ListenableFuture<MediaController>

    /**
     * Inyección de dependencias manual mediante un patrón Factory.
     * Centralizamos la creación de servicios y repositorios para asegurar una única instancia.
     */
    private val appViewModelFactory by lazy {
        val context = applicationContext
        val apiService = RetrofitClient.getInstance(context)
        val tokenManager = TokenManager(context)
        val repository = MusicRepository(apiService)

        AppViewModelFactory(repository, tokenManager, apiService)
    }

    // Inicialización de ViewModels compartidos a nivel de Activity
    private val playerViewModel: PlayerViewModel by viewModels { appViewModelFactory }
    private val settingsViewModel: SettingsViewModel by viewModels { appViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilitar diseño de borde a borde (Edge-to-Edge) para una UI moderna
        enableEdgeToEdge()

        setContent {
            // Observamos el estado del modo oscuro desde el SettingsViewModel
            val isDark by settingsViewModel.isDarkMode.collectAsState()

            // Estado local para alternar entre pantallas de Login y Registro
            var isRegistering by remember { mutableStateOf(false) }

            MySoundTheme(darkTheme = isDark) {
                // ViewModel de autenticación con factoría personalizada
                val authViewModel: AuthViewModel = viewModel(factory = appViewModelFactory)
                val authState by authViewModel.authState.collectAsState()

                /**
                 * Flujo de navegación condicional basado en el estado de autenticación (AuthState).
                 */
                when (authState) {
                    is AuthState.Success -> {
                        // El usuario está autenticado correctamente
                        AppNavigation(
                            factory = appViewModelFactory,
                            playerViewModel = playerViewModel,
                            onLogout = {
                                authViewModel.logout()
                            }
                        )
                    }

                    is AuthState.Loading -> {
                        // Feedback visual mientras se valida el token o se realiza el login
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    else -> {
                        // Pantallas de acceso para usuarios no autenticados
                        if (isRegistering) {
                            RegisterScreen(
                                onBackToLogin = { isRegistering = false },
                                onRegisterClick = { email, pass, date, username ->
                                    authViewModel.register(email, pass, username, date)
                                    isRegistering = false
                                }
                            )
                        } else {
                            LoginScreen(
                                onLoginClick = { email, pass, rememberMe ->
                                    authViewModel.login(email, pass, rememberMe)
                                },
                                onRegisterClick = {
                                    isRegistering = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Al iniciar la actividad, establecemos la conexión con el MediaPlaybackService.
     */
    override fun onStart() {
        super.onStart()
        // Creación del token de sesión vinculado al servicio de reproducción
        sessionToken = SessionToken(this, ComponentName(this, MediaPlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()

        // Listener para capturar el controlador cuando la conexión sea exitosa
        controllerFuture.addListener({
            try {
                val controller = controllerFuture.get()
                // Vinculamos el controlador de Media3 con el ViewModel de reproducción
                playerViewModel.setController(controller, applicationContext)
                Log.d("MEDIA_SESSION", "Controlador de medios conectado correctamente")
            } catch (e: Exception) {
                Log.e("MEDIA_SESSION", "Error al inicializar el controlador de medios", e)
            }
        }, MoreExecutors.directExecutor())
    }

    /**
     * Al detener la actividad, liberamos el controlador para evitar fugas de memoria.
     */
    override fun onStop() {
        MediaController.releaseFuture(controllerFuture)
        super.onStop()
    }
}