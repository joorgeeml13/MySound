package com.jorge.mysound.ui.screens.main

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun SearchScreen(){
    Text(
        "Search",
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.headlineMedium
    )
}