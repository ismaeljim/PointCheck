package com.pointcheck.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.pointcheck.data.prefs.UserPreferences
import com.pointcheck.navigation.Screen
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(nav: NavController) {
    val context = LocalContext.current
    val prefs = UserPreferences(context)

    LaunchedEffect(Unit) {
        // Comprobamos si el usuario ya ha iniciado sesión
        val isLogged = prefs.isLogged.first()

        if (isLogged) {
            // Si hay sesión, vamos directamente al perfil
            nav.navigate(Screen.Profile.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        } else {
            // Si no hay sesión, vamos al dashboard
            nav.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    // Mientras se decide a dónde navegar, mostramos un indicador de carga
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
