package com.jorge.mysound.ui.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState


@Composable
fun SpotifyNavBar(navController: NavHostController) {

    // Si esto te da error, asegúrate de tener la clase "Screen" creada
    val screens = listOf(
        Screen.Home,
        Screen.Search,
        Screen.Library,
        Screen.Profile
    )


    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination


    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.height(80.dp),
        tonalElevation = 0.dp
    ) {
        screens.forEach { screen ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true ||
                    // Caso 1: Estamos en el detalle de una playlist -> Mantiene encendido "Library"
                    (screen.route == Screen.Library.route && currentDestination?.route?.contains("playlist_detail") == true) ||
                    // Caso 2: Estamos en Ajustes o Editar Perfil -> Mantiene encendido "Profile"
                    (screen.route == Screen.Profile.route && (
                            currentDestination?.route?.contains("settings") == true ||
                                    currentDestination?.route?.contains("edit_profile") == true
                            ))

            NavigationBarItem(
                selected = isSelected,
                // PON LA LÓGICA DE NAVEGACIÓN AQUÍ, NO EN EL ICONO
                onClick = {
                    if (!isSelected) {
                        // Navegación normal cambiando de pestaña
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = (screen.route != Screen.Library.route)
                        }
                    } else if (screen.route == Screen.Library.route) {
                        // 🔥 Si ya estamos en Library (o en su detalle) y clicamos el icono:
                        // Limpiamos el stack hasta volver a la lista de Library
                        navController.popBackStack(Screen.Library.route, inclusive = false)
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(screen.icon),
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                        // He quitado el .clickable() de aquí dentro.
                        // El NavigationBarItem ya maneja el click y queda más limpio.
                    )
                },
                label = null,
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
