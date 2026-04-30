package com.pointcheck.features.reservation.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    nav: NavController, 
    snackbar: SnackbarHostState, 
    vm: ReservationViewModel = viewModel()
) {
    val s by vm.state.collectAsState()
    val scope = rememberCoroutineScope()
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    var professionalExpanded by remember { mutableStateOf(false) }
    var serviceExpanded by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Nueva Reserva") }) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp).fillMaxWidth()) {
            
            // Selector de Profesional
            Text("Seleccione Profesional:", style = MaterialTheme.typography.labelLarge)
            Box(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = s.selectedProfessional?.name ?: "Seleccionar...",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { professionalExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                    }
                )
                DropdownMenu(
                    expanded = professionalExpanded,
                    onDismissRequest = { professionalExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    s.professionals.forEach { prof ->
                        DropdownMenuItem(
                            text = { Text(prof.name + (prof.specialty?.let { " - $it" } ?: "")) },
                            onClick = {
                                vm.selectProfessional(prof)
                                professionalExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Selector de Servicio
            if (s.selectedProfessional != null) {
                Text("Seleccione Servicio:", style = MaterialTheme.typography.labelLarge)
                Box(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = s.selectedService?.name ?: "Seleccionar...",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { serviceExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = serviceExpanded,
                        onDismissRequest = { serviceExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        s.services.forEach { serv ->
                            DropdownMenuItem(
                                text = { Text("${serv.name} ($${serv.price})") },
                                onClick = {
                                    vm.selectService(serv)
                                    serviceExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Selector de Fecha
            Button(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = s.selectedService != null
            ) {
                Text("Seleccionar fecha y hora")
            }

            s.reservationStartMillis?.let {
                val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(it))
                Text(
                    text = "Cita: $formattedDate",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (s.error != null) {
                Text(s.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(Modifier.weight(1.0f))

            Button(
                onClick = {
                    vm.createReservation {
                        scope.launch {
                            snackbar.showSnackbar("Reserva confirmada")
                            nav.popBackStack()
                        }
                    }
                },
                enabled = s.isValid && !s.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (s.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text("Confirmar Reserva")
            }

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { vm.setReservationDateTime(it) }
                                showDatePicker = false
                            }
                        ) { Text("Aceptar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }
    }
}
