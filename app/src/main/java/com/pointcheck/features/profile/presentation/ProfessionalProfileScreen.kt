package com.pointcheck.features.profile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
                        
                        ProfileField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = "Nombre Público",
                            icon = Icons.Default.Badge,
                            enabled = s.isEditing
                        )
                        
                        ProfileField(
                            value = businessName,
                            onValueChange = { businessName = it },
                            label = "Nombre de Empresa (Opcional)",
                            icon = Icons.Default.Business,
                            enabled = s.isEditing
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Especialidad y Servicios", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

                        ProfileField(
                            value = specialty,
                            onValueChange = { specialty = it },
                            label = "Especialidad Principal",
                            icon = Icons.Default.Work,
                            enabled = s.isEditing
                        )

                        ProfileField(
                            value = description,
                            onValueChange = { description = it },
                            label = "Descripción / Bio",
                            icon = Icons.Default.Description,
                            enabled = s.isEditing,
                            minLines = 3
                        )
                        
                        ProfileField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = "Duración promedio cita (min)",
                            icon = Icons.Default.Timer,
                            enabled = s.isEditing
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Ubicación", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

                        ProfileField(
                            value = address,
                            onValueChange = { address = it },
                            label = "Dirección de atención",
                            icon = Icons.Default.Place,
                            enabled = s.isEditing
                        )

                        ProfileField(
                            value = city,
                            onValueChange = { city = it },
                            label = "Ciudad",
                            icon = Icons.Default.LocationCity,
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
                    Button(
                        onClick = {
                            vm.saveProfile(displayName, businessName, specialty, description, address, city, duration.toIntOrNull() ?: 30)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = !s.isLoading && displayName.isNotBlank() && specialty.isNotBlank()
                    ) {
                        if (s.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        else Text("Guardar Perfil Profesional")
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { vm.toggleEdit() }, modifier = Modifier.fillMaxWidth(), enabled = !s.isLoading) {
                        Text("Cancelar edición")
                    }
                } else {
                    Button(
                        onClick = { vm.toggleEdit() }, 
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = !s.isLoading
                    ) {
                        Text("Editar mi Información")
                    }
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
