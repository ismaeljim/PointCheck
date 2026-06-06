package com.pointcheck.features.reservation.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.core.navigation.Screen
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.core.presentation.components.*
import com.pointcheck.features.reservation.data.dto.ReservationResponseDto
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

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

    LaunchedEffect(Unit) {
        val userId = prefs.userId.first()
        if (userId != null) vm.loadDualAgenda(userId)
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isSpecialist) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color.White
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Mis Atenciones", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Mis Reservas", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            val rawList = if (isSpecialist && selectedTab == 0) attentions else reservations
            val currentList = remember(rawList, filter) {
                if (filter == null) rawList
                else {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val todayStr = sdf.format(Date())
                    val monthPrefix = todayStr.substring(0, 7)
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
                EmptyState(
                    title = if (selectedTab == 0 && isSpecialist) "Sin atenciones" else "Sin citas",
                    description = if (selectedTab == 0 && isSpecialist) "No tienes clientes agendados por ahora." else "Aún no tienes citas programadas.",
                    icon = Icons.Default.EventBusy,
                    actionText = if (userRole?.uppercase() == "CLIENT" || selectedTab == 1) "Agendar Cita" else null,
                    onAction = { nav.navigate(Screen.Booking.route) }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(currentList) { res ->
                        ReservationCardV2(
                            res = res,
                            isSpecialistView = isSpecialist && selectedTab == 0,
                            onAtender = { nav.navigate(Screen.Attention.createRoute(res.id)) },
                            onConfirmPayment = { vm.confirmPayment(res.id) {} },
                            onCancel = { vm.cancelReservation(res.id) }
                        )
                    }
                }
                
                if (userRole?.uppercase() == "CLIENT" || (isSpecialist && selectedTab == 1)) {
                    Box(Modifier.padding(16.dp)) {
                        AppButton(
                            text = "Nueva Reserva",
                            onClick = { nav.navigate(Screen.Booking.route) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReservationCardV2(
    res: ReservationResponseDto,
    isSpecialistView: Boolean,
    onAtender: () -> Unit,
    onConfirmPayment: () -> Unit,
    onCancel: () -> Unit
) {
    AppCard {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = res.reservationStart.replace("T", " ").substringBeforeLast(":"),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                StatusChip(res.status)
            }

            Spacer(Modifier.height(12.dp))

            Text(
                res.serviceName ?: "Servicio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isSpecialistView) "Cliente: ${res.client.name}" else "Especialista: ${res.specialist.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val statusUpper = res.status.uppercase()
            val canAction = statusUpper != "COMPLETED" && statusUpper != "CANCELLED"

            if (canAction) {
                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppOutlinedButton(
                        text = "Cancelar",
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        contentColor = MaterialTheme.colorScheme.error
                    )

                    if (isSpecialistView) {
                        AppButton(
                            text = "Atender",
                            onClick = onAtender,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                if (isSpecialistView && statusUpper == "CONFIRMED") {
                    Spacer(Modifier.height(8.dp))
                    AppButton(
                        text = "Cobrar y Finalizar",
                        containerColor = Color(0xFF00A650),
                        onClick = onConfirmPayment
                    )
                }
            }
        }
    }
}

