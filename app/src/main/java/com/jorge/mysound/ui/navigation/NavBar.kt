package com.jorge.mysound.ui.navigation

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember



@Composable
fun SpotifyNavBar(navController: NavHostController) {

    val screens = listOf(
        Screen.Home,
        Screen.Search,
        Screen.Library,
        Screen.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Surface(
        color =  Color.Black.copy(alpha = 0.05f),
        modifier = Modifier.fillMaxWidth()
    ) {
        NavigationBar(
            containerColor = Color.Transparent, // Ahora sí deja ver el Surface
            modifier = Modifier.height(80.dp),
            tonalElevation = 0.dp
        ) {
            screens.forEach { screen ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                val interactionSource = remember { MutableInteractionSource() }

                NavigationBarItem(
                    selected = isSelected,

                    onClick = { },
                    interactionSource = interactionSource,
                    icon = {
                        Icon(
                            painter = painterResource(screen.icon),
                            contentDescription = null,
                            modifier = Modifier.size(26.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null, // ADIÓS RIPPLE, ADIÓS SOMBRA, ADIÓS TODO
                                    onClick = {
                                        if (!isSelected) { // Solo navega si no estás ya ahí
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                )
                        )
                    },
                    label = null,
                    alwaysShowLabel = false,

                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,

                        indicatorColor = Color.Transparent // Mata la píldora de fondo
                    )
                )
            }
        }
    }


}

@Preview(showBackground = true, name = "Spotify Menu Dark Preview")
@Composable
fun SpotifyNavBarPreviewBlack() {
    val dummyNavController = rememberNavController()

    MySoundTheme (darkTheme = true) {
        SpotifyNavBar(navController = dummyNavController)
    }
}

@Preview(showBackground = true, name = "Spotify Menu Preview")
@Composable
fun SpotifyNavBarPreview() {
    val dummyNavController = rememberNavController()

    MySoundTheme (darkTheme = false) {
        SpotifyNavBar(navController = dummyNavController)
    }
}