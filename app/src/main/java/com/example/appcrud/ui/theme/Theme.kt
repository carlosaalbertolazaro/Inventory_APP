package com.example.appcrud.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AppCrudColorScheme = lightColorScheme(
    primary = Color(0xFF8F031D),
    secondary = Color(0xFF03DAC5),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    error = Color(0xFFB00020)
)

val DarkAppCrudColorScheme = darkColorScheme(
    primary = Color(0xFF9F0041),
    secondary = Color(0xFFDA0303),
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    error = Color(0xFFCF6679)
)

@Composable
fun AppCrudTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkAppCrudColorScheme else AppCrudColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}