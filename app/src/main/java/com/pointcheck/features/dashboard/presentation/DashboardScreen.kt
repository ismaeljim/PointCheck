package com.pointcheck.features.dashboard.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import com.pointcheck.core.ui.components.*
import com.pointcheck.core.navigation.Screen
import com.pointcheck.features.dashboard.data.dto.NotificationSummaryDto

/**
 * Pantalla principal del tablero que muestra contenido de forma adaptativa según el rol del usuario.
 * Utiliza el estado sellado del ViewModel para garantizar una UI consistente.
 */
@Composable
fun DashboardScreen(nav: NavController, vm: DashboardViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Auto-Refresco: Se dispara cada vez que la pantalla se vuelve a mostrar
    LaunchedEffect(Unit) {
        vm.loadDashboard(silent = true)
    }
    
    // Control de diálogos/hojas inferiores
    var showNotifications by remember { mutableStateOf(false) }
    var showAdminUsers by remember { mutableStateOf(false) }
    var showAdminAudit by remember { mutableStateOf(false) }
    var showAdminFinance by remember { mutableStateOf(false) }
    var showAdminSettings by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            PointCheckTopBar(
                title = "PointCheck",
                actions = {
                    val unreadCount = if (state is DashboardUiState.Success) {
                        (state as DashboardUiState.Success).clientDashboard?.recentNotifications?.count { !it.isRead } ?: 0
                    } else 0
                    
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge { Text(unreadCount.toString()) }
                            }
                        }
                    ) {
                        IconButton(onClick = { showNotifications = true }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                        }
                    }
                    IconButton(onClick = { nav.navigate(Screen.Profile.route) }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil")
                    }
                }
            )
        }
    ) { pad ->
        // Manejo de estados de la UI
        when (val s = state) {
            is DashboardUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is DashboardUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(pad).padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Text(s.message, style = MaterialTheme.typography.bodyLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(Modifier.height(24.dp))
                    PointCheckButton(text = "Reintentar", onClick = { vm.loadDashboard() })
                }
            }

            is DashboardUiState.ProfileIncomplete -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(pad).padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color(0xFFFBC02D))
                    Spacer(Modifier.height(16.dp))
                    Text("Tu perfil está incompleto", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Necesitas configurar tu perfil profesional para empezar a recibir citas.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(Modifier.height(24.dp))
                    PointCheckButton(text = "Completar Perfil", onClick = { nav.navigate(Screen.ProfessionalProfile.route) })
                }
            }

            is DashboardUiState.Success -> {
                if (showNotifications) {
                    NotificationBottomSheet(
                        notifications = s.clientDashboard?.recentNotifications ?: emptyList(),
                        onDismiss = { showNotifications = false }
                    )
                }

                if (showAdminUsers) AdminUsersBottomSheet(onDismiss = { showAdminUsers = false })
                if (showAdminAudit) AuditLogsBottomSheet(onDismiss = { showAdminAudit = false })
                if (showAdminFinance) AdminFinanceBottomSheet(onDismiss = { showAdminFinance = false })
                if (showAdminSettings) AdminSettingsBottomSheet(onDismiss = { showAdminSettings = false })

                Column(
                    modifier = Modifier
                        .padding(pad)
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    val isSpecialist = s.userRole == "SPECIALIST" || s.userRole == "PROFESSIONAL"

                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = if (isSpecialist) "Bienvenido, ${s.userName}" else "Hola, ${s.userName}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (isSpecialist) (s.reportSummary?.specialty ?: "Especialista") else "Tu resumen diario",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(Modifier.height(24.dp))

                    when (s.userRole) {
                        "ADMIN" -> {
                            AdminDashboard(
                                m = s.metrics,
                                chartType = s.adminChartType,
                                onChartTypeChange = { vm.toggleAdminChartType(it) },
                                onShowUsers = { showAdminUsers = true },
                                onShowAudit = { showAdminAudit = true },
                                onShowFinance = { showAdminFinance = true },
                                onShowSettings = { showAdminSettings = true }
                            )
                        }
                        "SPECIALIST", "PROFESSIONAL" -> {
                            ProfessionalDashboard(r = s.reportSummary, nav = nav, s = s)
                        }
                        else -> {
                            // Home del Cliente: Reemplazamos Agenda por Historial Completo (Unificado)
                            ClientDashboard(s = s, nav = nav)
                        }
                    }
                    
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationBottomSheet(
    notifications: List<NotificationSummaryDto>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            Text("Notificaciones", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)

            if (notifications.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("No tienes notificaciones", color = MaterialTheme.colorScheme.secondary)
                }
            } else {
                val vm: DashboardViewModel = viewModel()
                notifications.forEach { notification ->
                    NotificationItem(notification) {
                        if (!notification.isRead) vm.markAsRead(notification.id)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: NotificationSummaryDto, onClick: () -> Unit) {
    val color = if (notification.type == "ALERT") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    
    PointCheckCard(
        title = notification.title,
        subtitle = notification.message,
        icon = Icons.Default.Notifications,
        iconColor = color,
        badgeText = if (!notification.isRead) "Nueva" else null,
        badgeColor = MaterialTheme.colorScheme.errorContainer,
        onClick = onClick
    ) {
        // No additional content needed for simple notification items
    }
}
