package com.pointcheck.features.profile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import com.pointcheck.core.location.LocationViewModel
import com.pointcheck.core.ui.components.PointCheckButton
import com.pointcheck.core.ui.components.PointCheckCard
import com.pointcheck.core.ui.components.PointCheckTextField
import com.pointcheck.core.ui.components.PointCheckTopBar
import com.pointcheck.core.ui.components.PointCheckSelectorField
import com.pointcheck.core.ui.components.PointCheckDayScheduleRow
import com.pointcheck.core.util.RutVisualTransformation
import com.pointcheck.core.util.RutUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalProfileScreen(
    nav: NavController,
    vm: ProfessionalProfileViewModel = viewModel(),
    locationVm: LocationViewModel = viewModel()
) {
    val context = LocalContext.current
    val s by vm.state.collectAsStateWithLifecycle()
    val locState by locationVm.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var displayName by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    // Dirección Fraccionada para UX Autóctona
    var street by remember { mutableStateOf("") }
    var houseNumber by remember { mutableStateOf("") }
    var commune by remember { mutableStateOf("") }

    var city by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("30") }
    var rut by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var expandedCategory by remember { mutableStateOf(false) }
    var syncAddressWithUser by remember { mutableStateOf(true) }
    
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    // Auditoría de UI: Mostrar mensajes emergentes para éxito o error
    LaunchedEffect(s.error) {
        s.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }

    LaunchedEffect(s.successMessage) {
        s.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearSuccess()
        }
    }

    // Manejo de navegación externa (ej: ir a servicios tras crear perfil)
    val navEvent by vm.navigationEvent.collectAsStateWithLifecycle()
    LaunchedEffect(navEvent) {
        navEvent?.let { route ->
            nav.navigate(route)
            vm.clearNavigationEvent()
        }
    }

    // Sincronizar campos cuando el perfil carga
    LaunchedEffect(s.profile, s.rut, s.phone) {
        s.profile?.let {
            displayName = it.displayName
            businessName = it.businessName
            specialty = it.specialty
            description = it.description
            
            // Intentar desglosar la dirección guardada (Calle Numero, Comuna)
            val fullAddr = it.address
            if (fullAddr.contains(",")) {
                val parts = fullAddr.split(",")
                commune = parts.last().trim()
                val streetAndNum = parts.first().trim()
                val lastSpace = streetAndNum.lastIndexOf(" ")
                if (lastSpace != -1) {
                    street = streetAndNum.substring(0, lastSpace).trim()
                    houseNumber = streetAndNum.substring(lastSpace).trim()
                } else {
                    street = streetAndNum
                }
            } else {
                street = fullAddr
            }

            city = it.city
            duration = it.defaultSessionDurationMinutes.toString()
            selectedCategoryId = it.categoryId
            latitude = it.latitude
            longitude = it.longitude
        }
        if (rut.isEmpty()) rut = s.rut
        if (phone.isEmpty()) phone = s.phone
    }

    Scaffold(
        topBar = {
            PointCheckTopBar(
                title = "Perfil Profesional",
                onBack = { nav.popBackStack() }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { pad ->
        if (s.isLoading) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(pad)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (s.profile == null && !s.isLoading && !s.isEditing) {
                    EmptyProfileState(onStart = { vm.toggleEdit() })
                } else {
                    Text(
                        text = if (s.isEditing) "Editando Perfil" else "Tu Perfil Profesional",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Esta información será visible para tus clientes al momento de agendar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(24.dp))

                    PointCheckCard(
                        title = "Información de Identidad",
                        subtitle = "Datos básicos y contacto",
                        icon = Icons.Default.Badge
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            PointCheckTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                label = "Nombre Público",
                                placeholder = "Ej: Peluquería Central",
                                leadingIcon = Icons.Default.Badge,
                                enabled = s.isEditing
                            )
                            
                            PointCheckTextField(
                                value = businessName,
                                onValueChange = { businessName = it },
                                label = "Nombre de Empresa (Opcional)",
                                placeholder = "Razón Social",
                                leadingIcon = Icons.Default.Business,
                                enabled = s.isEditing
                            )

                            PointCheckTextField(
                                value = rut,
                                onValueChange = { if (it.length <= 9) rut = it },
                                label = "RUT (Sin puntos ni guión)",
                                placeholder = "12345678K",
                                leadingIcon = Icons.Default.AccountBox,
                                enabled = s.isEditing,
                                visualTransformation = RutVisualTransformation(),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                                )
                            )

                            PointCheckTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = "Teléfono de Contacto",
                                placeholder = "+569...",
                                leadingIcon = Icons.Default.Phone,
                                enabled = s.isEditing,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    PointCheckCard(
                        title = "Especialidad y Servicios",
                        subtitle = "Define tu categoría y tiempos",
                        icon = Icons.Default.Category
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Selector de Categoría Refactorizado
                            Box {
                                val selectedCategory = s.categories.find { it.id == selectedCategoryId }
                                PointCheckSelectorField(
                                    label = "Categoría de Servicio",
                                    value = selectedCategory?.name ?: "Seleccionar Categoría",
                                    icon = Icons.Default.Category,
                                    onClick = { if (s.isEditing) expandedCategory = true },
                                    enabled = s.isEditing
                                )

                                DropdownMenu(
                                    expanded = expandedCategory && s.isEditing,
                                    onDismissRequest = { expandedCategory = false },
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    s.categories.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text(category.name) },
                                            onClick = {
                                                selectedCategoryId = category.id
                                                expandedCategory = false
                                            }
                                        )
                                    }
                                }
                            }

                            PointCheckTextField(
                                value = specialty,
                                onValueChange = { specialty = it },
                                label = "Título Profesional (Ej: Peluquero, Kinesiólogo)",
                                placeholder = "Tu especialidad principal",
                                leadingIcon = Icons.Default.Work,
                                enabled = s.isEditing
                            )

                            PointCheckTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = "Descripción / Bio",
                                placeholder = "Cuéntales a tus clientes sobre tu trabajo...",
                                leadingIcon = Icons.Default.Description,
                                enabled = s.isEditing
                                // minLines/maxLines no están en PointCheckTextField, se asume singleLine por defecto
                            )
                            
                            PointCheckTextField(
                                value = duration,
                                onValueChange = { duration = it },
                                label = "Duración promedio cita (min)",
                                placeholder = "30",
                                leadingIcon = Icons.Default.Timer,
                                enabled = s.isEditing,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    PointCheckCard(
                        title = "Mi Horario de Atención",
                        subtitle = "Define tus días y horas disponibles",
                        icon = Icons.Default.Schedule
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            val daysTranslation = mapOf(
                                "MONDAY" to "Lunes",
                                "TUESDAY" to "Martes",
                                "WEDNESDAY" to "Miércoles",
                                "THURSDAY" to "Jueves",
                                "FRIDAY" to "Viernes",
                                "SATURDAY" to "Sábado",
                                "SUNDAY" to "Domingo"
                            )

                            listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY").forEach { dayKey ->
                                val config = s.workingHours[dayKey] ?: DayConfig()
                                PointCheckDayScheduleRow(
                                    dayName = daysTranslation[dayKey] ?: dayKey,
                                    config = config,
                                    onConfigChange = { vm.updateDayConfig(dayKey, it) },
                                    enabled = s.isEditing
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    PointCheckCard(
                        title = "Ubicación de Atención",
                        subtitle = "Dónde atiendes a tus clientes",
                        icon = Icons.Default.Place
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (s.isEditing) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    PointCheckTextField(
                                        value = street,
                                        onValueChange = { 
                                            street = it
                                            locationVm.getAddressSuggestions("$street $houseNumber, $commune")
                                        },
                                        label = "Calle",
                                        placeholder = "Ej: Av. Providencia",
                                        leadingIcon = Icons.Default.AddRoad,
                                        modifier = Modifier.weight(0.7f)
                                    )
                                    PointCheckTextField(
                                        value = houseNumber,
                                        onValueChange = { houseNumber = it },
                                        label = "N°",
                                        placeholder = "123",
                                        leadingIcon = Icons.Default.Numbers,
                                        modifier = Modifier.weight(0.3f)
                                    )
                                }
                                PointCheckTextField(
                                    value = commune,
                                    onValueChange = { commune = it },
                                    label = "Comuna",
                                    placeholder = "Santiago",
                                    leadingIcon = Icons.Default.LocationCity
                                )
                            } else {
                                ProfileInfoItem(Icons.Default.Place, "Dirección", "$street $houseNumber, $commune")
                            }

                            if (locState.addressSuggestions.isNotEmpty() && s.isEditing) {
                                DropdownMenu(
                                    expanded = true,
                                    onDismissRequest = { },
                                    properties = PopupProperties(focusable = false),
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    locState.addressSuggestions.forEach { suggestion ->
                                        val fullAddress = suggestion.getAddressLine(0)
                                        DropdownMenuItem(
                                            text = { Text(fullAddress, style = MaterialTheme.typography.bodySmall) },
                                            onClick = {
                                                street = suggestion.thoroughfare ?: street
                                                houseNumber = suggestion.subThoroughfare ?: houseNumber
                                                commune = suggestion.locality ?: suggestion.subLocality ?: commune
                                                city = suggestion.adminArea ?: city
                                                latitude = suggestion.latitude
                                                longitude = suggestion.longitude
                                                locationVm.clearSuggestions()
                                            }
                                        )
                                    }
                                }
                            }

                            PointCheckTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = "Ciudad",
                                placeholder = "Región Metropolitana",
                                leadingIcon = Icons.Default.Map,
                                enabled = s.isEditing
                            )

                            if (s.isEditing) {
                                val gpsButtonColor = when (locState.gpsStatus) {
                                    com.pointcheck.core.location.GpsStatus.SUCCESS -> Color(0xFF2E7D32) // Verde esmeralda
                                    com.pointcheck.core.location.GpsStatus.ERROR -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.primary
                                }

                                Button(
                                    onClick = {
                                        locationVm.getCurrentLocation { s, n, c, lat, lng ->
                                            street = s
                                            houseNumber = n
                                            commune = c
                                            latitude = lat
                                            longitude = lng
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = gpsButtonColor),
                                    enabled = !locState.isLocating,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (locState.isLocating) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                        Spacer(Modifier.width(12.dp))
                                        Text("Localizando...")
                                    } else {
                                        Icon(
                                            if (locState.gpsStatus == com.pointcheck.core.location.GpsStatus.SUCCESS) Icons.Default.CheckCircle else Icons.Default.MyLocation,
                                            contentDescription = null
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            if (locState.gpsStatus == com.pointcheck.core.location.GpsStatus.SUCCESS) "Ubicación Obtenida" else "Usar mi ubicación GPS"
                                        )
                                    }
                                }
                                
                                if (locState.gpsStatus == com.pointcheck.core.location.GpsStatus.SUCCESS) {
                                    Text(
                                        "✓ Coordenadas vinculadas con éxito",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF2E7D32),
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                }
                                
                                if (latitude != null && longitude != null) {
                                    Text(
                                        "Coordenadas: ${"%.5f".format(latitude)}, ${"%.5f".format(longitude)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }

                                if (s.isEditing) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        Checkbox(
                                            checked = syncAddressWithUser,
                                            onCheckedChange = { syncAddressWithUser = it }
                                        )
                                        Text(
                                            "Actualizar mi dirección personal también",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    if (s.error != null) {
                        Text(
                            text = s.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    if (s.isEditing) {
                        PointCheckButton(
                            text = "Guardar Cambios",
                            onClick = {
                                val finalAddress = if (street.isNotBlank()) "$street $houseNumber, $commune".trim() else ""
                                vm.saveProfile(
                                    selectedCategoryId,
                                    displayName,
                                    businessName,
                                    specialty,
                                    description,
                                    finalAddress,
                                    city,
                                    duration.toIntOrNull() ?: 30,
                                    rut,
                                    phone,
                                    latitude,
                                    longitude,
                                    updateBaseAddress = syncAddressWithUser
                                )
                            },
                            isLoading = s.isLoading,
                            enabled = displayName.isNotBlank() && specialty.isNotBlank() && selectedCategoryId != null && rut.isNotBlank() && phone.isNotBlank() && street.isNotBlank()
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { vm.toggleEdit() },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancelar")
                        }
                    } else {
                        PointCheckButton(
                            text = "Editar Perfil Profesional",
                            onClick = { vm.toggleEdit() }, 
                            enabled = !s.isLoading
                        )
                    }
                    
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun EmptyProfileState(onStart: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Work,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Crea tu Perfil Profesional",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Para poder ofrecer servicios y recibir citas, debes configurar primero tu perfil público.",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        PointCheckButton(text = "Configurar ahora", onClick = onStart)
    }
}
