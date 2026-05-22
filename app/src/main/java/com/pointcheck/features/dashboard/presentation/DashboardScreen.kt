package com.pointcheck.features.dashboard.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.*
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
import com.pointcheck.features.dashboard.data.dto.ClientDashboardResponseDto
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

    LaunchedEffect(s.error) {
        s.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("PointCheck") },
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
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Mi Perfil") },
                            onClick = { nav.navigate(Screen.Profile.route); showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Refrescar") },
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
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hola, ${s.userName}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Rol: ${s.userRole}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            if (s.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))
            }

            Spacer(Modifier.height(24.dp))

            if (s.userRole == "SPECIALIST" || s.userRole == "PROFESSIONAL") {
                ProfessionalDashboard(s.reportSummary, nav)
            } else {
                ClientDashboardV2(s.clientDashboard, s.weather, nav)
            }

            if (s.error != null) {
                Spacer(Modifier.height(16.dp))
                Button(onClick = { vm.loadDashboard() }, enabled = !s.isLoading) {
                    Text("Reintentar")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationBottomSheet(
    notifications: List<com.pointcheck.features.dashboard.data.dto.NotificationSummaryDto>,
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
    notification: com.pointcheck.features.dashboard.data.dto.NotificationSummaryDto,
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
            Text(
                notification.createdAt.replace("T", " ").substringBeforeLast(":"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun ClientDashboardV2(d: ClientDashboardResponseDto?, weather: WeatherResponseDto?, nav: NavController) {
    Column(Modifier.fillMaxWidth()) {
        if (d?.nextAppointment != null) {
            Text("Tu Próxima Cita", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            FeaturedAppointmentCard(d.nextAppointment, weather, nav)
            Spacer(Modifier.height(24.dp))
        }

        if (d?.favoriteSpecialists?.isNotEmpty() == true) {
            Text("Tus Especialistas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(d.favoriteSpecialists) { specialist ->
                    FavoriteSpecialistCard(specialist) {
                        nav.navigate(Screen.Booking.createRoute(specialist.specialistId))
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        Text("Explorar Servicios", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        ServiceCategoryGrid(nav)

        Spacer(Modifier.height(24.dp))
        DashboardButton("Historial Completo", Icons.Default.History) { 
            nav.navigate(Screen.AppointmentHistory.createRoute("all")) 
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Event,
                            null,
                            modifier = Modifier.padding(12.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            appointment.serviceName ?: "Tu Cita",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            appointment.reservationStart.replace("T", " ").substringBeforeLast(":"),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "con ${appointment.specialistName ?: "Especialista"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Widget de clima real
                if (weather != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${weather.main.temp.toInt()}°C", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            weather.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp
                        )
                        Text(weather.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                } else {
                    // Placeholder mientras carga o si falla
                    Column(horizontalAlignment = Alignment.End) {
                        Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFFBC02D).copy(alpha = 0.5f))
                        Text("--°C", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            val suggestion = when {
                weather?.weather?.firstOrNull()?.description?.contains("rain", ignoreCase = true) == true -> 
                    "¡Va a llover! No olvides tu paraguas para tu cita."
                (weather?.main?.temp ?: 20.0) > 28.0 -> 
                    "¡Día caluroso! Mantente hidratado para tu cita."
                else -> "¡Día ideal para tu cita! Recuerda llegar 5 minutos antes."
            }
            
            Text(
                suggestion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
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
                ) {
                    Icon(Icons.Default.Directions, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cómo llegar")
                }
                OutlinedButton(
                    onClick = { nav.navigate(Screen.Scheduled.route) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Detalles")
                }
            }
        }
    }
}

@Composable
fun FavoriteSpecialistCard(specialist: FavoriteSpecialistDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(140.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    Icons.Default.Person,
                    null,
                    modifier = Modifier.padding(12.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                specialist.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            Text(
                specialist.specialty ?: "Especialista",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ServiceCategoryGrid(nav: NavController) {
    val categories = listOf(
        CategoryData("Barbería", Icons.Default.ContentCut, Color(0xFFFFE0B2)),
        CategoryData("Salud", Icons.Default.MedicalServices, Color(0xFFC8E6C9)),
        CategoryData("Deporte", Icons.Default.FitnessCenter, Color(0xFFB3E5FC)),
        CategoryData("Estética", Icons.Default.Face, Color(0xFFF8BBD0)),
        CategoryData("Bienestar", Icons.Default.SelfImprovement, Color(0xFFD1C4E9)),
        CategoryData("Hogar", Icons.Default.Home, Color(0xFFF5F5F5))
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        categories.chunked(2).forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { cat ->
                    CategoryCard(cat, Modifier.weight(1f)) {
                        nav.navigate(Screen.Booking.route)
                    }
                }
            }
        }
    }
}

data class CategoryData(val name: String, val icon: ImageVector, val color: Color)

@Composable
fun CategoryCard(cat: CategoryData, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = cat.color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                cat.icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = Color.DarkGray
            )
            Spacer(Modifier.height(8.dp))
            Text(
                cat.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun ClientDashboard(m: DashboardMetricsDto, nav: NavController) {
    Column(Modifier.fillMaxWidth()) {
        Text("Resumen de Actividad", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard(
                label = "Próximas", 
                value = m.upcomingReservationsCount.toString(), 
                icon = Icons.Default.Event, 
                modifier = Modifier.weight(1f),
                onClick = { nav.navigate(Screen.AppointmentHistory.createRoute("upcoming")) }
            )
            MetricCard(
                label = "Recientes", 
                value = m.recentReservationsCount.toString(), 
                icon = Icons.Default.History, 
                modifier = Modifier.weight(1f),
                onClick = { nav.navigate(Screen.AppointmentHistory.createRoute("recent")) }
            )
        }
        
        Spacer(Modifier.height(24.dp))
        
        DashboardButton("Mis Citas", Icons.Default.CalendarMonth) { nav.navigate(Screen.Scheduled.route) }
        DashboardButton("Nueva Reserva", Icons.Default.AddCircle) { nav.navigate(Screen.Booking.route) }
    }
}

@Composable
fun ProfessionalDashboard(r: ReportSummaryResponseDto?, nav: NavController) {
    Column(Modifier.fillMaxWidth()) {
        Text("Panel de Control", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        
        if (r != null) {
            // Métricas operacionales rápidas (No clickeables para evitar redundancia)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("Citas Mes", r.totalReservations.toString(), Icons.Default.Assessment, Modifier.weight(1f))
                MetricCard("Hoy", r.todayReservations.toString(), Icons.Default.Today, Modifier.weight(1f))
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Card Principal de Reporte: Único punto de acceso a detalles y finanzas
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { nav.navigate(Screen.WeeklyReport.route) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Insights, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Reporte de Desempeño", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    FinancialRow("Ingresos del Mes", "$${r.totalCharged}", MaterialTheme.colorScheme.primary)
                    FinancialRow("Por Cobrar", "$${r.pendingAmount}", MaterialTheme.colorScheme.error)
                    FinancialRow("Tiempo Promedio", "${r.averageAttentionMinutes.toInt()} min")
                    
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Ver desglose semanal y estadísticas",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Accesos Rápidos", style = MaterialTheme.typography.titleMedium)
        
        DashboardButton("Perfil Profesional", Icons.Default.Person) { nav.navigate(Screen.ProfessionalProfile.route) }
        DashboardButton("Mi Agenda", Icons.Default.CalendarMonth) { nav.navigate(Screen.Scheduled.route) }
        DashboardButton("Servicios", Icons.AutoMirrored.Filled.List) { nav.navigate(Screen.ServiceManagement.route) }
        DashboardButton("Suscripción", Icons.Default.Star) { nav.navigate(Screen.Subscription.route) }
    }
}

@Composable
fun FinancialRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun MetricCard(
    label: String, 
    value: String, 
    icon: ImageVector, 
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    icon,
                    null,
                    modifier = Modifier.padding(8.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun DashboardButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        contentPadding = PaddingValues(16.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Icon(icon, null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}
