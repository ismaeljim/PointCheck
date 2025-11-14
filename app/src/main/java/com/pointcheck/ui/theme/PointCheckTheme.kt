package com.pointcheck.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF0D47A1),
    onPrimary = Color.White,
    background = Color(0xFFF5F5F5)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF0D47A1),
    onPrimary = Color.White,
    background = Color(0xFF121212)
)

@Composable
fun PointCheckTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, typography = androidx.compose.material3.Typography(), content = content)
}
