package com.pointcheck.features.dashboard.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.core.ui.theme.*
import com.pointcheck.core.navigation.Screen
import com.pointcheck.core.presentation.components.*
import com.pointcheck.core.utils.CategoryIdentityMapper
import com.pointcheck.features.dashboard.data.dto.DashboardMetricsDto
import com.pointcheck.features.dashboard.data.dto.FavoriteSpecialistDto
import com.pointcheck.features.dashboard.data.dto.ReportSummaryResponseDto
import com.pointcheck.features.external.data.dto.WeatherResponseDto
import com.pointcheck.features.reservation.data.dto.ReservationResponseDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(nav: NavController, vm: DashboardViewModel = viewModel()) {
    val s by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showMenu by remember { mutableStateOf(false) }
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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "PointCheck",
                actions = {
                    val unreadCount = s.clientDashboard?.recentNotifications?.count { !it.isRead } ?: 0
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge(containerColor = MaterialTheme.colorScheme.error) { 
                                    Text(unreadCount.toString(), color = Color.White) 
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = { showNotifications = true }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                        }
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Mi Perfil") },
                            leadingIcon = { Icon(Icons.Default.Person, null) },
                            onClick = { nav.navigate(Screen.Profile.route); showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Refrescar") },
                            leadingIcon = { Icon(Icons.Default.Refresh, null) },
                            onClick = { vm.loadDashboard(); showMenu = false }
                        )
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
        if (showAdminUsers) AdminUsersBottomSheet(onDismiss = { showAdminUsers = false })
        if (showAdminFinance) AdminFinanceBottomSheet(onDismiss = { showAdminFinance = false })
        if (showAdminSettings) AdminSettingsBottomSheet(onDismiss = { showAdminSettings = false })
        if (showAuditLogs) AuditLogsBottomSheet(onDismiss = { showAuditLogs = false })

        Box(modifier = Modifier.padding(pad).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            val isSpecialist = s.userRole == "SPECIALIST" || s.userRole == "PROFESSIONAL"
            // Header con fondo de color principal
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Sección de bienvenida sobre el fondo
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = if (isSpecialist) "Bienvenido, ${s.userName}" else "Hola, ${s.userName}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = if (isSpecialist) (s.reportSummary?.specialty ?: s.userRole) else "Que gusto verte de nuevo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }

                // Contenedor principal con las cards (empezando desde el traslape con el header)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    if (s.isLoading) {
                        ShimmerDashboard()
                    } else {
                        if (s.userRole == "ADMIN") {
                            AdminDashboard(
                                m = s.metrics,
                                nav = nav,
                                onShowUsers = { showAdminUsers = true },
                                onShowAudit = { showAuditLogs = true },
                                onShowFinance = { showAdminFinance = true },
                                onShowSettings = { showAdminSettings = true }
                            )
                        } else if (isSpecialist) {
                            ProfessionalDashboard(s.reportSummary, nav)
                        } else {
                            ClientDashboardV3(s, nav)
                        }
                    }

                    if (s.error != null) {
                        Spacer(Modifier.height(32.dp))
                        EmptyState(
                            title = "Ups! Algo salió mal",
                            description = s.error ?: "No pudimos cargar tu información en este momento.",
                            icon = Icons.Default.CloudOff,
                            actionText = "Reintentar",
                            onAction = { vm.loadDashboard() }
                        )
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun ClientDashboardV3(s: DashboardUiState, nav: NavController) {
    val d = s.clientDashboard
    val weather = s.weather
    
    Column(Modifier.fillMaxWidth()) {
        if (d?.nextAppointment != null) {
            FeaturedAppointmentCard(d.nextAppointment, weather, nav)
            Spacer(Modifier.height(24.dp))
        }

        AppCard {
            Column(Modifier.padding(16.dp)) {
                Text("Tus Favoritos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                if (d?.favoriteSpecialists?.isNotEmpty() == true) {
                    LazyRow(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(d.favoriteSpecialists) { specialist ->
                            FavoriteSpecialistCard(specialist) {
                                nav.navigate(Screen.Booking.createRoute(specialist.specialistId))
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.FavoriteBorder, 
                            null, 
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            "Agrega especialistas a favoritos para acceder más rápido.", 
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))

        Text("¿Qué necesitas hoy?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        
        if (s.categories.isEmpty() && s.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            ServiceCategoryGrid(s.categories, nav)
        }
    }
}

@Composable
fun FeaturedAppointmentCard(
    appointment: ReservationResponseDto?,
    weather: WeatherResponseDto?,
    nav: NavController
) {
    if (appointment == null) return
    val context = LocalContext.current

    AppCard {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            null,
                            modifier = Modifier.padding(10.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Tu próxima cita",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            appointment.serviceName ?: "Servicio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                if (weather != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WbSunny, null, tint = Color(0xFFFBC02D), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${weather.main.temp.toInt()}°C", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            
            HorizontalDivider(Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                Text(appointment.specialist.name, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                Text(
                    appointment.reservationStart.replace("T", " ").substringBeforeLast(":"),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppButton(
                    text = "Cómo llegar",
                    onClick = {
                        val address = appointment.address
                        if (!address.isNullOrBlank()) {
                            val gmmIntentUri = android.net.Uri.parse("geo:0,0?q=${android.net.Uri.encode(address)}")
                            val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            try {
                                context.startActivity(mapIntent)
                            } catch (e: Exception) {
                                val fallbackIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
                                context.startActivity(fallbackIntent)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !appointment.address.isNullOrBlank()
                )
                AppOutlinedButton(
                    text = "Detalles",
                    onClick = { nav.navigate(Screen.Scheduled.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun FavoriteSpecialistCard(specialist: FavoriteSpecialistDto, onClick: () -> Unit) {
    Column(
        Modifier.width(80.dp).clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Icon(
                Icons.Default.Person,
                null,
                modifier = Modifier.padding(12.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            specialist.name.split(" ").firstOrNull() ?: "",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ServiceCategoryGrid(categories: List<com.pointcheck.features.onboarding.presentation.dto.CategoryDto>, nav: NavController) {
    AppCard {
        Column(Modifier.padding(16.dp)) {
            categories.chunked(4).forEach { rowItems ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    rowItems.forEach { cat ->
                        val icon = CategoryIdentityMapper.mapIcon(cat.icon)
                        CategoryItem(cat.name, icon) {
                            nav.navigate(Screen.Booking.createRoute(null, cat.id))
                        }
                    }
                    // Rellenar con espacios vacíos si la fila no está completa para mantener el alineado
                    repeat(4 - rowItems.size) {
                        Spacer(Modifier.width(64.dp))
                    }
                }
                if (categories.indexOf(rowItems.last()) < categories.size - 1) {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun CategoryItem(name: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(64.dp).clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.padding(12.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            name,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 12.sp
        )
    }
}

@Composable
fun ProfessionalDashboard(r: ReportSummaryResponseDto?, nav: NavController) {
    Column(Modifier.fillMaxWidth()) {
        AppCard {
            Column(Modifier.padding(16.dp)) {
                Text("Resumen de hoy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MetricBox(
                        label = "Citas Hoy", 
                        value = (r?.todayReservations ?: 0).toString(), 
                        icon = Icons.Default.Today,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        label = "Ingresos", 
                        value = "$${r?.totalCharged ?: 0}", 
                        icon = Icons.Default.Payments,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        Text("Rendimiento Semanal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        
        AppCard(onClick = { nav.navigate(Screen.WeeklyReport.route) }) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Insights, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(8.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Ver Reporte Detallado", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text("Tienes ${r?.completedAttentions ?: 0} atenciones completadas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
                }
                
                if (r != null) {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(16.dp))
                    
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Por cobrar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            Text("$${r.pendingAmount}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Tiempo promedio", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            Text("${r.averageAttentionMinutes.toInt()} min", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricBox(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        }
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AdminDashboard(m: DashboardMetricsDto, nav: NavController, onShowUsers: () -> Unit, onShowAudit: () -> Unit, onShowFinance: () -> Unit, onShowSettings: () -> Unit) {
    val s by viewModel<DashboardViewModel>().state.collectAsState()
    
    Column(Modifier.fillMaxWidth()) {
        AppCard {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Analytics, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text("Estado de la Plataforma", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(20.dp))
                
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCardV2("Citas Totales", m.appointmentsToday.toString(), Modifier.weight(1f))
                    MetricCardV2("Usuarios Activos", m.totalAttentionsPerformed.toString(), Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Ingresos Brutos", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        Text("$${String.format("%,.0f", m.paidBillingAmount)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Pendiente Cobro", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        Text("$${String.format("%,.0f", m.pendingBillingAmount)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ErrorRed)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Citas de la Semana (Global)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        
        if (s.adminWeeklyReservations.isEmpty()) {
            AppCard {
                Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("No hay citas registradas para esta semana", color = MaterialTheme.colorScheme.secondary)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                s.adminWeeklyReservations.take(5).forEach { res ->
                    AdminReservationItem(res)
                }
                if (s.adminWeeklyReservations.size > 5) {
                    TextButton(
                        onClick = { /* Navegar a reporte completo */ },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Ver todas las citas")
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Administración del Sistema", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminActionSquare("Usuarios", Icons.Default.People, onShowUsers, Modifier.weight(1f))
            AdminActionSquare("Auditoría", Icons.Default.Shield, onShowAudit, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminActionSquare("Finanzas", Icons.Default.MonetizationOn, onShowFinance, Modifier.weight(1f))
            AdminActionSquare("Ajustes", Icons.Default.Settings, onShowSettings, Modifier.weight(1f))
        }
    }
}

@Composable
fun MetricCardV2(label: String, value: String, modifier: Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AdminReservationItem(res: com.pointcheck.features.reservation.data.dto.ReservationResponseDto) {
    AppCard {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    res.reservationStart.replace("T", " ").substringBeforeLast(":"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                StatusChip(res.status)
            }
            Spacer(Modifier.height(8.dp))
            Text(res.serviceName ?: "Servicio", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(4.dp))
                Text("Cliente: ${res.client.name}", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Default.Work, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(4.dp))
                Text("Atiende: ${res.specialist.name}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun AdminActionSquare(title: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier) {
    AppCard(onClick = onClick, modifier = modifier) {
        Column(
            Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationBottomSheet(
    notifications: List<com.pointcheck.features.dashboard.data.dto.NotificationSummaryDto>,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            Text(
                "Notificaciones",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold
            )
            
            if (notifications.isEmpty()) {
                EmptyState(
                    title = "Sin notificaciones",
                    description = "Te avisaremos cuando haya novedades importantes.",
                    icon = Icons.Default.NotificationsNone
                )
            } else {
                val vm: DashboardViewModel = viewModel()
                notifications.forEach { notification ->
                    NotificationItem(notification) {
                        if (!notification.isRead) vm.markAsRead(notification.id)
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: com.pointcheck.features.dashboard.data.dto.NotificationSummaryDto,
    onClick: () -> Unit
) {
    val color = when (notification.type) {
        "ALERT" -> MaterialTheme.colorScheme.error
        "CONFIRMATION" -> Color(0xFF00A650)
        else -> MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(shape = CircleShape, color = color.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
            Icon(
                if (notification.type == "ALERT") Icons.Default.PriorityHigh else Icons.Default.Notifications, 
                null, 
                tint = color, 
                modifier = Modifier.padding(10.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                notification.title, 
                fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(notification.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                notification.createdAt.replace("T", " ").substringBeforeLast(":"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        if (!notification.isRead) {
            Surface(modifier = Modifier.size(8.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) {}
        }
    }
}

@Composable
fun ShimmerDashboard() {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp)).shimmerEffect())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.weight(1f).height(100.dp).clip(RoundedCornerShape(12.dp)).shimmerEffect())
            Box(Modifier.weight(1f).height(100.dp).clip(RoundedCornerShape(12.dp)).shimmerEffect())
        }
        repeat(3) {
            Box(Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(12.dp)).shimmerEffect())
        }
    }
}

fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000)
        ), label = ""
    )

    background(
        brush = androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(
                Color(0xFFEBEBF4),
                Color(0xFFF4F4F4),
                Color(0xFFEBEBF4),
            ),
            start = androidx.compose.ui.geometry.Offset(startOffsetX, 0f),
            end = androidx.compose.ui.geometry.Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned {
        size = it.size
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogsBottomSheet(onDismiss: () -> Unit) {
    val vm: DashboardViewModel = viewModel()
    val s by vm.state.collectAsState()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            Text("Auditoría", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)

            if (s.auditLogs.isEmpty()) {
                EmptyState(title = "Sin registros", description = "No hay acciones administrativas recientes.", icon = Icons.Default.History)
            } else {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    s.auditLogs.forEach { log ->
                        AuditLogItem(log)
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun AuditLogItem(log: com.pointcheck.features.admin.data.dto.AuditLogDto) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.Top) {
        Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(log.action.replace("_", " "), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Text(log.details ?: "Sin detalles", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(
                "Por: ${log.performedBy.substringBefore("@")} • ${log.timestamp.replace("T", " ").substringBeforeLast(".")}", 
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersBottomSheet(onDismiss: () -> Unit) {
    val vm: DashboardViewModel = viewModel()
    val s by vm.state.collectAsState()
    
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            Text("Usuarios", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
            if (s.adminUsers.isEmpty()) {
                EmptyState(title = "Sin usuarios", description = "No se encontraron usuarios en el sistema.", icon = Icons.Default.People)
            } else {
                s.adminUsers.forEach { user ->
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(user.name, fontWeight = FontWeight.Bold)
                            Text("${user.role} • ${user.email}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = user.active, onCheckedChange = { vm.toggleUserStatus(user.id) })
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
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
    val report = s.financialReport

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(bottom = 40.dp).padding(horizontal = 20.dp)) {
            Text("Balance Global", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            
            if (report == null) {
                Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                AppCard {
                    Column(Modifier.padding(16.dp)) {
                        Text("Ingresos Totales", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        Text("$${report["totalRevenue"]}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(16.dp))
                FinancialRowV2("Transacciones", report["totalTransactions"].toString())
                FinancialRowV2("Cobros Pendientes", "$${report["pendingRevenue"]}")
                
                Spacer(Modifier.height(32.dp))
                AppButton("Exportar Reporte", onClick = { })
            }
        }
    }
}

@Composable
fun FinancialRowV2(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsBottomSheet(onDismiss: () -> Unit) {
    val vm: DashboardViewModel = viewModel()
    val s by vm.state.collectAsState()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(bottom = 40.dp).padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
            Text("Configuración", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            if (s.adminSettings.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                s.adminSettings.forEach { setting ->
                    SettingItemV2(setting) { newValue -> vm.updateSetting(setting.key, newValue) }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                }
            }
            Spacer(Modifier.height(24.dp))
            AppButton("Cerrar", onClick = onDismiss)
        }
    }
}

@Composable
fun SettingItemV2(setting: com.pointcheck.features.dashboard.data.dto.GlobalSettingDto, onUpdate: (String) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(setting.key.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold)
        if (setting.description != null) {
            Text(setting.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(8.dp))
        if (setting.value == "true" || setting.value == "false") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (setting.value == "true") "Activado" else "Desactivado")
                Switch(checked = setting.value == "true", onCheckedChange = { onUpdate(it.toString()) })
            }
        } else {
            var textValue by remember { mutableStateOf(setting.value) }
            AppTextField(value = textValue, onValueChange = { textValue = it }, label = "Valor", trailingIcon = {
                IconButton(onClick = { onUpdate(textValue) }) { Icon(Icons.Default.Save, null, tint = MaterialTheme.colorScheme.primary) }
            })
        }
    }
}
