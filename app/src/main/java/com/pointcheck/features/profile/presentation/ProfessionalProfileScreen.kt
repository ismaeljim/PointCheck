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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import com.pointcheck.core.location.LocationViewModel
import com.pointcheck.core.presentation.components.AppButton
import com.pointcheck.core.presentation.components.AppTextField
import com.pointcheck.core.presentation.components.AppTopBar
import com.pointcheck.core.presentation.components.AppSelectorField
import com.pointcheck.core.presentation.components.DayScheduleRow
import com.pointcheck.core.presentation.components.AppOutlinedButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalProfileScreen(
    nav: NavController,
    vm: ProfessionalProfileViewModel = viewModel(),
    locationVm: LocationViewModel = viewModel()
) {
    val context = LocalContext.current
    val s by vm.state.collectAsState()
    val locState by locationVm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var displayName by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("30") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var expandedCategory by remember { mutableStateOf(false) }
    
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

    // Sincronizar campos cuando el perfil carga
    LaunchedEffect(s.profile) {
        s.profile?.let {
            displayName = it.displayName ?: ""
            businessName = it.businessName ?: ""
            specialty = it.specialty ?: ""
            description = it.description ?: ""
            address = it.address ?: ""
            city = it.city ?: ""
            duration = (it.defaultSessionDurationMinutes ?: 30).toString()
            selectedCategoryId = it.categoryId
            latitude = it.latitude
            longitude = it.longitude
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Perfil Profesional",
                onBack = { nav.popBackStack() },
                actions = {
                    if (!s.isEditing && s.profile != null) {
                        IconButton(onClick = { vm.toggleEdit() }, enabled = !s.isLoading) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { pad ->
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

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Información de Identidad", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        
                        AppTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = "Nombre Público",
                            leadingIcon = Icons.Default.Badge,
                            enabled = s.isEditing
                        )
                        
                        AppTextField(
                            value = businessName,
                            onValueChange = { businessName = it },
                            label = "Nombre de Empresa (Opcional)",
                            leadingIcon = Icons.Default.Business,
                            enabled = s.isEditing
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Especialidad y Servicios", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

                        // Selector de Categoría Refactorizado
                        Box {
                            val selectedCategory = s.categories.find { it.id == selectedCategoryId }
                            AppSelectorField(
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

                        AppTextField(
                            value = specialty,
                            onValueChange = { specialty = it },
                            label = "Título Profesional (Ej: Peluquero, Kinesiólogo)",
                            leadingIcon = Icons.Default.Work,
                            enabled = s.isEditing
                        )

                        AppTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = "Descripción / Bio",
                            leadingIcon = Icons.Default.Description,
                            enabled = s.isEditing,
                            minLines = 3
                        )
                        
                        AppTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = "Duración promedio cita (min)",
                            leadingIcon = Icons.Default.Timer,
                            enabled = s.isEditing,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Mi Horario de Atención", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text("Activa los días que atiendes y define tu horario.", style = MaterialTheme.typography.bodySmall)

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
                            DayScheduleRow(
                                dayName = daysTranslation[dayKey] ?: dayKey,
                                config = config,
                                onConfigChange = { vm.updateDayConfig(dayKey, it) },
                                enabled = s.isEditing
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Ubicación", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

                        Box {
                            Column {
                                AppTextField(
                                    value = address,
                                    onValueChange = { 
                                        address = it
                                        locationVm.getAddressSuggestions(it)
                                    },
                                    label = "Dirección de atención",
                                    leadingIcon = Icons.Default.Place,
                                    enabled = s.isEditing
                                )
                                if (s.isEditing) {
                                    Text(
                                        "Si trabajas a domicilio, indica tu comuna o punto de referencia principal",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
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
                                                address = fullAddress
                                                city = suggestion.locality ?: city
                                                latitude = suggestion.latitude
                                                longitude = suggestion.longitude
                                                locationVm.clearSuggestions()
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        AppTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = "Ciudad",
                            leadingIcon = Icons.Default.LocationCity,
                            enabled = s.isEditing
                        )

                        if (s.isEditing) {
                            AppOutlinedButton(
                                text = "Usar mi ubicación actual (GPS)",
                                icon = Icons.Default.MyLocation,
                                onClick = {
                                    locationVm.getCurrentLocation { lat, lng ->
                                        latitude = lat
                                        longitude = lng
                                        android.widget.Toast.makeText(
                                            context,
                                            "Ubicación activada: Ahora los clientes podrán ver tu zona de cobertura o local",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                    }
                                },
                                isLoading = locState.isLocating,
                                enabled = !locState.isLocating
                            )
                            
                            if (latitude != null && longitude != null) {
                                Text(
                                    "Coordenadas: ${"%.5f".format(latitude)}, ${"%.5f".format(longitude)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
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
                    AppButton(
                        text = "Guardar Perfil Profesional",
                        onClick = {
                            vm.saveProfile(
                                selectedCategoryId,
                                displayName,
                                businessName,
                                specialty,
                                description,
                                address,
                                city,
                                duration.toIntOrNull() ?: 30,
                                latitude,
                                longitude
                            )
                        },
                        isLoading = s.isLoading,
                        enabled = displayName.isNotBlank() && specialty.isNotBlank() && selectedCategoryId != null
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { vm.toggleEdit() }, modifier = Modifier.fillMaxWidth(), enabled = !s.isLoading) {
                        Text("Cancelar edición")
                    }
                } else {
                    AppButton(
                        text = "Editar mi Información",
                        onClick = { vm.toggleEdit() }, 
                        enabled = !s.isLoading
                    )
                }
                
                Spacer(Modifier.height(24.dp))
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
        AppButton(text = "Configurar ahora", onClick = onStart)
    }
}
