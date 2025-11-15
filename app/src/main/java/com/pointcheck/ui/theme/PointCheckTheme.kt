package com.pointcheck.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Paleta de colores para el modo claro
private val LightColors = lightColorScheme(
    primary = MetallicBlue,
    onPrimary = White,
    background = LightBackground,
    surface = White
)

// Paleta de colores para el modo oscuro
private val DarkColors = darkColorScheme(
    primary = MetallicBlue,
    onPrimary = White,
    background = DarkBackground,
    surface = DarkBackground
)

@Composable
fun PointCheckTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes,       //  formas
        content = content
    )
}
