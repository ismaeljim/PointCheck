package com.pointcheck.features.reservation.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.features.reservation.data.dto.ReservationResponseDto
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentHistoryScreen(
    type: String,
    nav: NavController,
    vm: AppointmentHistoryViewModel = viewModel()
) {
    val s by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(s.error) {
        s.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }

    LaunchedEffect(type) {
        vm.loadAppointments(type)
    }

    val title = when (type) {
        "upcoming" -> "Próximas Citas"
        "recent" -> "Citas Recientes"
        else -> "Historial de Citas"
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { pad ->
        Box(modifier = Modifier.padding(pad).fillMaxSize()) {
            if (s.isLoading && s.appointments.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (s.appointments.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Info, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    Text("No hay citas para mostrar", color = Color.Gray)
                    if (s.error != null) {
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { vm.loadAppointments(type) }) {
                            Text("Reintentar")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(s.appointments) { appointment ->
                        AppointmentItem(appointment)
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentItem(res: ReservationResponseDto) {
    val formattedDate = try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(res.reservationStart)
        if (date != null) outputFormat.format(date) else res.reservationStart
    } catch (e: Exception) {
        res.reservationStart
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Estado: ${res.status}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (res.status) {
                        "PENDING" -> Color(0xFFFFA500)
                        "COMPLETED" -> Color(0xFF4CAF50)
                        "CANCELLED" -> Color(0xFFF44336)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                if (!res.notes.isNullOrEmpty()) {
                    Text(
                        text = res.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}
