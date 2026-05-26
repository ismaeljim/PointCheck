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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import com.pointcheck.core.presentation.components.AppButton
import com.pointcheck.core.presentation.components.AppTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalProfileScreen(
    nav: NavController,
    vm: ProfessionalProfileViewModel = viewModel()
) {
    val s by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var displayName by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("30") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var expandedCategory by remember { mutableStateOf(false) }

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
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil Profesional") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }, enabled = !s.isLoading) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
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

                        // Selector de Categoría (Visual)
                        ExposedDropdownMenuBox(
                            expanded = expandedCategory && s.isEditing,
                            onExpandedChange = { if (s.isEditing) expandedCategory = !expandedCategory },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val selectedCategory = s.categories.find { it.id == selectedCategoryId }
                            OutlinedTextField(
                                value = selectedCategory?.name ?: "Seleccionar Categoría",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Categoría de Servicio") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                                leadingIcon = { Icon(Icons.Default.Category, null, tint = MaterialTheme.colorScheme.primary) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                enabled = s.isEditing,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = expandedCategory && s.isEditing,
                                onDismissRequest = { expandedCategory = false }
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
                            enabled = s.isEditing
                        )
                        
                        AppTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = "Duración promedio cita (min)",
                            leadingIcon = Icons.Default.Timer,
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
                        Text("Ubicación", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

                        AppTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = "Dirección de atención",
                            leadingIcon = Icons.Default.Place,
                            enabled = s.isEditing
                        )

                        AppTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = "Ciudad",
                            leadingIcon = Icons.Default.LocationCity,
                            enabled = s.isEditing
                        )
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
                            vm.saveProfile(selectedCategoryId, displayName, businessName, specialty, description, address, city, duration.toIntOrNull() ?: 30)
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
fun ProfileField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        leadingIcon = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
        shape = MaterialTheme.shapes.medium,
        minLines = minLines
    )
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
        Button(onClick = onStart, modifier = Modifier.height(56.dp).fillMaxWidth()) {
            Text("Configurar ahora")
        }
    }
}

@Composable
fun ProfileStatusMessage(msg: String, isError: Boolean) {
    val containerColor = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
    
    Card(
        modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Text(
            msg,
            color = contentColor,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
