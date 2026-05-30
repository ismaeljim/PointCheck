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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.core.presentation.components.HeaderIcon
import com.pointcheck.core.presentation.components.SectionHeader
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
                    title,
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
                if (s.isLoading && s.appointments.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                    }
                } else if (s.appointments.isEmpty()) {
                    EmptyHistoryState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item { SectionHeader("Listado de actividad") }
                        
                        items(s.appointments) { appointment ->
                            AppointmentHistoryItem(appointment)
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentHistoryItem(res: ReservationResponseDto) {
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono Circular
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = catColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = catIcon,
                        contentDescription = null,
                        tint = catColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = res.serviceName ?: "Servicio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = res.specialistName ?: "Profesional",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                
                Spacer(Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarToday, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Status Badge Vertical
            Surface(
                color = when (res.status.uppercase()) {
                    "COMPLETED" -> Color(0xFFE8F5E9)
                    "CANCELLED" -> Color(0xFFFFEBEE)
                    else -> Color(0xFFFFF3E0)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = when(res.status.uppercase()) {
                        "COMPLETED" -> "Listo"
                        "CANCELLED" -> "Cancelado"
                        else -> "Pendiente"
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = when (res.status.uppercase()) {
                        "COMPLETED" -> Color(0xFF2E7D32)
                        "CANCELLED" -> Color(0xFFC62828)
                        else -> Color(0xFFEF6C00)
                    }
                )
            }
        }
    }
}

@Composable
fun EmptyHistoryState() {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.History,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Sin actividad reciente",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Tus citas pasadas y canceladas aparecerán en esta sección.",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}
