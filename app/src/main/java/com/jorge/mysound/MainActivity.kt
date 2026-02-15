package com.jorge.mysound

import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.ui.tooling.preview.Preview
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
import com.jorge.mysound.ui.navigation.SpotifyNavBar
import com.jorge.mysound.ui.theme.MySoundTheme
import com.jorge.mysound.ui.viewmodels.PlayerViewModel
import kotlin.getValue

class MainActivity : ComponentActivity() {

    private lateinit var sessionToken: SessionToken
    private lateinit var controllerFuture: ListenableFuture<MediaController>

    // 1. INYECCIÓN DE DEPENDENCIAS MANUAL (Nivel Pro)
    // Creamos la fábrica UNA vez y la reusamos.
    private val appViewModelFactory by lazy {
        val context = applicationContext
        val apiService = RetrofitClient.getInstance(context)
        val tokenManager = TokenManager(context)
        val repository = MusicRepository(apiService)

        AppViewModelFactory(repository, tokenManager, apiService)
    }

    // 2. VIEWMODELS
    // Usamos nuestra fábrica para que tengan Repository y TokenManager dentro
    private val playerViewModel: PlayerViewModel by viewModels { appViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MySoundTheme {
                // 3. NAVEGACIÓN
                // Pasamos la fábrica y el playerViewModel (que es el que conectaremos al servicio)
                AppNavigation(
                    factory = appViewModelFactory,
                    playerViewModel = playerViewModel
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Esta lógica estaba PERFECTA, gordo. No la toques.
        sessionToken = SessionToken(this, ComponentName(this, MediaPlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()

        controllerFuture.addListener({
            try {
                val controller = controllerFuture.get()
                // LE PASAMOS EL CONTEXTO EXPLÍCITAMENTE
                playerViewModel.setController(controller, applicationContext)
                Log.d("DEBUG_PLAYER", "¡CONECTADO!")
            } catch (e: Exception) {
                Log.e("DEBUG_PLAYER", "Error al conectar", e)
            }
        }, MoreExecutors.directExecutor())
    }

    override fun onStop() {
        MediaController.releaseFuture(controllerFuture)
        super.onStop()
    }
}