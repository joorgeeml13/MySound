package com.jorge.mysound.ui.navigation

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.jorge.mysound.ui.AppViewModelFactory
import com.jorge.mysound.ui.screens.MainScreen
import com.jorge.mysound.ui.screens.auth.LoginScreen
import com.jorge.mysound.ui.screens.auth.RegisterScreen
import com.jorge.mysound.ui.viewmodels.AuthViewModel
import com.jorge.mysound.ui.viewmodels.AuthState
import com.jorge.mysound.ui.viewmodels.PlayerViewModel

@Composable
fun AppNavigation(
    factory: AppViewModelFactory,
    playerViewModel: PlayerViewModel
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val authState by authViewModel.authState.collectAsState()

    // Lógica de redirección automática si el login es Success
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            navController.navigate("main") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = "login") {

        // PANTALLA DE LOGIN
        composable("login") {
            LoginScreen(
                onLoginClick = { user, pass, rememberMe ->
                    // AQUÍ ESTÁN LOS 3 PARÁMETROS, GORDO
                    authViewModel.login(user, pass, rememberMe)
                },
                onRegisterClick = { navController.navigate("register") }
            )
        }

        // PANTALLA DE REGISTRO
        composable("register") {
            RegisterScreen(
                onRegisterClick = { user, email, birth, pass ->
                    Log.d("DEBUG_AUTH", "Navegación capturó el click. Llamando al VM...")
                    authViewModel.register(email, pass, user, birth)
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        // PANTALLA PRINCIPAL (Home, Search, etc.)
        composable("main") {
            MainScreen(
                playerViewModel = playerViewModel,
                searchViewModel = viewModel(factory = factory)
            )
        }
    }
}