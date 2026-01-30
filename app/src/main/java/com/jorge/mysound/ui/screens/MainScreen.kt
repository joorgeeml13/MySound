package com.jorge.mysound.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jorge.mysound.ui.navigation.Screen
import com.jorge.mysound.ui.navigation.SpotifyNavBar
import com.jorge.mysound.ui.screens.main.HomeScreen
import com.jorge.mysound.ui.screens.main.LibraryScreen
import com.jorge.mysound.ui.screens.main.SearchScreen

@Composable
fun MainScreen(){
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { SpotifyNavBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Search.route) { SearchScreen() }
            composable(Screen.Library.route) { LibraryScreen() }
        }
    }
}