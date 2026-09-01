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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.AccessTime
import java.text.SimpleDateFormat
import com.pointcheck.core.utils.FormatUtils
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Payments
import com.pointcheck.core.ui.components.PointCheckTopBar
import com.pointcheck.core.ui.components.PointCheckSelectorField
import com.pointcheck.core.ui.components.PointCheckButton
import com.pointcheck.core.ui.components.PointCheckTextField
import com.pointcheck.core.ui.components.PointCheckOutlinedButton
import com.pointcheck.core.ui.components.PointCheckMapView

import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.shape.RoundedCornerShape

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
 * @param preSelectedSpecialistProfileId Optional ID to pre-fill the specialist selection.
 * @param preSelectedCategoryId Optional ID to filter specialists by category.
 * @param vm [ReservationViewModel] that encapsulates the complex booking logic.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookingScreen(
    nav: NavController, 
    snackbar: SnackbarHostState, 
    preSelectedSpecialistProfileId: String? = null,
    preSelectedCategoryId: String? = null,
    vm: ReservationViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    
    // LaunchedEffect para cargar datos iniciales
    LaunchedEffect(preSelectedSpecialistProfileId, preSelectedCategoryId) {
        if (preSelectedSpecialistProfileId != null) {
            vm.selectProfessionalById(preSelectedSpecialistProfileId, preSelectedCategoryId)
        } else {
            vm.loadProfessionals(preSelectedCategoryId)
        }
    }

    Scaffold(
        topBar = {
            PointCheckTopBar(
                title = "Agendar Cita",
                onBack = { nav.popBackStack() }
            )
        }
    ) { pad ->
        Box(modifier = Modifier.padding(pad).fillMaxSize()) {
            when (val s = state) {
                is BookingUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is BookingUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Warning, 
                            contentDescription = null, 
                            modifier = Modifier.size(48.dp), 
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(s.message, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Spacer(Modifier.height(24.dp))
                        PointCheckButton(text = "Reintentar", onClick = { vm.loadProfessionals(preSelectedCategoryId) })
                    }
                }
                is BookingUiState.Success -> {
                    BookingContent(
                        s = s,
                        vm = vm,
                        nav = nav,
                        snackbar = snackbar
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun BookingContent(
    s: BookingUiState.Success,
    vm: ReservationViewModel,
    nav: NavController,
    snackbar: SnackbarHostState
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val isDayUnit = s.selectedService?.priceUnit == "DAY"
    val isAtHome = s.selectedService?.isAtHome ?: false
    
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

    val scrollState = rememberScrollState()
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

    LaunchedEffect(s.successMessage) {
        s.successMessage?.let {
            snackbar.showSnackbar(it)
            vm.clearSuccess()
            nav.popBackStack()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        if (s.isAtHomeAddressMissing) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Falta dirección de domicilio",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "No tienes una dirección registrada en tu perfil. Puedes ingresarla abajo para esta reserva.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // PASO 1: Selección
        Text("Paso 1: Selección", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = s.searchQuery,
                    onValueChange = { vm.onSearchQueryChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar profesional o especialidad...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (s.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { vm.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(16.dp))

                PointCheckSelectorField(
                    label = "Profesional",
                    value = s.selectedProfessional?.name ?: "Seleccionar especialista",
                    icon = Icons.Default.Person,
                    onClick = { professionalExpanded = true }
                )

                DropdownMenu(
                    expanded = professionalExpanded,
                    onDismissRequest = { professionalExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    if (s.filteredProfessionals.isEmpty()) {
                        DropdownMenuItem(text = { Text("No se encontraron resultados") }, onClick = { })
                    }
                    s.filteredProfessionals.forEach { prof ->
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

                PointCheckSelectorField(
                    label = "Servicio",
                    value = s.selectedService?.name ?: "Seleccionar servicio",
                    icon = Icons.Default.Work,
                    enabled = s.selectedProfessional != null,
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(serv.name)
                                        Text(FormatUtils.formatCurrency(serv.price), style = MaterialTheme.typography.bodySmall)
                                    }
                                    if (serv.isAtHome) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.tertiaryContainer,
                                            shape = MaterialTheme.shapes.extraSmall
                                        ) {
                                            Text(
                                                "Domicilio",
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall
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

        if (isAtHome) {
            Spacer(Modifier.height(16.dp))
            PointCheckTextField(
                value = s.notes, 
                onValueChange = { vm.setNotes(it) },
                label = "Dirección para el servicio",
                placeholder = "Ingrese la dirección completa",
                leadingIcon = Icons.Default.Home,
                isError = s.isAtHomeAddressMissing && s.notes.isBlank()
            )
        } else {
            s.selectedProfessional?.let { prof ->
                Spacer(Modifier.height(16.dp))
                PointCheckOutlinedButton(
                    text = "Ver Ubicación del Profesional",
                    icon = Icons.Default.LocationOn,
                    onClick = { showMapSheet = true }
                )
                
                if (showMapSheet) {
                    ModalBottomSheet(onDismissRequest = { showMapSheet = false }) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp).height(450.dp)) {
                            Text("Ubicación del profesional", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(16.dp))
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                PointCheckMapView(
                                    latitude = prof.latitude ?: -33.4489, 
                                    longitude = prof.longitude ?: -70.6693,
                                    title = prof.name
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            PointCheckButton(text = "Entendido", onClick = { showMapSheet = false })
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Paso 2: Fecha y Hora", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        PointCheckOutlinedButton(
            text = if (s.reservationStartMillis == null) "Elegir fecha" else "Cambiar fecha",
            icon = Icons.Default.CalendarMonth,
            onClick = { showDatePicker = true },
            enabled = s.selectedService != null
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
            
            ContextualFlowRow(
                modifier = Modifier.fillMaxWidth(),
                itemCount = s.availableSlots.size,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) { index ->
                // Blindaje total contra IndexOutOfBounds durante recomposiciones
                val slots = s.availableSlots
                if (index < slots.size) {
                    val slot = slots[index]
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
                    Icon(Icons.Default.CalendarMonth, null)
                    Spacer(Modifier.width(12.dp))
                    Text(formattedDate, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
            }

            s.weather?.let { w ->
                Card(
                    modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Pronóstico", style = MaterialTheme.typography.labelMedium)
                            Text("${w.main.temp.toInt()}°C - ${w.weather.firstOrNull()?.description ?: ""}", 
                                style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                        w.weather.firstOrNull()?.icon?.let { icon ->
                            AsyncImage(model = "https://openweathermap.org/img/wn/$icon@2x.png", contentDescription = null, modifier = Modifier.size(48.dp))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Paso 3: Pago y Notas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        Box {
            PointCheckSelectorField(
                label = "Método de Pago",
                value = paymentMethods.find { it.first == s.paymentMethod }?.second ?: "Seleccionar...",
                icon = Icons.Default.Payments,
                onClick = { paymentMethodExpanded = true }
            )
            DropdownMenu(expanded = paymentMethodExpanded, onDismissRequest = { paymentMethodExpanded = false }) {
                paymentMethods.forEach { (key, label) ->
                    DropdownMenuItem(text = { Text(label) }, onClick = { vm.setPaymentMethod(key); paymentMethodExpanded = false })
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        PointCheckTextField(
            value = s.notes, 
            onValueChange = { vm.setNotes(it) }, 
            label = "Notas (Opcional)", 
            placeholder = "Añada detalles adicionales...", 
            leadingIcon = Icons.Default.Info, 
            modifier = Modifier.heightIn(min = 100.dp)
        )
        Spacer(Modifier.height(32.dp))

        PointCheckButton(
            text = "Confirmar Reserva",
            onClick = { vm.createReservation { } },
            enabled = s.isValid
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
            }
        ) { DatePicker(state = datePickerState) }
    }
}


