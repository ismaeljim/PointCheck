package com.pointcheck.features.reservation.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.AccessTime
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    nav: NavController, 
    snackbar: SnackbarHostState, 
    preSelectedSpecialistId: Long? = null,
    vm: ReservationViewModel = viewModel()
) {
    val s by vm.state.collectAsState()
    val scope = rememberCoroutineScope()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    val scrollState = rememberScrollState()

    var professionalExpanded by remember { mutableStateOf(false) }
    var serviceExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(preSelectedSpecialistId) {
        preSelectedSpecialistId?.let { id ->
            vm.selectProfessionalById(id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agendar Cita") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text("Paso 1: Selección", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    SelectorField(
                        label = "Profesional",
                        value = s.selectedProfessional?.name ?: "Seleccionar especialista",
                        icon = Icons.Default.Person,
                        onClick = { professionalExpanded = true }
                    )

                    DropdownMenu(
                        expanded = professionalExpanded,
                        onDismissRequest = { professionalExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        s.professionals.forEach { prof ->
                            DropdownMenuItem(
                                text = { Text("${prof.name} - ${prof.specialty ?: "General"}") },
                                onClick = {
                                    vm.selectProfessional(prof)
                                    professionalExpanded = false
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    SelectorField(
                        label = "Servicio",
                        value = s.selectedService?.name ?: "Seleccionar servicio",
                        icon = Icons.Default.Work,
                        enabled = s.selectedProfessional != null,
                        onClick = { serviceExpanded = true }
                    )

                    DropdownMenu(
                        expanded = serviceExpanded,
                        onDismissRequest = { serviceExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
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
            }

            Spacer(Modifier.height(24.dp))
            Text("Paso 2: Fecha y Hora", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = s.selectedService != null,
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(Icons.Default.CalendarMonth, null)
                Spacer(Modifier.width(8.dp))
                Text(if (s.reservationStartMillis == null) "Elegir fecha" else "Cambiar fecha")
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = s.reservationStartMillis != null,
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(Icons.Filled.AccessTime, null)
                Spacer(Modifier.width(8.dp))
                Text("Elegir hora")
            }

            s.reservationStartMillis?.let {
                val formattedDate = SimpleDateFormat("EEEE d 'de' MMMM, HH:mm", Locale.getDefault()).format(Date(it))
                Card(
                    modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(Modifier.width(12.dp))
                        Text(formattedDate, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    }
                }

                // Weather component integrated as per Prompt 5
                s.weather?.let { w ->
                    Card(
                        modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Pronóstico para el día", style = MaterialTheme.typography.labelMedium)
                                Text("${w.main.temp.toInt()}°C - ${w.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: ""}", 
                                    style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            }
                            w.weather.firstOrNull()?.icon?.let { icon ->
                                AsyncImage(
                                    model = "https://openweathermap.org/img/wn/$icon@2x.png",
                                    contentDescription = "Weather Icon",
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Paso 3: Notas Adicionales", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = s.notes,
                onValueChange = { vm.setNotes(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Instrucciones especiales, síntomas, etc.") },
                minLines = 3,
                maxLines = 5,
                shape = MaterialTheme.shapes.medium
            )

            if (s.error != null) {
                Text(s.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp))
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    vm.createReservation {
                        scope.launch {
                            snackbar.showSnackbar("¡Reserva confirmada con éxito!")
                            nav.popBackStack()
                        }
                    }
                },
                enabled = s.isValid && !s.isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                if (s.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("Confirmar Reserva", fontSize = 16.sp)
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { vm.setReservationDateTime(it) }
                        showDatePicker = false
                    }) { Text("Confirmar") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
                }
            ) { DatePicker(state = datePickerState) }
        }
        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        vm.updateReservationTime(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }) { Text("Confirmar") }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        TimePicker(state = timePickerState)
                    }
                }
            )
        }
    }
}

@Composable
fun SelectorField(label: String, value: String, icon: ImageVector, enabled: Boolean = true, onClick: () -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(icon, null) },
            trailingIcon = { IconButton(onClick = onClick, enabled = enabled) { Icon(Icons.Default.ArrowDropDown, null) } },
            shape = MaterialTheme.shapes.medium
        )
    }
}
