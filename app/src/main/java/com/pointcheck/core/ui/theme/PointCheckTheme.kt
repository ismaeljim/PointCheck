package com.pointcheck.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = SlateGray,
    onPrimary = White,
    primaryContainer = SlateGray.copy(alpha = 0.1f),
    onPrimaryContainer = SlateGray,
    secondary = MercadoPagoBlue,
    onSecondary = White,
    tertiary = MercadoPagoYellow,
    onTertiary = TextPrimary,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = DividerColor,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
    error = ErrorRed,
    onError = White
)

private val DarkColors = darkColorScheme(
    primary = SlateGray,
    onPrimary = White,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = White,
    onSurface = White
)

@Composable
fun PointCheckTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    // Forzamos modo claro para la estética de Mercado Pago si se prefiere, 
    // pero respetaremos el parámetro por ahora.
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
