package com.pointcheck.features.reservation.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.core.navigation.Screen
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.core.presentation.components.AppButton
import com.pointcheck.core.presentation.components.HeaderIcon
import com.pointcheck.core.presentation.components.SectionHeader
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
        val userId = prefs.userId.first()
        if (userId != null) {
            vm.loadMyReservations(userId)
        }
        userRole = prefs.role.first()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- HEADER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderIcon(Icons.AutoMirrored.Filled.ArrowBack) { nav.popBackStack() }
                Spacer(Modifier.width(16.dp))
                Text(
                    "Agenda Activa",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        // --- CONTENT ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-20).dp)
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                if (state.isLoading && reservations.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                    }
                } else if (reservations.isEmpty()) {
                    EmptyReservationsState(onNewBooking = { nav.navigate(Screen.Booking.route) })
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            SectionHeader("Próximos compromisos")
                        }
                        
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
                        
                        item {
                            Spacer(Modifier.height(80.dp))
                        }
                    }
                }
            }
            
            // Floating Action Button Style
            ExtendedFloatingActionButton(
                onClick = { nav.navigate(Screen.Booking.route) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Nueva Cita")
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dateStr = res.reservationStart.replace("T", " ").substringBeforeLast(":")
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                StatusChip(res.status)
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = res.serviceName ?: "Servicio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black
            )
            
            Spacer(Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Person, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (userRole?.uppercase() == "CLIENT") "Especialista: ${res.specialistName}" else "Cliente ID: ${res.clientId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                ) {
                    Text("Cancelar")
                }

                val roleUpper = userRole?.uppercase()?.trim()
                val isWorker = roleUpper == "SPECIALIST" || roleUpper == "PROFESSIONAL" || roleUpper == "ADMIN"
                val canAtender = res.status.uppercase() !in listOf("COMPLETED", "CANCELLED")
                
                if (isWorker && canAtender) {
                    Button(
                        onClick = onAtender,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Atender")
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val color = when (status.uppercase()) {
        "PENDING", "PENDIENTE" -> Color(0xFFFB8C00)
        "CONFIRMED", "CONFIRMADA" -> Color(0xFF1976D2)
        "COMPLETED", "COMPLETADA" -> Color(0xFF43A047)
        "CANCELLED", "CANCELADA" -> Color(0xFFE53935)
        else -> Color.Gray
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Black
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
            Icons.Outlined.EventBusy,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Tu agenda está libre",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Aún no tienes citas programadas para hoy.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        AppButton(text = "Agendar una cita", onClick = onNewBooking)
    }
}
