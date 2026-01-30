package com.jorge.mysound.ui.screens.main

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.jorge.mysound.R

@Composable
fun LibraryScreen(){
    Text(
        "Library",
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.headlineMedium
    )
}