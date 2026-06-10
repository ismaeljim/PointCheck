package com.pointcheck.core.ui.theme

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

/**
 * Configuración del tema principal de la aplicación PointCheck.
 * Implementa el sistema de diseño Material3 con soporte para modos claro y oscuro.
 * 
 * @param darkTheme Indica si se debe aplicar el esquema de colores oscuro.
 * @param content Composable que se renderizará bajo este tema.
 */
@Composable
fun PointCheckTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
