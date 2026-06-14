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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import com.pointcheck.core.presentation.components.AppTopBar
import com.pointcheck.core.ui.components.PCButton
import com.pointcheck.core.navigation.Screen
import com.pointcheck.features.dashboard.data.dto.NotificationSummaryDto

/**
 * Pantalla principal del tablero que muestra contenido de forma adaptativa según el rol del usuario.
 */
@Composable
fun DashboardScreen(nav: NavController, vm: DashboardViewModel = viewModel()) {
    val s by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showNotifications by remember { mutableStateOf(false) }
    var showAdminUsers by remember { mutableStateOf(false) }
    var showAdminFinance by remember { mutableStateOf(false) }
    var showAdminSettings by remember { mutableStateOf(false) }
    var showAuditLogs by remember { mutableStateOf(false) }

    LaunchedEffect(s.error) {
        s.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    LaunchedEffect(Unit) {
        vm.loadDashboard()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "PointCheck",
                actions = {
                    val unreadCount = s.clientDashboard?.recentNotifications?.count { !it.isRead } ?: 0
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
        if (showNotifications) {
            NotificationBottomSheet(
                notifications = s.clientDashboard?.recentNotifications ?: emptyList(),
                onDismiss = { showNotifications = false }
            )
        }
        if (showAdminUsers) {
            AdminUsersBottomSheet(onDismiss = { showAdminUsers = false })
        }
        if (showAdminFinance) {
            AdminFinanceBottomSheet(onDismiss = { showAdminFinance = false })
        }
        if (showAdminSettings) {
            AdminSettingsBottomSheet(onDismiss = { showAdminSettings = false })
        }
        if (showAuditLogs) {
            AuditLogsBottomSheet(onDismiss = { showAuditLogs = false })
        }
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            val isSpecialist = s.userRole == "SPECIALIST" || s.userRole == "PROFESSIONAL"
            
            Spacer(Modifier.height(16.dp))
            Text(
                text = if (isSpecialist) "Bienvenido, ${s.userName}" else "Hola, ${s.userName}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (isSpecialist) (s.reportSummary?.specialty ?: "Especialista") else "Tu resumen diario",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            if (s.isLoading) {
                ShimmerDashboard()
            } else {
                Spacer(Modifier.height(24.dp))

                when (s.userRole) {
                    "ADMIN" -> AdminDashboard(
                        m = s.metrics,
                        nav = nav,
                        onShowUsers = { showAdminUsers = true },
                        onShowAudit = { showAuditLogs = true },
                        onShowFinance = { showAdminFinance = true },
                        onShowSettings = { showAdminSettings = true }
                    )
                    "SPECIALIST", "PROFESSIONAL" -> ProfessionalDashboard(s.reportSummary, nav, s)
                    else -> ClientDashboardV2(s, nav)
                }
            }

            if (s.error != null) {
                Spacer(Modifier.height(16.dp))
                PCButton(text = "Reintentar", onClick = { vm.loadDashboard() }, enabled = !s.isLoading)
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                "Notificaciones",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold
            )
            
            if (notifications.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No tienes notificaciones", color = MaterialTheme.colorScheme.secondary)
                }
            } else {
                val vm: DashboardViewModel = viewModel()
                notifications.forEach { notification ->
                    NotificationItem(notification) {
                        if (!notification.isRead) {
                            vm.markAsRead(notification.id)
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: NotificationSummaryDto,
    onClick: () -> Unit
) {
    val icon = when (notification.type) {
        "ALERT" -> Icons.Default.Warning
        "CONFIRMATION" -> Icons.Default.CheckCircle
        else -> Icons.Default.Info
    }
    val color = when (notification.type) {
        "ALERT" -> MaterialTheme.colorScheme.error
        "CONFIRMATION" -> Color(0xFF4CAF50)
        else -> MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            if (!notification.isRead) {
                Surface(
                    modifier = Modifier.size(8.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.error
                ) {}
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                notification.title, 
                fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(notification.message, style = MaterialTheme.typography.bodyMedium)
            
            val displayDate = try {
                notification.createdAt.replace("T", " ").substringBeforeLast(":")
            } catch (_: Exception) {
                "Recientemente"
            }
            
            Text(
                displayDate,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
