package com.jorge.mysound.ui.screens.main

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun HomeScreen(){
    Text(
        "Home",
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.headlineMedium
    )
}