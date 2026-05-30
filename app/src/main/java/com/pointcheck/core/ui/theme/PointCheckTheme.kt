package com.pointcheck.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = MP_SlateGray,
    onPrimary = White,
    secondary = MP_Indigo,
    onSecondary = White,
    tertiary = MP_IndigoLight,
    background = MP_Background,
    surface = MP_Surface,
    onSurface = Black,
    surfaceVariant = MP_SurfaceVariant,
    onSurfaceVariant = GrayText,
    error = MP_Error,
    outline = MP_SlateGrayLight.copy(alpha = 0.5f)
)

private val DarkColors = darkColorScheme(
    primary = MP_SlateGrayLight,
    onPrimary = White,
    secondary = MP_IndigoLight,
    onSecondary = White,
    background = MP_SlateGrayDark,
    surface = MP_SlateGrayDark,
    onSurface = White
)

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
