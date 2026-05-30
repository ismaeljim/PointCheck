package com.pointcheck.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.pointcheck.core.presentation.components.AppButton
import com.pointcheck.core.presentation.components.HeaderIcon
import com.pointcheck.core.presentation.components.SectionHeader
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(nav: NavController, rootNav: NavController) {
    val context = LocalContext.current
    val prefs = UserPreferences(context)
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        email = prefs.email.first() ?: "No identificado"
        role = prefs.role.first() ?: "CLIENT"
        name = prefs.name.first() ?: "Usuario"
    }

    val isWorker = role.uppercase() == "SPECIALIST" || role.uppercase() == "ADMIN"
    val isAdmin = role.uppercase() == "ADMIN"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // --- HEADER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(50.dp)
                    ) {
                        Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.padding(10.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = if (isWorker) "Perfil Profesional" else "Cuenta Cliente",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
                HeaderIcon(Icons.Outlined.Settings)
            }
        }

        // --- FLOATING CONTENT ---
        Column(
            modifier = Modifier
                .offset(y = (-40).dp)
                .padding(horizontal = 16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(20.dp)) {
                    SectionHeader("Información de Cuenta")
                    ProfileInfoRow(Icons.Outlined.Email, "Email", email)
                    ProfileInfoRow(Icons.Outlined.Badge, "Tipo de cuenta", if (isWorker) "Especialista" else "Cliente")
                }
            }

            Spacer(Modifier.height(16.dp))

            if (isWorker) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        SectionHeader("Negocio")
                        ProfileMenuItem(
                            icon = Icons.Outlined.Store,
                            title = "Configurar Perfil",
                            subtitle = "Información pública y ubicación",
                            onClick = { nav.navigate(Screen.ProfessionalProfile.route) }
                        )
                        ProfileMenuItem(
                            icon = Icons.Outlined.CreditCard,
                            title = "Mi Suscripción",
                            subtitle = "Gestiona tu plan comercial",
                            onClick = { nav.navigate(Screen.Subscription.route) }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            if (isAdmin) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        SectionHeader("Panel de Administración")
                        ProfileMenuItem(
                            icon = Icons.Outlined.Group,
                            title = "Gestión de Usuarios",
                            subtitle = "Editar roles y permisos",
                            onClick = { nav.navigate(Screen.AdminUsers.route) }
                        )
                        ProfileMenuItem(
                            icon = Icons.AutoMirrored.Outlined.Assignment,
                            title = "Logs de Auditoría",
                            subtitle = "Ver actividad del sistema",
                            onClick = { nav.navigate(Screen.AdminAudit.route) }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            AppButton(
                text = "Cerrar Sesión",
                onClick = {
                    scope.launch {
                        prefs.clear()
                        rootNav.navigate(Screen.Login.route) {
                            popUpTo(Screen.Main.route) { inclusive = true }
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
fun ProfileInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}
