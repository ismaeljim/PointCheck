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

@Composable
fun SplashScreen(nav: NavController) {
    val context = LocalContext.current
    val prefs = UserPreferences(context)

    LaunchedEffect(Unit) {
        Log.d("SplashScreen", "Iniciando SplashScreen")
        delay(1500)
        try {
            // timeout para evitar bloqueo eterno si DataStore falla
            val isLogged = withTimeoutOrNull(2000) {
                prefs.isLogged.first()
            } ?: false
            
            Log.d("SplashScreen", "Estado login obtenido: $isLogged")
            
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
            Log.e("SplashScreen", "Error en SplashScreen: ${e.message}", e)
            nav.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Fondo explícito para diagnóstico
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "PointCheck", 
            style = MaterialTheme.typography.headlineLarge, // Usar estilo existente
            color = Color.Black
        )
    }
}
