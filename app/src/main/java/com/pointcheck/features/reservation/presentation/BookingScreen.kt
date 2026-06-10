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
import com.pointcheck.core.presentation.components.PointCheckMapView

import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn

/**
 * PointCheck Booking UX - REFACTORIZACIÓN DE NIVEL SENIOR
 * 
 * ¿QUÉ CAMBIÓ Y POR QUÉ?
 * 1. MAPA EN MODAL: Antes el mapa se cargaba directamente en el scroll. Esto causaba "jank" (tirones) 
 *    al hacer scroll y carga cognitiva innecesaria. Ahora se usa ModalBottomSheet, mejorando la 
 *    fluidez de la pantalla de reserva.
 * 
 * 2. LÓGICA DUAL DE UBICACIÓN: 
 *    - Si 'isAtHome' es true: El sistema pide una dirección (Input).
 *    - Si 'isAtHome' es false: El sistema muestra el mapa del local del profesional (Visualización).
 *    Esto adapta la UX dinámicamente según el tipo de servicio seleccionado.
 * 
 * 3. OPTIMIZACIÓN DE DATOS: Se eliminaron las consultas redundantes. El ViewModel ahora recibe 
 *    objetos ya hidratados desde el backend (Zero N+1), asegurando que los iconos de categoría 
 *    se pinten instantáneamente sin parpadeos.
 */
/**
 * Advanced Booking Screen for scheduling appointments with professionals.
 *
 * This screen implements a multi-step booking process:
 * 1. Specialist and Service selection.
 * 2. Location management (Dual logic: Input for home services or Map for on-site services).
 * 3. Date and Time selection with dynamic availability slots and weather forecasting.
 * 4. Payment method and additional notes.
 *
 * Key features:
 * - Map integration using [ModalBottomSheet] for improved performance.
 * - Dynamic slot loading based on professional availability.
 * - Integration with OpenWeatherMap API for context-aware scheduling.
 * - Sophisticated date picker with restrictions for past dates.
 *
 * @param nav [NavController] for navigation after booking or on back press.
 * @param snackbar [SnackbarHostState] for displaying feedback and errors.
 * @param preSelectedSpecialistId Optional ID to pre-fill the specialist selection.
 * @param preSelectedCategoryId Optional ID to filter specialists by category.
 * @param vm [ReservationViewModel] that encapsulates the complex booking logic.
 */
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
    
    // Identificadores de lógica de negocio derivados del servicio seleccionado
    val isDayUnit = s.selectedService?.priceUnit == "DAY"
    val isAtHome = s.selectedService?.isAtHome ?: false
    
    // Configuración del DatePicker con restricción de fechas (UX: Solo fechas futuras)
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
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

    // Estados para control de componentes UI modales y dropdowns
    var showMapSheet by remember { mutableStateOf(false) }
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
            // PASO 1: Selección de Especialista y Servicio
            // Optimización: Los dropdowns usan datos precargados vía ViewModel para evitar parpadeos.
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
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Text(serv.name)
                                            Text("$${serv.price}", style = MaterialTheme.typography.bodySmall)
                                        }
                                        if (serv.isAtHome) {
                                            Spacer(Modifier.width(8.dp))
                                            Surface(
                                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                                shape = MaterialTheme.shapes.extraSmall
                                            ) {
                                                Text(
                                                    "Domicilio",
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                                )
                                            }
                                        }
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

            // PASO 2: Gestión de Ubicación (Mejora UX Prompt 3)
            // Lógica Dual: Si es a domicilio, pedimos dirección. Si no, mostramos ubicación fija en Modal.
            if (isAtHome) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Dirección para el servicio a domicilio",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(Modifier.height(8.dp))
                AppTextField(
                    value = s.notes, // Podríamos usar un campo específico para dirección si existiera en el estado
                    onValueChange = { vm.setNotes(it) },
                    label = "Ingrese dirección exacta",
                    leadingIcon = Icons.Default.Home,
                    enabled = !s.isLoading
                )
                Text(
                    "El profesional se desplazará a esta ubicación.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            } else {
                s.selectedProfessional?.let { prof ->
                    Spacer(Modifier.height(16.dp))
                    AppOutlinedButton(
                        text = "Ver Ubicación del Profesional",
                        icon = Icons.Default.LocationOn,
                        onClick = { showMapSheet = true }
                    )
                    
                    if (showMapSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showMapSheet = false },
                            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .height(450.dp)
                            ) {
                                Text(
                                    "Ubicación del profesional", 
                                    style = MaterialTheme.typography.titleLarge, 
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Punto de atención: ${prof.name}", 
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(Modifier.height(16.dp))
                                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                    PointCheckMapView(
                                        latitude = prof.latitude ?: -33.4489, 
                                        longitude = prof.longitude ?: -70.6693,
                                        title = prof.name
                                    )
                                }
                                Spacer(Modifier.height(16.dp))
                                AppButton(
                                    text = "Entendido", 
                                    onClick = { showMapSheet = false },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }

            // PASO 3: Fecha, Hora e Información Contextual
            // Aquí se integra la lógica de slots dinámicos y el clima
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

            // PASO 4: Finanzas
            // Preparación para el cierre de cita y cobro posterior.
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

