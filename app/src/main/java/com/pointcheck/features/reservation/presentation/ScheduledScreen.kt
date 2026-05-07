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
import com.pointcheck.features.reservation.data.dto.ReservationResponseDto
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduledScreen(nav: NavController, vm: ReservationViewModel = viewModel()) {
    val reservations by vm.reservations.collectAsState()
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    var userRole by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        vm.loadMyReservations()
        userRole = prefs.role.first()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agenda de Citas") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            if (state.isLoading && reservations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (reservations.isEmpty()) {
                EmptyReservationsState(onNewBooking = { nav.navigate(Screen.Booking.route) })
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reservations) { res ->
                        ReservationCard(
                            res = res,
                            userRole = userRole,
                            onAtender = {
                                nav.navigate(
                                    Screen.Attention.createRoute(
                                        res.id,
                                        res.clientId,
                                        res.specialistId
                                    )
                                )
                            },
                            onCancel = { vm.cancelReservation(res.id) }
                        )
                    }
                }
                
                Button(
                    onClick = { nav.navigate(Screen.Booking.route) },
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Nueva Reserva")
                }
            }
        }
    }
}

@Composable
fun ReservationCard(
    res: ReservationResponseDto,
    userRole: String?,
    onAtender: () -> Unit,
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
                    text = "Cita #${res.id}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                StatusChip(res.status)
            }

            Spacer(Modifier.height(8.dp))

            InfoRow(icon = Icons.Default.MedicalServices, text = "Servicio #${res.serviceId ?: "N/A"}")
            InfoRow(icon = Icons.Default.Person, text = "Especialista #${res.specialistId}")
            InfoRow(icon = Icons.Default.CalendarToday, text = res.reservationStart)

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cancelar")
                }

                if (userRole == "SPECIALIST" || userRole == "PROFESSIONAL") {
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onAtender) {
                        Text("Atender")
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
fun EmptyReservationsState(onNewBooking: () -> Unit) {
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
            "No tienes citas programadas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Cuando agendes una cita, aparecerá aquí para que puedas gestionarla.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onNewBooking) {
            Text("Agendar mi primera cita")
        }
    }
}
