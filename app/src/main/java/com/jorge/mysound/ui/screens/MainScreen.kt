package com.jorge.mysound.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jorge.mysound.ui.components.SpotifyMiniPlayer
import com.jorge.mysound.ui.navigation.Screen
import com.jorge.mysound.ui.navigation.SpotifyNavBar
import com.jorge.mysound.ui.screens.main.HomeScreen
import com.jorge.mysound.ui.screens.main.LibraryScreen
import com.jorge.mysound.ui.screens.main.ProfileScreen
import com.jorge.mysound.ui.screens.main.SearchScreen

@Composable
fun MainScreen(){
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SpotifyMiniPlayer() // El reproductor arriba
                SpotifyNavBar(navController) // La barra abajo
            }
                    },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Search.route) { SearchScreen() }
            composable(Screen.Library.route) { LibraryScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
        }
    }
}