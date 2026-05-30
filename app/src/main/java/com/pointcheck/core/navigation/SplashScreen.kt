package com.pointcheck.core.navigation

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pointcheck.core.prefs.UserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(nav: NavController) {
    val context = LocalContext.current
    val prefs = UserPreferences(context)

    LaunchedEffect(Unit) {
        delay(2000)
        val isLogged = prefs.isLogged.first()
        if (isLogged) {
            nav.navigate(Screen.Main.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        } else {
            nav.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary), 
        contentAlignment = Alignment.Center
    ) {
        Text(
            "PointCheck", 
            style = MaterialTheme.typography.displayLarge,
            color = Color.White,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
    }
}
