package com.pointcheck.features.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.core.navigation.Screen
import com.pointcheck.core.presentation.components.HeaderIcon
import com.pointcheck.core.presentation.components.SectionHeader
import com.pointcheck.core.utils.CategoryIdentityMapper
import com.pointcheck.features.dashboard.data.dto.DashboardMetricsDto
import com.pointcheck.features.dashboard.data.dto.FavoriteSpecialistDto
import com.pointcheck.features.dashboard.data.dto.ReportSummaryResponseDto
import com.pointcheck.features.external.data.dto.WeatherResponseDto
import com.pointcheck.features.reservation.data.dto.ReservationResponseDto
import coil.compose.AsyncImage

/**
 * AUDITORÍA: Orquestador principal de la interfaz de usuario.
 * Implementa un Dashboard polimórfico basado en roles (ADMIN, SPECIALIST, CLIENT).
 * Hallazgo: Uso extensivo de BottomSheets para acciones administrativas agiliza la navegación
 * pero incrementa la complejidad del estado en un solo archivo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(nav: NavController, vm: DashboardViewModel = viewModel()) {
    val s by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showNotifications by remember { mutableStateOf(false) }
    var showAdminUsers by remember { mutableStateOf(false) }
    var showAdminFinance by remember { mutableStateOf(false) }
    var showAdminSettings by remember { mutableStateOf(false) }
    var showAuditLogs by remember { mutableStateOf(false) }

    val isWorker = s.userRole == "SPECIALIST" || s.userRole == "PROFESSIONAL" || s.userRole == "ADMIN"
    val isAdmin = s.userRole == "ADMIN"

    LaunchedEffect(s.error) {
        s.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { pad ->
        if (showNotifications) {
            NotificationBottomSheet(
                notifications = s.clientDashboard?.recentNotifications ?: emptyList(),
                onDismiss = { showNotifications = false }
            )
        }
        if (showAdminUsers) AdminUsersBottomSheet(onDismiss = { showAdminUsers = false })
        if (showAdminFinance) AdminFinanceBottomSheet(onDismiss = { showAdminFinance = false })
        if (showAdminSettings) AdminSettingsBottomSheet(onDismiss = { showAdminSettings = false })
        if (showAuditLogs) AuditLogsBottomSheet(onDismiss = { showAuditLogs = false })

        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // --- HEADER (VIBRANT SLATE GRAY) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
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
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.padding(8.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Hola, ${s.userName.split(" ").firstOrNull() ?: "Usuario"}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                    Row {
                        HeaderIcon(Icons.Outlined.Search)
                        Spacer(Modifier.width(12.dp))
                        val unreadCount = s.clientDashboard?.recentNotifications?.count { !it.isRead } ?: 0
                        BadgedBox(
                            badge = { if (unreadCount > 0) Badge { Text(unreadCount.toString()) } }
                        ) {
                            HeaderIcon(Icons.Outlined.Notifications) { showNotifications = true }
                        }
                    }
                }
            }

            // --- FLOATING MAIN CARD ---
            Column(
                modifier = Modifier
                    .offset(y = (-100).dp)
                    .padding(horizontal = 16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(24.dp)) {
                        if (isAdmin) {
                            AdminSummaryContent(s.metrics)
                        } else if (isWorker) {
                            ProfessionalSummaryContent(s.reportSummary)
                        } else {
                            ClientSummaryContent(s)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // --- SECTION: QUICK ACTIONS ---
                SectionHeader("Accesos rápidos")
                
                if (isAdmin) {
                    AdminQuickActions(
                        onShowUsers = { showAdminUsers = true },
                        onShowAudit = { showAuditLogs = true },
                        onShowFinance = { showAdminFinance = true },
                        onShowSettings = { showAdminSettings = true }
                    )
                } else if (isWorker) {
                    ProfessionalQuickActions(nav)
                } else {
                    ClientQuickActions(nav, s.categories)
                }

                Spacer(Modifier.height(24.dp))

                // --- SECTION: PROMOS/TIPS ---
                InfoCard()
                
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun AdminSummaryContent(m: DashboardMetricsDto) {
    Column {
        Text("Resumen del Ecosistema", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(
                    text = "$${String.format("%,.0f", m.paidBillingAmount)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black
                )
                Text("Ingresos netos hoy", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Icon(Icons.Outlined.Analytics, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(40.dp))
        }
    }
}

@Composable
fun ProfessionalSummaryContent(r: ReportSummaryResponseDto?) {
    Column {
        Text("Saldo estimado", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$ ${r?.totalCharged ?: 0.0}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Outlined.Visibility, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${r?.todayReservations ?: 0} citas para hoy",
            color = Color(0xFF43A047),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ClientSummaryContent(s: DashboardUiState) {
    val next = s.clientDashboard?.nextAppointment
    if (next != null) {
        Column {
            Text("Tu próxima cita", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Text(
                text = next.serviceName ?: "Servicio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            Text(
                text = next.reservationStart.replace("T", " ").substringBeforeLast(":"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Person, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                Spacer(Modifier.width(4.dp))
                Text(next.specialistName ?: "Especialista", style = MaterialTheme.typography.bodySmall)
            }
        }
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("No tienes citas próximas", color = Color.Gray)
            TextButton(onClick = { /* Navigate to Booking handled by actions */ }) {
                Text("Explorar servicios", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminQuickActions(onShowUsers: () -> Unit, onShowAudit: () -> Unit, onShowFinance: () -> Unit, onShowSettings: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionItem("Usuarios", Icons.Outlined.Group, Modifier.weight(1f), onClick = onShowUsers)
            QuickActionItem("Auditoría", Icons.Outlined.HistoryEdu, Modifier.weight(1f), onClick = onShowAudit)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionItem("Finanzas", Icons.Outlined.Payments, Modifier.weight(1f), onClick = onShowFinance)
            QuickActionItem("Ajustes", Icons.Outlined.Settings, Modifier.weight(1f), onClick = onShowSettings)
        }
    }
}

@Composable
fun ProfessionalQuickActions(nav: NavController) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionItem("Mi Agenda", Icons.Outlined.CalendarMonth, Modifier.weight(1f)) {
                nav.navigate(Screen.Scheduled.route)
            }
            QuickActionItem("Servicios", Icons.Outlined.Handyman, Modifier.weight(1f)) {
                nav.navigate(Screen.ServiceManagement.route)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionItem("Reportes", Icons.Outlined.Assessment, Modifier.weight(1f)) {
                nav.navigate(Screen.WeeklyReport.route)
            }
            QuickActionItem("Suscripción", Icons.Outlined.StarOutline, Modifier.weight(1f)) {
                nav.navigate(Screen.Subscription.route)
            }
        }
    }
}

@Composable
fun ClientQuickActions(nav: NavController, categories: List<com.pointcheck.features.onboarding.presentation.dto.CategoryDto>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionItem("Nueva Cita", Icons.Outlined.AddCircleOutline, Modifier.weight(1f)) {
                nav.navigate(Screen.Booking.route)
            }
            QuickActionItem("Historial", Icons.Outlined.History, Modifier.weight(1f)) {
                nav.navigate(Screen.AppointmentHistory.createRoute("all"))
            }
        }
        
        SectionHeader("Categorías populares")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(categories) { cat ->
                CategoryChip(cat.name, CategoryIdentityMapper.mapIcon(cat.icon)) {
                    nav.navigate(Screen.Booking.createRoute(null, cat.id))
                }
            }
        }
    }
}

@Composable
fun CategoryChip(name: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
        modifier = Modifier.height(48.dp)
    ) {
        Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Punto de control", fontWeight = FontWeight.Bold)
                Text("Mantén tu perfil actualizado para que tus clientes confíen en ti.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun QuickActionItem(label: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

// Reuse existing Notification Bottom Sheets and other logic but with new styling if needed
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationBottomSheet(
    notifications: List<com.pointcheck.features.dashboard.data.dto.NotificationSummaryDto>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            Text("Notificaciones", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(24.dp), fontWeight = FontWeight.Black)
            if (notifications.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No tienes notificaciones", color = Color.Gray)
                }
            } else {
                notifications.forEach { NotificationItem(it) }
            }
        }
    }
}

@Composable
fun NotificationItem(n: com.pointcheck.features.dashboard.data.dto.NotificationSummaryDto) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(10.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(n.title, fontWeight = FontWeight.Bold)
            Text(n.message, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        if (!n.isRead) {
            Surface(modifier = Modifier.size(8.dp), shape = CircleShape, color = MaterialTheme.colorScheme.error) {}
        }
    }
}

// ... Implement other Admin Bottom Sheets similarly with white background and clean cards ...
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersBottomSheet(onDismiss: () -> Unit) {
    val vm: DashboardViewModel = viewModel()
    val s by vm.state.collectAsState()
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(Modifier.fillMaxWidth().padding(bottom = 32.dp).padding(horizontal = 24.dp)) {
            Text("Gestión de Usuarios", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(16.dp))
            s.adminUsers.forEach { user ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(user.name, fontWeight = FontWeight.Bold)
                            Text(user.role, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                        Switch(checked = user.active, onCheckedChange = { vm.toggleUserStatus(user.id) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFinanceBottomSheet(onDismiss: () -> Unit) {
    val vm: DashboardViewModel = viewModel()
    val s by vm.state.collectAsState()
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(Modifier.fillMaxWidth().padding(bottom = 40.dp).padding(horizontal = 24.dp)) {
            Text("Balance Global", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(24.dp))
            s.financialReport?.let { report ->
                AdminFinanceRow("Ingresos Totales", "$${report["totalRevenue"]}", Color(0xFF43A047))
                AdminFinanceRow("Transacciones", "${report["totalTransactions"]}")
                AdminFinanceRow("Pendiente", "$${report["pendingRevenue"]}", MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AdminFinanceRow(label: String, value: String, color: Color = Color.Black) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Black, color = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsBottomSheet(onDismiss: () -> Unit) {
    val vm: DashboardViewModel = viewModel()
    val s by vm.state.collectAsState()
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(Modifier.fillMaxWidth().padding(bottom = 40.dp).padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
            Text("Ajustes del Sistema", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(24.dp))
            s.adminSettings.forEach { setting ->
                Column(Modifier.padding(vertical = 8.dp)) {
                    Text(setting.key, fontWeight = FontWeight.Bold)
                    Text(setting.description ?: "", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = setting.value, onValueChange = { vm.updateSetting(setting.key, it) }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogsBottomSheet(onDismiss: () -> Unit) {
    val vm: DashboardViewModel = viewModel()
    val s by vm.state.collectAsState()
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(Modifier.fillMaxWidth().padding(bottom = 40.dp).padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
            Text("Logs de Auditoría", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(16.dp))
            s.auditLogs.forEach { log ->
                Column(Modifier.padding(vertical = 12.dp)) {
                    Text(log.action, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(log.details ?: "", style = MaterialTheme.typography.bodyMedium)
                    Text(log.timestamp.replace("T", " "), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = Color.LightGray.copy(alpha = 0.2f))
                }
            }
        }
    }
}
