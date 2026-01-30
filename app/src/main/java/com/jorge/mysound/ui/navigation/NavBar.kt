package com.jorge.mysound.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.jorge.mysound.ui.theme.MySoundTheme

@Composable
fun SpotifyNavBar(navController: NavHostController) {
    val screens = listOf(
        Screen.Home,
        Screen.Search,
        Screen.Library
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = Color(0xCC121212),
        contentColor = Color.White
    ) {
        screens.forEach { screen ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = screen.icon),
                        contentDescription = screen.title,
                        modifier = Modifier.size(24.dp),

                        tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                },
                selected = isSelected,
                onClick = {

                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent // Evita ese círculo feo de Material 3
                )
            )
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