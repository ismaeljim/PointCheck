package com.pointcheck.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * PointCheck "SaaS Operativa" Theme Configuration.
 * Maps custom design tokens to Material 3 ColorScheme.
 */
private val LightColors = lightColorScheme(
    primary = PointCheckBlue,
    onPrimary = PointCheckOnBlue,
    primaryContainer = PointCheckBlueContainer,
    onPrimaryContainer = PointCheckOnBlueContainer,
    
    secondary = PointCheckSecondary,
    onSecondary = Color.White,
    secondaryContainer = PointCheckSecondaryContainer,
    onSecondaryContainer = PointCheckOnSecondaryContainer,
    
    background = PointCheckBackground,
    onBackground = PointCheckOnSurface,
    
    surface = PointCheckSurface,
    onSurface = PointCheckOnSurface,
    surfaceVariant = PointCheckSurfaceVariant,
    onSurfaceVariant = PointCheckOnSurfaceVariant,
    
    outline = PointCheckOutline,
    
    error = PointCheckError,
    onError = Color.White,
    errorContainer = PointCheckErrorContainer,
    onErrorContainer = PointCheckOnErrorContainer
)

// Dark mode adaptation for SaaS environment
private val DarkColors = darkColorScheme(
    primary = PointCheckBlue,
    onPrimary = Color.White,
    background = Color(0xFF1A1C1E),
    surface = Color(0xFF1A1C1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF44474E),
    onSurfaceVariant = Color.LightGray
)

@Composable
fun PointCheckTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
