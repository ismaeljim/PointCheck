package com.pointcheck.features.reservation.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.core.presentation.components.*
import com.pointcheck.core.utils.CategoryIdentityMapper
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

    LaunchedEffect(type) {
        vm.loadAppointments(type)
    }

    val title = when (type) {
        "upcoming" -> "Próximas Citas"
        "recent" -> "Citas Recientes"
        else -> "Historial"
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = title,
                onBack = { nav.popBackStack() }
            )
        }
    ) { pad ->
        Box(modifier = Modifier.padding(pad).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            if (s.isLoading && s.appointments.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (s.appointments.isEmpty()) {
                EmptyState(
                    title = "Sin actividad",
                    description = "No encontramos citas en esta sección.",
                    icon = Icons.Default.History
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(s.appointments) { appointment ->
                        HistoryItem(appointment)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(res: ReservationResponseDto) {
    val formattedDate = try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd 'de' MMM, HH:mm", Locale.getDefault())
        val date = inputFormat.parse(res.reservationStart)
        if (date != null) outputFormat.format(date) else res.reservationStart
    } catch (e: Exception) {
        res.reservationStart
    }

    val catColor = CategoryIdentityMapper.mapColor(res.categoryColor)
    val catIcon = CategoryIdentityMapper.mapIcon(res.categoryIcon)

    AppCard {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = catColor.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = catIcon,
                    contentDescription = null,
                    tint = catColor,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = res.serviceName ?: "Servicio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = res.specialist.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday, 
                        null, 
                        modifier = Modifier.size(14.dp), 
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            StatusChip(res.status)
        }
    }
}
