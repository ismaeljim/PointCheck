package com.pointcheck.features.reservation.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.foundation.layout.ContextualFlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

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
import androidx.compose.material.icons.filled.Payments
import com.pointcheck.core.presentation.components.AppTopBar
import com.pointcheck.core.presentation.components.AppSelectorField
import com.pointcheck.core.presentation.components.AppButton
import com.pointcheck.core.presentation.components.AppTextField
import com.pointcheck.core.presentation.components.AppOutlinedButton

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
        initialSelectedDateMillis = System.currentTimeMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // Bloquea días anteriores a hoy (comparando solo la fecha, sin hora)
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                return utcTimeMillis >= calendar.timeInMillis
            }
        }
    )
    val timePickerState = rememberTimePickerState()

    val scrollState = rememberScrollState()

    var professionalExpanded by remember { mutableStateOf(false) }
    var serviceExpanded by remember { mutableStateOf(false) }
    var paymentMethodExpanded by remember { mutableStateOf(false) }

    val paymentMethods = listOf(
        "CASH" to "Efectivo",
        "TRANSFER" to "Transferencia",
        "CARD_EXTERNAL" to "Tarjeta (POS Externo)",
        "OTHER" to "Otro"
    )

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

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Agendar Cita",
                onBack = { nav.popBackStack() }
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
                    AppSelectorField(
                        label = "Profesional",
                        value = s.selectedProfessional?.name ?: "Seleccionar especialista",
                        icon = Icons.Default.Person,
                        onClick = { professionalExpanded = true },
                        enabled = !s.isLoading
                    )

                    DropdownMenu(
                        expanded = professionalExpanded && !s.isLoading,
                        onDismissRequest = { professionalExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
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

                    AppSelectorField(
                        label = "Servicio",
                        value = s.selectedService?.name ?: "Seleccionar servicio",
                        icon = Icons.Default.Work,
                        enabled = s.selectedProfessional != null && !s.isLoading,
                        onClick = { serviceExpanded = true }
                    )

                    DropdownMenu(
                        expanded = serviceExpanded && !s.isLoading,
                        onDismissRequest = { serviceExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
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

            AppOutlinedButton(
                text = if (s.reservationStartMillis == null) "Elegir fecha" else "Cambiar fecha",
                icon = Icons.Default.CalendarMonth,
                onClick = { showDatePicker = true },
                enabled = s.selectedService != null && !s.isLoading
            )

            if (s.reservationStartMillis != null && !isDayUnit) {
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Horarios Disponibles", style = MaterialTheme.typography.labelLarge)
                    if (s.isAvailabilityLoading) {
                        Spacer(Modifier.width(8.dp))
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                
                if (s.availableSlots.isEmpty() && !s.isAvailabilityLoading) {
                    Text("El profesional no tiene turnos configurados para este día.",
                        style = MaterialTheme.typography.bodySmall, 
                        color = MaterialTheme.colorScheme.error)
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
                            label = { Text(slot) }
                        )
                    }
                }
            }

            s.reservationStartMillis?.let {
                val pattern = if (isDayUnit) "EEEE d 'de' MMMM" else "EEEE d 'de' MMMM, HH:mm"
                val formattedDate = SimpleDateFormat(pattern, Locale.getDefault()).format(Date(it))
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
            Text("Paso 3: Método de Pago", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            Box {
                AppSelectorField(
                    label = "Seleccione Método de Pago",
                    value = paymentMethods.find { it.first == s.paymentMethod }?.second ?: "Seleccionar...",
                    icon = Icons.Default.Payments,
                    onClick = { paymentMethodExpanded = true },
                    enabled = !s.isLoading
                )

                DropdownMenu(
                    expanded = paymentMethodExpanded,
                    onDismissRequest = { paymentMethodExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    paymentMethods.forEach { (key, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                vm.setPaymentMethod(key)
                                paymentMethodExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Paso 4: Notas Adicionales", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            AppTextField(
                value = s.notes,
                onValueChange = { vm.setNotes(it) },
                label = "Notas (Opcional)",
                minLines = 3,
                enabled = !s.isLoading
            )

            Spacer(Modifier.height(32.dp))

            AppButton(
                text = "Confirmar Reserva",
                onClick = {
                    vm.createReservation {
                        // success handled by LaunchedEffect
                    }
                },
                enabled = s.isValid && !s.isLoading,
                isLoading = s.isLoading
            )
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

