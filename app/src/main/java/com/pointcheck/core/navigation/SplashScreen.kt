package com.pointcheck.core.navigation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
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
        Log.d("SplashScreen", "Iniciando flujo de verificación de sesión")
        
        // Retardo visual para mostrar la marca
        delay(1500)
        
        try {
            // Se utiliza un timeout de 2 segundos para evitar que un fallo en DataStore 
            // deje la aplicación bloqueada en blanco.
            val isLogged = withTimeoutOrNull(2000) {
                prefs.isLogged.first()
            } ?: false
            
            Log.d("SplashScreen", "Estado de sesión: isLogged=$isLogged")
            
            if (isLogged) {
                // Si está logueado, vamos al Dashboard y limpiamos el Splash del historial
                nav.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            } else {
                // Si no, vamos al Login
                nav.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        } catch (e: Exception) {
            Log.e("SplashScreen", "Fallo crítico en lectura de preferencias: ${e.message}", e)
            // Ante cualquier error, por seguridad redirigimos al Login
            nav.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    // UI del Splash: Fondo blanco con el nombre de la app centrado.
    // TODO: Se podría añadir una animación de carga o el logo oficial.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "PointCheck", 
            style = MaterialTheme.typography.headlineLarge,
            color = Color.Black
        )
    }
}
