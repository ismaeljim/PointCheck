package com.pointcheck.features.reservation.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.pointcheck.core.presentation.components.*
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
    val scrollState = rememberScrollState()
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showMapSheet by remember { mutableStateOf(false) }
    var professionalExpanded by remember { mutableStateOf(false) }
    var serviceExpanded by remember { mutableStateOf(false) }
    var paymentMethodExpanded by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= System.currentTimeMillis() - 86400000
            }
        }
    )

    val paymentMethods = listOf(
        "CASH" to "Efectivo",
        "TRANSFER" to "Transferencia",
        "CARD_EXTERNAL" to "Tarjeta (POS Externo)"
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
                title = "Nueva Reserva",
                onBack = { nav.popBackStack() }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text("¿Qué servicio buscas?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            AppCard {
                Column(Modifier.padding(16.dp)) {
                    Box {
                        AppSelectorField(
                            label = "Especialista",
                            value = s.selectedProfessional?.name ?: "Seleccionar profesional",
                            icon = Icons.Default.Person,
                            onClick = { professionalExpanded = true },
                            enabled = !s.isLoading
                        )
                        DropdownMenu(
                            expanded = professionalExpanded,
                            onDismissRequest = { professionalExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            s.professionals.forEach { prof ->
                                DropdownMenuItem(
                                    text = { Text(prof.name) },
                                    onClick = {
                                        vm.selectProfessional(prof)
                                        professionalExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Box {
                        AppSelectorField(
                            label = "Servicio",
                            value = s.selectedService?.name ?: "Seleccionar servicio",
                            icon = Icons.Default.Work,
                            enabled = s.selectedProfessional != null && !s.isLoading,
                            onClick = { serviceExpanded = true }
                        )
                        DropdownMenu(
                            expanded = serviceExpanded,
                            onDismissRequest = { serviceExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            s.services.forEach { serv ->
                                DropdownMenuItem(
                                    text = { 
                                        Column {
                                            Text(serv.name, fontWeight = FontWeight.SemiBold)
                                            Text("$${serv.price}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                        }
                                    },
                                    onClick = {
                                        vm.selectService(serv)
                                        serviceExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (s.selectedService?.isAtHome == true) {
                Spacer(Modifier.height(24.dp))
                Text("Dirección del servicio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                AppTextField(
                    value = s.notes,
                    onValueChange = { vm.setNotes(it) },
                    label = "Ingresa tu dirección exacta",
                    leadingIcon = Icons.Default.LocationOn
                )
            } else if (s.selectedProfessional != null) {
                Spacer(Modifier.height(16.dp))
                AppOutlinedButton(
                    text = "Ver ubicación del local",
                    icon = Icons.Default.Map,
                    onClick = { showMapSheet = true }
                )
            }

            Spacer(Modifier.height(24.dp))
            Text("Fecha y Hora", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            AppCard {
                Column(Modifier.padding(16.dp)) {
                    AppOutlinedButton(
                        text = if (s.reservationStartMillis == null) "Seleccionar Fecha" else SimpleDateFormat("EEEE d 'de' MMMM", Locale.getDefault()).format(Date(s.reservationStartMillis!!)),
                        icon = Icons.Default.CalendarToday,
                        onClick = { showDatePicker = true },
                        enabled = s.selectedService != null && !s.isLoading
                    )
                    
                    if (s.reservationStartMillis != null && s.selectedService?.priceUnit != "DAY") {
                        Spacer(Modifier.height(16.dp))
                        Text("Horarios disponibles", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.height(8.dp))
                        
                        if (s.isAvailabilityLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally))
                        } else if (s.availableSlots.isEmpty()) {
                            Text("No hay turnos disponibles para este día.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
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
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("Pago y Notas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            AppCard {
                Column(Modifier.padding(16.dp)) {
                    Box {
                        AppSelectorField(
                            label = "Método de Pago",
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
                    
                    Spacer(Modifier.height(16.dp))
                    AppTextField(
                        value = s.notes,
                        onValueChange = { vm.setNotes(it) },
                        label = "Instrucciones adicionales (Opcional)",
                        minLines = 2
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            AppButton(
                text = "Confirmar Reserva",
                onClick = { vm.createReservation {} },
                enabled = s.isValid && !s.isLoading,
                isLoading = s.isLoading
            )
            Spacer(Modifier.height(40.dp))
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

        if (showMapSheet && s.selectedProfessional != null) {
            ModalBottomSheet(
                onDismissRequest = { showMapSheet = false },
                containerColor = Color.White
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp).height(400.dp)) {
                    Text("Ubicación del Profesional", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(s.selectedProfessional?.city ?: "Ubicación no disponible", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.height(16.dp))
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        PointCheckMapView(
                            latitude = s.selectedProfessional?.latitude ?: -33.4489, 
                            longitude = s.selectedProfessional?.longitude ?: -70.6693,
                            title = s.selectedProfessional?.name ?: "Especialista"
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    AppButton(text = "Cerrar", onClick = { showMapSheet = false })
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
