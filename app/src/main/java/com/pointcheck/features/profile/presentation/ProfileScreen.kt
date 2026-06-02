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
import com.pointcheck.core.presentation.components.AppButton
import com.pointcheck.core.presentation.components.AppOutlinedButton
import com.pointcheck.core.presentation.components.AppTopBar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(nav: NavController) {
    val context = LocalContext.current
    val prefs = UserPreferences(context)
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        email = prefs.email.first() ?: "No identificado"
        role = prefs.role.first() ?: "SIN ROL"
    }

    Scaffold(topBar = { AppTopBar(title = "Mi Perfil") }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Información de Cuenta", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Email: $email", style = MaterialTheme.typography.bodyLarge)
                    Text("Rol: $role", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                }
            }

            Spacer(Modifier.height(16.dp))

            if (role.equals("SPECIALIST", ignoreCase = true)) {
                AppButton(
                    text = "Configurar Perfil Profesional",
                    onClick = { nav.navigate(Screen.ProfessionalProfile.route) }
                )
                Spacer(Modifier.height(8.dp))
            }

            AppOutlinedButton(
                text = "Cerrar Sesión",
                onClick = {
                    scope.launch {
                        prefs.clear()
                        nav.navigate(Screen.Login.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}
