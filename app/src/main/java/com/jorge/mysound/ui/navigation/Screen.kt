package com.jorge.mysound.ui.navigation

import com.jorge.mysound.R

sealed class Screen(val route: String, val title: String, val icon: Int) {
    object Home : Screen("home", "Inicio", R.drawable.ic_home)
    object Search : Screen("search", "Buscar", R.drawable.ic_search)
    object Library : Screen("library", "Biblioteca", R.drawable.ic_library)
}