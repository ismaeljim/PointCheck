package com.pointcheck.features.profile.presentation

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.core.location.LocationViewModel
import com.pointcheck.core.presentation.components.*

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
                        IconButton(onClick = { vm.toggleEdit() }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { pad ->
        Box(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (s.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (s.profile == null && !s.isEditing) {
                EmptyState(
                    title = "Crea tu perfil",
                    description = "Para ofrecer servicios, primero debes configurar tu perfil público.",
                    icon = Icons.Default.Work,
                    actionText = "Configurar ahora",
                    onAction = { vm.toggleEdit() }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (s.isEditing) "Editando tu información" else "Información de tu negocio",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))

                    AppCard {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Datos Principales", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            
                            AppTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                label = "Nombre para Clientes",
                                leadingIcon = Icons.Default.Person,
                                enabled = s.isEditing
                            )
                            
                            AppTextField(
                                value = businessName,
                                onValueChange = { businessName = it },
                                label = "Nombre de Empresa (Opcional)",
                                leadingIcon = Icons.Default.Business,
                                enabled = s.isEditing
                            )

                            Box {
                                val selectedCategory = s.categories.find { it.id == selectedCategoryId }
                                AppSelectorField(
                                    label = "Categoría",
                                    value = selectedCategory?.name ?: "Elegir...",
                                    icon = Icons.Default.Category,
                                    onClick = { if (s.isEditing) expandedCategory = true },
                                    enabled = s.isEditing
                                )
                                DropdownMenu(
                                    expanded = expandedCategory,
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
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    AppCard {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Servicio", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

                            AppTextField(
                                value = specialty,
                                onValueChange = { specialty = it },
                                label = "Tu Especialidad",
                                leadingIcon = Icons.Default.WorkspacePremium,
                                enabled = s.isEditing
                            )

                            AppTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = "Descripción de lo que haces",
                                leadingIcon = Icons.Default.Description,
                                enabled = s.isEditing,
                                minLines = 3
                            )
                            
                            AppTextField(
                                value = duration,
                                onValueChange = { duration = it },
                                label = "Duración promedio (min)",
                                leadingIcon = Icons.Default.Timer,
                                enabled = s.isEditing,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    AppCard {
                        Column(Modifier.padding(16.dp)) {
                            Text("Horarios de Atención", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))

                            val days = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")
                            val names = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

                            days.zip(names).forEach { (key, name) ->
                                val config = s.workingHours[key] ?: DayConfig()
                                DayScheduleRow(
                                    dayName = name,
                                    config = config,
                                    onConfigChange = { vm.updateDayConfig(key, it) },
                                    enabled = s.isEditing
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    AppCard {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Ubicación", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

                            Box {
                                AppTextField(
                                    value = address,
                                    onValueChange = { 
                                        address = it
                                        locationVm.getAddressSuggestions(it)
                                    },
                                    label = "Dirección",
                                    leadingIcon = Icons.Default.Place,
                                    enabled = s.isEditing
                                )

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
                                                text = { Text(fullAddress) },
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
                                    text = "Obtener GPS actual",
                                    icon = Icons.Default.MyLocation,
                                    onClick = {
                                        locationVm.getCurrentLocation { lat, lng ->
                                            latitude = lat
                                            longitude = lng
                                        }
                                    },
                                    isLoading = locState.isLocating
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    if (s.isEditing) {
                        AppButton(
                            text = "Guardar Cambios",
                            onClick = {
                                vm.saveProfile(
                                    selectedCategoryId, displayName, businessName, specialty, 
                                    description, address, city, duration.toIntOrNull() ?: 30, latitude, longitude
                                )
                            },
                            isLoading = s.isLoading
                        )
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = { vm.toggleEdit() }, modifier = Modifier.fillMaxWidth()) {
                            Text("Cancelar Edición", color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        AppButton(
                            text = "Modificar Perfil",
                            onClick = { vm.toggleEdit() }
                        )
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}
