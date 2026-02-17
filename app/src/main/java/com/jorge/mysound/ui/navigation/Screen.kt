package com.jorge.mysound.ui.navigation

import androidx.annotation.DrawableRes
import com.jorge.mysound.R


sealed class Screen(
    val route: String,
    val title: String,
    @DrawableRes val icon: Int
) {
    object Home : Screen(
        route = "home",
        title = "Inicio",
        icon = R.drawable.ic_home,
    )

    object Search : Screen(
        route = "search",
        title = "Buscar",
        icon = R.drawable.ic_search,
    )

    object Library : Screen(
        route = "library",
        title = "Tu biblioteca",
        icon = R.drawable.ic_library,
    )

    object Profile : Screen(
        route = "profile",
        title = "Perfil",
        icon = R.drawable.ic_profile,
    )
}