package com.pointcheck.core.navigation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.compose.ui.unit.dp
import com.pointcheck.core.prefs.UserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Pantalla de Bienvenida (Splash).
 * Responsable de la lógica de decisión inicial: redirigir al Dashboard si hay sesión activa,
 * o al Login en caso contrario.
 */
@Composable
fun SplashScreen(nav: NavController) {
    val context = LocalContext.current
    // Acceso a preferencias locales (DataStore)
    val prefs = UserPreferences(context)

    LaunchedEffect(Unit) {
        Log.d("SplashScreen", "Iniciando flujo de verificación de sesión con sincronización de RAM")
        val startTime = System.currentTimeMillis()
        
        try {
            // 1. Esperamos a que la persistencia haya subido a RAM (Max 3s)
            // Esto garantiza que prefs.isLoggedCached sea la verdad absoluta
            withTimeoutOrNull(3000) {
                prefs.isInitialized.first { it }
            }
            
            // 2. Calculamos cuánto tiempo falta para cumplir el mínimo de branding (1.2s)
            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed < 1200) {
                delay(1200 - elapsed)
            }
            
            val isLogged = prefs.isLoggedCached
            Log.d("SplashScreen", "Decisión de navegación final: isLogged=$isLogged")
            
            if (isLogged) {
                nav.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            } else {
                nav.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        } catch (e: Exception) {
            Log.e("SplashScreen", "Error en warm-up de sesión: ${e.message}")
            nav.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    // UI del Splash: Identidad visual corporativa.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.AvTimer,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "PointCheck",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = "Tu tiempo, bajo control",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }
    }
}
