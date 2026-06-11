package com.pointcheck.features.reservation.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Payments
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
import com.pointcheck.core.presentation.components.AppButton
import com.pointcheck.core.presentation.components.AppOutlinedButton
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
    // ... resto del código
    val reservations by vm.reservations.collectAsState()
    val attentions by vm.attentions.collectAsState()
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    var userRole by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val userId = prefs.userId.first()
        if (userId != null) {
            vm.loadDualAgenda(userId)
        }
        userRole = prefs.role.first()
    }

    val isSpecialist = userRole?.uppercase() == "SPECIALIST" || userRole?.uppercase() == "PROFESSIONAL"

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Agenda de Citas",
                onBack = { nav.popBackStack() }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
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
            }

            val rawList = if (isSpecialist && selectedTab == 0) attentions else reservations
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

            if (state.isLoading && currentList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (currentList.isEmpty()) {
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
                    AppButton(
                        text = "Nueva Reserva",
                        onClick = { nav.navigate(Screen.Booking.route) },
                        modifier = Modifier.padding(16.dp)
                    )
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
    onAtender: () -> Unit,
    onConfirmPayment: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cita #${res.id.takeLast(8)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                StatusChip(res.status)
            }

            Spacer(Modifier.height(8.dp))

            InfoRow(icon = Icons.Default.MedicalServices, text = res.serviceName ?: "Servicio sin nombre")
            if (isSpecialistView) {
                InfoRow(icon = Icons.Default.Person, text = "Cliente: ${res.client.name}")
            } else {
                InfoRow(icon = Icons.Default.Person, text = "Especialista: ${res.specialist.name}")
            }
            InfoRow(icon = Icons.Default.CalendarToday, text = res.reservationStart.replace("T", " ").substringBeforeLast(":"))

            Spacer(Modifier.height(16.dp))

            val statusUpper = res.status.uppercase()
            val canAction = statusUpper != "COMPLETED" && statusUpper != "CANCELLED"

            if (canAction) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    AppOutlinedButton(
                        text = "Cancelar",
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        contentColor = MaterialTheme.colorScheme.error
                    )

                    if (isSpecialistView) {
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1.5f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppButton(
                                text = "Atender",
                                onClick = onAtender,
                                modifier = Modifier.fillMaxWidth()
                            )
                            AppButton(
                                text = "Cobrar y Finalizar",
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                onClick = onConfirmPayment,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun StatusChip(status: String) {
    val color = when (status.uppercase()) {
        "PENDING", "PENDIENTE" -> MaterialTheme.colorScheme.tertiary
        "CONFIRMED", "CONFIRMADA" -> MaterialTheme.colorScheme.primary
        "COMPLETED", "COMPLETADA" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.extraSmall,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
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
            AppButton(
                text = "Agendar mi primera cita",
                onClick = onNewBooking
            )
        }
    }
}
