package com.pointcheck.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.core.navigation.Screen
import com.pointcheck.core.presentation.components.*
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
    var name by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        email = prefs.email.first() ?: "No identificado"
        role = prefs.role.first() ?: "SIN ROL"
        name = email.substringBefore("@").replaceFirstChar { it.uppercase() }
    }

    Scaffold(
        topBar = { 
            AppTopBar(
                title = "Mi Perfil"
            ) 
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header del perfil
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Icon(
                            Icons.Default.Person, 
                            null, 
                            modifier = Modifier.padding(16.dp), 
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Configuración",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                AppCard {
                    Column {
                        ProfileOptionItem(
                            title = "Mis Datos Personales",
                            icon = Icons.Default.Badge,
                            onClick = { /* Edición de datos */ }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

                        if (role.equals("SPECIALIST", ignoreCase = true) || role.equals("PROFESSIONAL", ignoreCase = true)) {
                            ProfileOptionItem(
                                title = "Configuración Profesional",
                                icon = Icons.Default.BusinessCenter,
                                onClick = { nav.navigate(Screen.ProfessionalProfile.route) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                            ProfileOptionItem(
                                title = "Gestión de Servicios",
                                icon = Icons.AutoMirrored.Filled.List,
                                onClick = { nav.navigate(Screen.ServiceManagement.route) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                            ProfileOptionItem(
                                title = "Mi Suscripción",
                                icon = Icons.Default.Star,
                                onClick = { nav.navigate(Screen.Subscription.route) }
                            )
                        } else {
                            // Opciones adicionales para clientes si las hubiera
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                AppOutlinedButton(
                    text = "Cerrar Sesión",
                    icon = Icons.AutoMirrored.Filled.Logout,
                    onClick = {
                        scope.launch {
                            prefs.clear()
                            nav.navigate(Screen.Login.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                        }
                    },
                    contentColor = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ProfileOptionItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon, 
            null, 
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            title, 
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Default.ChevronRight, 
            null, 
            tint = MaterialTheme.colorScheme.outline
        )
    }
}
