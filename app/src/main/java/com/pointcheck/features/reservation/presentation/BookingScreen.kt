package com.pointcheck.features.reservation.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.pointcheck.core.presentation.components.AppButton
import com.pointcheck.core.presentation.components.HeaderIcon
import com.pointcheck.core.presentation.components.SectionHeader
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookingScreen(
    nav: NavController, 
    snackbar: SnackbarHostState, 
    preSelectedSpecialistId: String? = null,
    preSelectedCategoryId: String? = null,
    vm: ReservationViewModel = viewModel()
) {
    val s by vm.state.collectAsState()
    val scope = rememberCoroutineScope()
    var showDatePicker by remember { mutableStateOf(false) }
    
    val isDayUnit = s.selectedService?.priceUnit == "DAY"
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    var professionalExpanded by remember { mutableStateOf(false) }
    var serviceExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(preSelectedSpecialistId, preSelectedCategoryId) {
        if (preSelectedSpecialistId != null) {
            vm.selectProfessionalById(preSelectedSpecialistId, preSelectedCategoryId)
        } else {
            vm.loadProfessionals(preSelectedCategoryId)
        }
    }

    LaunchedEffect(s.error) {
        s.error?.let {
            snackbar.showSnackbar(it)
            vm.clearError()
        }
    }

    LaunchedEffect(s.successMessage) {
        s.successMessage?.let {
            snackbar.showSnackbar(it)
            vm.clearSuccess()
            nav.popBackStack()
        }
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
                    "Agendar Cita",
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    SectionHeader("Detalles del servicio")

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            SelectorField(
                                label = "Profesional",
                                value = s.selectedProfessional?.name ?: "Seleccionar especialista",
                                icon = Icons.Outlined.Person,
                                onClick = { professionalExpanded = true },
                                enabled = !s.isLoading
                            )

                            DropdownMenu(
                                expanded = professionalExpanded && !s.isLoading,
                                onDismissRequest = { professionalExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.85f).background(Color.White)
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
                                icon = Icons.Outlined.WorkOutline,
                                enabled = s.selectedProfessional != null && !s.isLoading,
                                onClick = { serviceExpanded = true }
                            )

                            DropdownMenu(
                                expanded = serviceExpanded && !s.isLoading,
                                onDismissRequest = { serviceExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.85f).background(Color.White)
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
                    SectionHeader("Fecha y Hora")

                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = s.selectedService != null && !s.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (s.reservationStartMillis == null) "Seleccionar Fecha" else "Cambiar Fecha")
                    }

                    if (s.reservationStartMillis != null && !isDayUnit) {
                        Spacer(Modifier.height(24.dp))
                        Text("Horarios Disponibles", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        
                        if (s.availableSlots.isEmpty()) {
                            Surface(
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "No hay horarios disponibles para hoy.",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        } else {
                            ContextualFlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                itemCount = s.availableSlots.size,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) { index ->
                                val slot = s.availableSlots[index]
                                FilterChip(
                                    selected = s.selectedSlot == slot,
                                    onClick = { vm.updateReservationTimeFromSlot(slot) },
                                    label = { Text(slot) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    s.reservationStartMillis?.let {
                        val pattern = if (isDayUnit) "EEEE d 'de' MMMM" else "EEEE d 'de' MMMM, HH:mm"
                        val formattedDate = SimpleDateFormat(pattern, Locale.getDefault()).format(Date(it))
                        
                        Card(
                            modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f))
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.EventAvailable, null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(Modifier.width(12.dp))
                                Text(formattedDate, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                        }

                        s.weather?.let { w ->
                            Card(
                                modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                            ) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = "https://openweathermap.org/img/wn/${w.weather.firstOrNull()?.icon}@2x.png",
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Column {
                                        Text("Pronóstico del clima", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text("${w.main.temp.toInt()}°C - ${w.weather.firstOrNull()?.description}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    SectionHeader("Notas opcionales")

                    OutlinedTextField(
                        value = s.notes,
                        onValueChange = { vm.setNotes(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("¿Alguna instrucción especial?") },
                        minLines = 3,
                        shape = RoundedCornerShape(16.dp),
                        enabled = !s.isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Spacer(Modifier.height(40.dp))

                    AppButton(
                        text = "Confirmar Reserva",
                        onClick = { vm.createReservation {} },
                        enabled = s.isValid && !s.isLoading,
                        isLoading = s.isLoading
                    )
                    
                    Spacer(Modifier.height(100.dp))
                }
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
    }
}

@Composable
fun SelectorField(label: String, value: String, icon: ImageVector, enabled: Boolean = true, onClick: () -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(Modifier.height(6.dp))
        Surface(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
                Icon(Icons.Default.ArrowDropDown, null, tint = Color.Gray)
            }
        }
    }
}
