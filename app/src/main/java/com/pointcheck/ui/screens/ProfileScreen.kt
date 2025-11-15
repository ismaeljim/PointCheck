package com.pointcheck.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.pointcheck.data.prefs.UserPreferences
import com.pointcheck.model.Reservation
import com.pointcheck.navigation.Screen
import com.pointcheck.repository.RoomRepository
import com.pointcheck.viewmodel.ReservationViewModel
import com.pointcheck.viewmodel.UserViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(nav: NavController, vm: UserViewModel = viewModel(), reservationVm: ReservationViewModel = viewModel()) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as android.app.Application
    val prefs = remember { UserPreferences(ctx) }
    val repository = remember { RoomRepository(app) }

    var name by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf<String?>(null) }
    var avatar by remember { mutableStateOf<String?>(null) }
    var reservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }

    // Obtener datos del usuario
    LaunchedEffect(Unit) {
        prefs.name.collectLatest { name = it }
    }

    LaunchedEffect(Unit) {
        prefs.email.collectLatest { email = it }
    }

    LaunchedEffect(Unit) {
        prefs.avatar.collectLatest { avatar = it }
    }

    // Obtener reservas cuando hay email
    LaunchedEffect(email) {
        email?.let { userEmail ->
            repository.getUpcomingReservations(userEmail).collectLatest { res ->
                reservations = res
            }
        } ?: run {
            reservations = emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                actions = {
                    TextButton(onClick = { nav.navigate(Screen.Dashboard.route) }) {
                        Text("Dashboard")
                    }
                    TextButton(onClick = { nav.navigate(Screen.Booking.route) }) {
                        Text("Nueva Reserva")
                    }
                    TextButton(onClick = {
                        vm.logout {
                            nav.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                        }
                    }) {
                        Text("Cerrar sesión")
                    }
                }
            )
        }
    ) { pad ->
        LazyColumn(
            Modifier.padding(pad).padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (avatar != null) {
                        Image(
                            painter = rememberAsyncImagePainter(avatar),
                            contentDescription = null,
                            modifier = Modifier.size(96.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    Text(
                        text = name ?: "-",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = email ?: "-",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                HorizontalDivider()
            }

            item {
                Text(
                    text = "Mis Reservas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (reservations.isEmpty()) {
                item {
                    Text(
                        text = "No tienes reservas programadas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(reservations) { reservation ->
                    ReservationCard(
                        reservation = reservation,
                        onClick = {
                            nav.navigate(Screen.Scheduled.createRoute(reservation.id.toLong()))
                        },
                        onDelete = {
                            reservationVm.deleteReservation(reservation.id)
                        }
                    )
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { nav.navigate(Screen.Booking.route) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Hacer una Reserva")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationCard(reservation: Reservation, onClick: () -> Unit, onDelete: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(reservation.epochMillis))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).clickable(onClick = onClick)) {
                Text(
                    text = reservation.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Fecha: $dateString",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar reserva")
            }
        }
    }
}
