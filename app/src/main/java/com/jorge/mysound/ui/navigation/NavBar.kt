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
import androidx.navigation.compose.rememberNavController
import com.jorge.mysound.ui.theme.MySoundTheme

// Asegúrate de tener tu clase Screen definida en algún sitio
// import com.jorge.mysound.ui.navigation.Screen

@Composable
fun SpotifyNavBar(navController: NavHostController) {

    // Si esto te da error, asegúrate de tener la clase "Screen" creada
    val screens = listOf(
        Screen.Home,
        Screen.Search,
        Screen.Library,
        Screen.Profile
    )

    // HE BORRADO LA LÍNEA DE AuthViewModel.authState PORQUE AQUÍ NO PINTA NADA
    // Y DABA ERROR DE COMPILACIÓN.

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Surface(
        color = Color.Black.copy(alpha = 0.05f),
        modifier = Modifier.fillMaxWidth()
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            modifier = Modifier.height(80.dp),
            tonalElevation = 0.dp
        ) {
            screens.forEach { screen ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                NavigationBarItem(
                    selected = isSelected,
                    // PON LA LÓGICA DE NAVEGACIÓN AQUÍ, NO EN EL ICONO
                    onClick = {
                        if (!isSelected) {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
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
}

// ... tus previews están bien ...