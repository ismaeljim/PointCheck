package com.pointcheck.features.reservation.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.core.navigation.Screen
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.core.ui.components.PCButton
import com.pointcheck.core.ui.components.PCCard
import com.pointcheck.core.ui.components.PCOutlinedButton
import com.pointcheck.core.ui.components.PCStatusChip
import com.pointcheck.core.ui.components.PCShimmer
import com.pointcheck.core.presentation.components.AppTopBar
import com.pointcheck.features.reservation.data.dto.ReservationResponseDto
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

/**
 * PointCheck Scheduled Screen - GESTIÓN DE AGENDA DUAL
 * 
 * ¿POR QUÉ "AGENDA DUAL"?
 * En PointCheck, un Especialista no solo atiende clientes, sino que también puede ser cliente 
 * de otros profesionales. Una agenda única causaría confusión sobre si el usuario es quien 
 * debe dar el servicio o recibirlo.
 * 
 * IMPLEMENTACIÓN:
 * 1. TAB SEPARATION: Si el rol es SPECIALIST, habilitamos pestañas ("Mis Atenciones" vs "Mis Reservas").
 * 2. ACTION LOGIC: 
 *    - En "Mis Atenciones", el profesional tiene botones de "Atender" y "Cobrar".
 *    - En "Mis Reservas", el usuario solo tiene la opción de "Cancelar", comportándose como un cliente.
 * 
 * ESTO RESOLVIÓ: La ambigüedad de roles en la misma pantalla y centralizó la gestión 
 * de tiempos del profesional en un solo lugar.
 */
/**
 * Screen for managing scheduled appointments and client service sessions.
 *
 * This screen implements a "Dual Agenda" logic:
 * 1. For Specialists/Professionals: It provides two tabs—"Mis Atenciones" (services they provide)
 *    and "Mis Reservas" (services they receive).
 * 2. For Clients: It displays a single list of their upcoming appointments.
 *
 * The screen allows specialists to initiate service sessions ("Atender") or confirm payments,
 * while clients can view details and cancel reservations.
 *
 * @param nav [NavController] for navigating between screens (e.g., to Attention or Booking).
 * @param filter Optional string to filter appointments by time (e.g., "today", "month").
 * @param vm [ReservationViewModel] providing the agenda data and management actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduledScreen(nav: NavController, filter: String? = null, vm: ReservationViewModel = viewModel()) {
    val reservations by vm.reservations.collectAsState()
    val attentions by vm.attentions.collectAsState()
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    var userRole by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedReservation by remember { mutableStateOf<ReservationResponseDto?>(null) }

    LaunchedEffect(Unit) {
        val userId = prefs.userId.first()
        if (userId != null) {
            vm.loadDualAgenda(userId)
        }
        userRole = prefs.role.first()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Agenda de Citas",
                onBack = { nav.popBackStack() }
            )
        }
    ) { pad ->
        if (selectedReservation != null) {
            ReservationDetailBottomSheet(
                res = selectedReservation!!,
                userRole = userRole,
                onDismiss = { selectedReservation = null },
                onAtender = {
                    val resId = selectedReservation!!.id
                    selectedReservation = null
                    nav.navigate(Screen.Attention.createRoute(resId))
                },
                onCancel = {
                    vm.cancelReservation(selectedReservation!!.id)
                    selectedReservation = null
                }
            )
        }

        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            val isSpecialist = userRole?.uppercase() == "SPECIALIST" || userRole?.uppercase() == "PROFESSIONAL"
            val isAdmin = userRole?.uppercase() == "ADMIN"

            if (isSpecialist) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Mis Atenciones") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Mis Reservas") }
                    )
                }
            } else if (isAdmin) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Modo Supervisión Global (ADMIN)",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            val rawList = if (isSpecialist && selectedTab == 0) attentions else reservations
            
            // UI de carga optimizada para evitar flasheo
            if (state.isLoading && rawList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        repeat(5) {
                            PCShimmer(modifier = Modifier.fillMaxWidth().height(180.dp))
                        }
                    }
                }
            } else {
                val currentList = remember(rawList, filter) {
                    if (filter == null) rawList
                    else {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val now = Calendar.getInstance()
                        val todayStr = sdf.format(now.time)
                        val monthPrefix = todayStr.substring(0, 7) // "YYYY-MM"

                        rawList.filter { res ->
                            when (filter) {
                                "today" -> res.reservationStart.startsWith(todayStr)
                                "month" -> res.reservationStart.startsWith(monthPrefix)
                                else -> true
                            }
                        }
                    }
                }

                if (currentList.isEmpty()) {
                    EmptyReservationsState(
                        userRole = userRole,
                        isAttentionsTab = isSpecialist && selectedTab == 0,
                        onNewBooking = { nav.navigate(Screen.Booking.route) }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(currentList) { res ->
                            ReservationCard(
                            res = res,
                            userRole = userRole,
                            isSpecialistView = isSpecialist && selectedTab == 0,
                            onClick = { selectedReservation = res },
                            onAtender = {
                                nav.navigate(
                                    Screen.Attention.createRoute(res.id)
                                )
                            },
                            onConfirmPayment = {
                                vm.confirmPayment(res.id) {
                                    // El dashboard se recargará automáticamente al volver por el LaunchedEffect en DashboardScreen
                                }
                            },
                            onCancel = { vm.cancelReservation(res.id) }
                        )
                        }
                    }
                    
                    // Solo mostramos "Nueva Reserva" si el usuario es CLIENTE o está en su pestaña de reservas
                    if (userRole?.uppercase() == "CLIENT" || (isSpecialist && selectedTab == 1)) {
                        PCButton(
                            text = "Nueva Reserva",
                            onClick = { nav.navigate(Screen.Booking.route) },
                            modifier = Modifier.padding(16.dp).fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReservationCard(
    res: ReservationResponseDto,
    userRole: String?,
    isSpecialistView: Boolean = false,
    onClick: () -> Unit,
    onAtender: () -> Unit,
    onConfirmPayment: () -> Unit,
    onCancel: () -> Unit
) {
    val isAdmin = userRole?.uppercase() == "ADMIN"
    
    PCCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cita #${res.id.takeLast(8)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = res.serviceName ?: "Servicio sin nombre",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                StatusChip(res.status)
            }

            Spacer(Modifier.height(12.dp))

            // Badge de Domicilio
            if (res.isAtHome) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.extraSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "SERVICIO A DOMICILIO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(12.dp))

            if (isAdmin) {
                InfoRow(
                    icon = Icons.Default.Person,
                    text = "Cliente: ${res.client.name}",
                    fontWeight = FontWeight.SemiBold
                )
                InfoRow(
                    icon = Icons.Default.MedicalServices,
                    text = "Especialista: ${res.specialist.name}",
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                InfoRow(
                    icon = Icons.Default.Person,
                    text = if (isSpecialistView) "Cliente: ${res.client.name}" else "Especialista: ${res.specialist.name}",
                    fontWeight = FontWeight.SemiBold
                )
            }

            InfoRow(
                icon = Icons.Default.CalendarToday,
                text = formatDateTime(res.reservationStart),
                color = MaterialTheme.colorScheme.primary
            )

            if (!res.address.isNullOrBlank()) {
                InfoRow(icon = Icons.Default.LocationOn, text = res.address)
            }

            val contactInfo = if (isSpecialistView) res.client.phone else res.specialist.phone
            if (!contactInfo.isNullOrBlank()) {
                InfoRow(icon = Icons.Default.Phone, text = contactInfo)
            }

            if (!res.notes.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(8.dp)) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            res.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            val isPast = try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val date = inputFormat.parse(res.reservationStart)
                date?.before(Date()) ?: false
            } catch (e: Exception) {
                false
            }

            val statusUpper = res.status.uppercase()
            val isCancelled = statusUpper == "CANCELLED" || statusUpper == "CANCELADA"
            val isCompleted = statusUpper == "COMPLETED" || statusUpper == "COMPLETADA"
            
            val canCancel = !isPast && !isCancelled && !isCompleted
            val canAction = !isCancelled && !isCompleted

            if (canAction) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (canCancel) {
                        PCOutlinedButton(
                            text = "Cancelar",
                            onClick = onCancel,
                            modifier = Modifier.weight(1f)
                        )
                    } else if (isPast && (statusUpper == "PENDING" || statusUpper == "PENDIENTE")) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "EXPIRADA (NO-SHOW)",
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (isSpecialistView && !isPast) {
                        PCButton(
                            text = "Atender",
                            onClick = onAtender,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (isSpecialistView && statusUpper == "CONFIRMED" && !isPast) {
                    Spacer(Modifier.height(8.dp))
                    PCButton(
                        text = "Cobrar y Finalizar",
                        icon = Icons.Default.Payments,
                        onClick = onConfirmPayment,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else if (isCancelled && isPast) {
                // Feedback para citas que el backend ya canceló por tiempo
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "ESTA CITA FUE CANCELADA POR EXPIRACIÓN DE TIEMPO",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector, 
    text: String, 
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 3.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = color.copy(alpha = 0.7f)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text, 
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = fontWeight
        )
    }
}

fun formatDateTime(isoString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = inputFormat.parse(isoString)
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
        date?.let { outputFormat.format(it) } ?: isoString.replace("T", " ").substringBeforeLast(":")
    } catch (e: Exception) {
        isoString.replace("T", " ").substringBeforeLast(":")
    }
}

@Composable
fun StatusChip(status: String) {
    val (containerColor, contentColor) = when (status.uppercase()) {
        "PENDING", "PENDIENTE" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        "CONFIRMED", "CONFIRMADA" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        "COMPLETED", "COMPLETADA" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    PCStatusChip(
        text = status,
        containerColor = containerColor,
        contentColor = contentColor
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDetailBottomSheet(
    res: ReservationResponseDto,
    userRole: String?,
    onDismiss: () -> Unit,
    onAtender: () -> Unit,
    onCancel: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Detalles de la Cita",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(16.dp))

            // ... (Contenido similar a la card pero extendido si fuera necesario)
            Text("Información General", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            
            InfoRow(Icons.Default.MedicalServices, res.serviceName, MaterialTheme.colorScheme.primary, FontWeight.Bold)
            InfoRow(Icons.Default.Person, if (userRole == "CLIENT") "Especialista: ${res.specialist.name}" else "Cliente: ${res.client.name}")
            InfoRow(Icons.Default.CalendarToday, formatDateTime(res.reservationStart))
            if (res.address.isNotBlank()) {
                InfoRow(Icons.Default.LocationOn, res.address)
            }
            
            if (res.notes.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text("Notas del Cliente", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    Text(res.notes, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(32.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PCOutlinedButton(
                    text = "Cerrar",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                // Aquí podrías agregar botones de acción según el estado si no estuvieran en la card
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun EmptyReservationsState(
    userRole: String?,
    isAttentionsTab: Boolean = false,
    onNewBooking: () -> Unit
) {
    val isSpecialist = userRole?.uppercase() == "SPECIALIST"
    
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.EventBusy,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(Modifier.height(16.dp))
        Text(
            if (isAttentionsTab) "No tienes atenciones pendientes" 
            else if (isSpecialist) "Tu agenda de reservas está libre" 
            else "No tienes citas programadas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (isAttentionsTab) "No tienes clientes agendados para este periodo."
            else if (isSpecialist) "No tienes citas para este periodo. ¡Buen momento para descansar o promocionar tus servicios!" 
            else "Cuando agendes una cita, aparecerá aquí para que puedas gestionarla.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        if (!isSpecialist || !isAttentionsTab) {
            Spacer(Modifier.height(24.dp))
            PCButton(
                text = "Agendar mi primera cita",
                onClick = onNewBooking,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
