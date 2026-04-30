package com.pointcheck.features.reservation.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.core.navigation.Screen
import com.pointcheck.core.prefs.UserPreferences
import kotlinx.coroutines.flow.first
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
        topBar = { TopAppBar(title = { Text("Citas Programadas") }) }
    ) { pad ->
        Column(modifier = Modifier.padding(pad).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            
            if (state.isLoading && reservations.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (reservations.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("No tienes citas programadas", modifier = Modifier.padding(16.dp))
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    items(reservations) { res ->
                        ListItem(
                            headlineContent = { 
                                Text("Servicio #${res.serviceId ?: "N/A"}") 
                            },
                            supportingContent = { 
                                Column {
                                    Text("Especialista #${res.specialistId}")
                                    Text("Fecha: ${res.reservationStart}", style = MaterialTheme.typography.bodySmall)
                                    Text("Estado: ${res.status}", color = MaterialTheme.colorScheme.primary)
                                }
                            },
                            trailingContent = {
                                Column(horizontalAlignment = Alignment.End) {
                                    if (userRole == "SPECIALIST" || userRole == "PROFESSIONAL") {
                                        Button(
                                            onClick = {
                                                nav.navigate(
                                                    Screen.Attention.createRoute(
                                                        res.id,
                                                        res.clientId,
                                                        res.specialistId
                                                    )
                                                )
                                            },
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        ) {
                                            Text("Atender")
                                        }
                                    }
                                    TextButton(onClick = { vm.cancelReservation(res.id) }) {
                                        Text("Cancelar", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
            
            Button(
                onClick = { nav.navigate(Screen.Booking.route) },
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                Text("Nueva Reserva")
            }
        }
    }
}
