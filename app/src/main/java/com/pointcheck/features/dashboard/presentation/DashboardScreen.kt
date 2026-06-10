package com.pointcheck.features.dashboard.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.SelfImprovement
import com.pointcheck.core.presentation.components.AppButton
import com.pointcheck.core.presentation.components.AppTextField
import com.pointcheck.core.presentation.components.AppTopBar
import com.pointcheck.core.presentation.components.AppOutlinedButton
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.composed
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
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
import com.pointcheck.core.utils.CategoryIdentityMapper
import com.pointcheck.features.dashboard.data.dto.ClientDashboardResponseDto
import com.pointcheck.features.dashboard.data.dto.DashboardMetricsDto
import com.pointcheck.features.dashboard.data.dto.FavoriteSpecialistDto
import com.pointcheck.features.dashboard.data.dto.ReportSummaryResponseDto
import com.pointcheck.features.external.data.dto.WeatherResponseDto
import com.pointcheck.features.reservation.data.dto.ReservationResponseDto

/**
 * Pantalla principal del tablero que muestra contenido de forma adaptativa según el rol del usuario.
 *
 * Actúa como el centro neurálgico de la aplicación, proporcionando:
 * - Controles de administración para la supervisión de la plataforma.
 * - Métricas profesionales y gestión de agenda para especialistas.
 * - Exploración de servicios y próximas citas para clientes.
 *
 * Puntos de integración:
 * - Datos climáticos en tiempo real para las próximas citas.
 * - Integración con mapas para navegación.
 * - Gestión de notificaciones mediante BottomSheets.
 *
 * @param nav Controlador de navegación para las transiciones entre pantallas.
 * @param vm ViewModel que gestiona el estado del tablero y la obtención de datos basada en el rol.
 */
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val isSpecialist = s.userRole == "SPECIALIST" || s.userRole == "PROFESSIONAL"
            
            Text(
                text = if (isSpecialist) "Bienvenido, ${s.userName}" else "Hola, ${s.userName}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isSpecialist) (s.reportSummary?.specialty ?: s.userRole) else "Rol: ${s.userRole}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            if (s.isLoading) {
                ShimmerDashboard()
            } else {
                Spacer(Modifier.height(24.dp))

                if (s.userRole == "ADMIN") {
                    AdminDashboard(
                        m = s.metrics,
                        nav = nav,
                        onShowUsers = { showAdminUsers = true },
                        onShowAudit = { showAuditLogs = true },
                        onShowFinance = { showAdminFinance = true },
                        onShowSettings = { showAdminSettings = true }
                    )
                } else if (s.userRole == "SPECIALIST" || s.userRole == "PROFESSIONAL") {
                    ProfessionalDashboard(s.reportSummary, nav)
                } else {
                    ClientDashboardV2(s, nav)
                }
            }

            if (s.error != null) {
                Spacer(Modifier.height(16.dp))
                AppButton(text = "Reintentar", onClick = { vm.loadDashboard() }, enabled = !s.isLoading)
            }
        }
    }
}

/**
 * Muestra una lista de notificaciones recientes en una hoja modal inferior (bottom sheet).
 *
 * @param notifications Lista de resúmenes de notificaciones a mostrar.
 * @param onDismiss Callback para cerrar la hoja modal.
 */
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

/**
 * Renderiza un elemento individual de notificación.
 *
 * @param notification Los datos de la notificación.
 * @param onClick Callback que se dispara cuando se toca la notificación.
 */
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

/**
 * Diseño del tablero adaptado para usuarios de tipo Cliente.
 *
 * Muestra las próximas citas con integración de clima, especialistas favoritos
 * y una cuadrícula de categorías de servicios para exploración.
 *
 * @param s Estado actual de la UI del tablero.
 * @param nav Controlador de navegación.
 */
@Composable
fun ClientDashboardV2(s: DashboardUiState, nav: NavController) {
    val d = s.clientDashboard
    val weather = s.weather
    Column(Modifier.fillMaxWidth()) {
        if (d?.nextAppointment != null) {
            Text("Tu Próxima Cita", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            FeaturedAppointmentCard(d.nextAppointment, weather, nav)
            Spacer(Modifier.height(24.dp))
        }

        Text("Tus Especialistas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        if (d?.favoriteSpecialists?.isNotEmpty() == true) {
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
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.FavoriteBorder, 
                        null, 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Aún no tienes favoritos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Explora las categorías abajo para encontrar y guardar a tus especialistas de confianza.", 
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        Text("Explorar Servicios", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (s.categories.isEmpty() && s.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            ServiceCategoryGrid(s.categories, nav)
        }

        Spacer(Modifier.height(24.dp))
        AppOutlinedButton(
            text = "Historial Completo",
            icon = Icons.Default.History,
            onClick = { nav.navigate(Screen.AppointmentHistory.createRoute("all")) }
        )
    }
}

/**
 * Una tarjeta prominente que destaca la próxima cita programada.
 *
 * Integra datos climáticos externos para la ubicación de la cita y proporciona
 * acciones rápidas para navegación y visualización de detalles.
 *
 * @param appointment Los datos de la reservación.
 * @param weather Datos climáticos en tiempo real para la ciudad de la cita.
 * @param nav Controlador de navegación.
 */
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
                            "con ${appointment.specialist.name}",
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
fun ServiceCategoryGrid(categories: List<com.pointcheck.features.onboarding.presentation.dto.CategoryDto>, nav: NavController) {
    if (categories.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Category, null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text("Cargando categorías...", color = MaterialTheme.colorScheme.secondary)
            }
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 12.dp)) {
        categories.chunked(2).forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { cat ->
                    val icon = CategoryIdentityMapper.mapIcon(cat.icon)
                    
                    CategoryCard(cat.name, icon, Modifier.weight(1f)) {
                        nav.navigate(Screen.Booking.createRoute(null, cat.id))
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCard(name: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
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
        
        AppOutlinedButton(text = "Mis Citas", icon = Icons.Default.CalendarMonth, onClick = { nav.navigate(Screen.Scheduled.route) })
        Spacer(Modifier.height(8.dp))
        AppOutlinedButton(text = "Nueva Reserva", icon = Icons.Default.AddCircle, onClick = { nav.navigate(Screen.Booking.route) })
    }
}

@Composable
fun ProfessionalDashboard(r: ReportSummaryResponseDto?, nav: NavController) {
    Column(Modifier.fillMaxWidth()) {
        Text("Panel de Control", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        
        if (r != null) {
            // Métricas operacionales rápidas (Clickable para navegación filtrada)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard(
                    label = "Citas Mes", 
                    value = (r.totalReservations).toString(), 
                    icon = Icons.Default.Assessment, 
                    modifier = Modifier.weight(1f),
                    onClick = { nav.navigate(Screen.Scheduled.createRoute("month")) }
                )
                MetricCard(
                    label = "Hoy", 
                    value = (r.todayReservations).toString(), 
                    icon = Icons.Default.Today, 
                    modifier = Modifier.weight(1f),
                    onClick = { nav.navigate(Screen.Scheduled.createRoute("today")) }
                )
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
        Spacer(Modifier.height(8.dp))
        
        AppOutlinedButton(text = "Perfil Profesional", icon = Icons.Default.Person, onClick = { nav.navigate(Screen.ProfessionalProfile.route) })
        Spacer(Modifier.height(8.dp))
        AppOutlinedButton(text = "Mi Agenda", icon = Icons.Default.CalendarMonth, onClick = { nav.navigate(Screen.Scheduled.route) })
        Spacer(Modifier.height(8.dp))
        AppOutlinedButton(text = "Servicios", icon = Icons.AutoMirrored.Filled.List, onClick = { nav.navigate(Screen.ServiceManagement.route) })
        Spacer(Modifier.height(8.dp))
        AppOutlinedButton(text = "Suscripción", icon = Icons.Default.Star, onClick = { nav.navigate(Screen.Subscription.route) })
    }
}

@Composable
fun AdminDashboard(m: DashboardMetricsDto, nav: NavController, onShowUsers: () -> Unit, onShowAudit: () -> Unit, onShowFinance: () -> Unit, onShowSettings: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = "Panel de Control Maestro",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Supervisión global de la plataforma",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.15f)),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiary,
                        shape = androidx.compose.foundation.shape.CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Analytics, 
                            null, 
                            tint = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("Métricas del Ecosistema", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(20.dp))
                
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard(
                        label = "Citas Totales", 
                        value = m.appointmentsToday.toString(), 
                        icon = Icons.AutoMirrored.Filled.EventNote, 
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Ingresos Netos", 
                        value = "$${String.format("%,.0f", m.paidBillingAmount)}", 
                        icon = Icons.Default.MonetizationOn, 
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard(
                        label = "Usuarios Clientes", 
                        value = m.totalAttentionsPerformed.toString(), 
                        icon = Icons.Default.People, 
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        label = "Especialistas", 
                        value = m.averageDurationMinutes.toInt().toString(), 
                        icon = Icons.Default.Engineering, 
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Accesos Directos Administrativos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        
        AdminActionButton("Auditoría de Usuarios", Icons.Default.SupervisorAccount, "Ver estados y roles", onShowUsers)
        AdminActionButton("Historial de Cambios", Icons.Default.HistoryEdu, "Log de acciones administrativas", onShowAudit)
        AdminActionButton("Reportes Financieros", Icons.Default.Payments, "Exportar balances globales", onShowFinance)
        AdminActionButton("Configuración de Red", Icons.Default.Settings, "Parámetros del sistema", onShowSettings)
    }
}

@Composable
fun AdminActionButton(title: String, icon: ImageVector, subtitle: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogsBottomSheet(onDismiss: () -> Unit) {
    val vm: DashboardViewModel = viewModel()
    val s by vm.state.collectAsState()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                "Historial de Auditoría",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold
            )

            if (s.auditLogs.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No hay registros de auditoría", color = MaterialTheme.colorScheme.secondary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    s.auditLogs.forEach { log ->
                        AuditLogItem(log)
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AuditLogItem(log: com.pointcheck.features.admin.data.dto.AuditLogDto) {
    val icon = when (log.action) {
        "ACTIVATE_USER" -> Icons.Default.PersonAdd
        "DEACTIVATE_USER" -> Icons.Default.PersonRemove
        "UPDATE_SETTING" -> Icons.Default.SettingsSuggest
        else -> Icons.Default.History
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(log.action.replace("_", " "), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Text(log.details ?: "Sin detalles", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AdminPanelSettings, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(4.dp))
                Text(
                    "Por: ${log.performedBy.substringBefore("@")}", 
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(4.dp))
                Text(
                    log.timestamp.replace("T", " ").substringBeforeLast("."), 
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
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
fun ShimmerDashboard() {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(24.dp))
                .shimmerEffect()
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.weight(1f).height(100.dp).clip(RoundedCornerShape(16.dp)).shimmerEffect())
            Box(Modifier.weight(1f).height(100.dp).clip(RoundedCornerShape(16.dp)).shimmerEffect())
        }
        repeat(3) {
            Box(Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)).shimmerEffect())
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
    com.pointcheck.core.presentation.components.AppOutlinedButton(
        text = text,
        icon = icon,
        onClick = onClick,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersBottomSheet(onDismiss: () -> Unit) {
    val vm: DashboardViewModel = viewModel()
    val s by vm.state.collectAsState()
    
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            Text("Gestión de Usuarios", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
            
            if (s.adminUsers.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No se encontraron usuarios", color = MaterialTheme.colorScheme.secondary)
                }
            } else {
                s.adminUsers.forEach { user ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(user.name, fontWeight = FontWeight.Bold)
                            Text("${user.role} • ${user.email}", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = user.active,
                            onCheckedChange = { vm.toggleUserStatus(user.id) }
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
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
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            } else {
                MetricCard("Ingresos Totales", "$${report["totalRevenue"]}", Icons.Default.MonetizationOn, Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                FinancialRow("Transacciones", report["totalTransactions"].toString())
                FinancialRow("Cobros Pendientes", "$${report["pendingRevenue"]}", MaterialTheme.colorScheme.error)
                FinancialRow("Pagos Realizados", report["paidTransactions"].toString(), Color(0xFF4CAF50))
                
                Spacer(Modifier.height(32.dp))
                AppButton("Descargar Reporte PDF", onClick = { /* Simular descarga */ })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsBottomSheet(onDismiss: () -> Unit) {
    val vm: DashboardViewModel = viewModel()
    val s by vm.state.collectAsState()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Configuración Global",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            if (s.adminSettings.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                s.adminSettings.forEach { setting ->
                    SettingItem(setting) { newValue ->
                        vm.updateSetting(setting.key, newValue)
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                }
            }

            Spacer(Modifier.height(24.dp))
            AppButton("Cerrar", onClick = onDismiss)
        }
    }
}

@Composable
fun SettingItem(
    setting: com.pointcheck.features.dashboard.data.dto.GlobalSettingDto,
    onUpdate: (String) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            setting.key.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        if (setting.description != null) {
            Text(setting.description, style = MaterialTheme.typography.bodySmall)
        }
        
        Spacer(Modifier.height(8.dp))
        
        // Determinar si es un booleano o un texto/número
        if (setting.value == "true" || setting.value == "false") {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (setting.value == "true") "Activado" else "Desactivado")
                Switch(
                    checked = setting.value == "true",
                    onCheckedChange = { onUpdate(it.toString()) }
                )
            }
        } else {
            var textValue by remember { mutableStateOf(setting.value) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    label = "",
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onUpdate(textValue) }) {
                    Icon(Icons.Default.Save, "Guardar", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
