package com.pointcheck.features.booking.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.core.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduledScreen(nav: NavController, vm: ReservationViewModel = viewModel()) {
    val reservations by vm.reservations.collectAsState(initial = emptyList())
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Citas Programadas") }) }
    ) { pad ->
        Column(modifier = Modifier.padding(pad).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            if (reservations.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("No tienes citas programadas", modifier = Modifier.padding(16.dp))
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    items(reservations) { res ->
                        ListItem(
                            headlineContent = { Text(res.name) },
                            supportingContent = { Text(sdf.format(Date(res.epochMillis))) },
                            trailingContent = {
                                IconButton(onClick = { vm.deleteReservation(res.id) }) {
                                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
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
