package com.pointcheck.features.profile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.core.navigation.Screen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(nav: NavController) {
    val context = LocalContext.current
    val prefs = UserPreferences(context)
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        email = prefs.email.first() ?: "No identificado"
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Mi Perfil") }) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            Text("Email: $email", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                scope.launch {
                    prefs.clear()
                    nav.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            }) {
                Text("Cerrar Sesión")
            }
        }
    }
}
