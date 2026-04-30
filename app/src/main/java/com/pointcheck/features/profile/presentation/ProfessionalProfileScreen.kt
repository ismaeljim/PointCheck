package com.pointcheck.features.profile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
    
    var displayName by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("30") }

    // Sincronizar campos cuando el perfil carga
    LaunchedEffect(s.profile) {
        s.profile?.let {
            displayName = it.displayName
            businessName = it.businessName ?: ""
            specialty = it.specialty
            description = it.description ?: ""
            address = it.address ?: ""
            city = it.city ?: ""
            duration = it.defaultSessionDurationMinutes.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil Profesional") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (s.profile == null && !s.isLoading && !s.isEditing) {
                Text("Aún no tienes un perfil profesional configurado.")
                Button(onClick = { vm.toggleEdit() }, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Crear Perfil Ahora")
                }
            } else {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Nombre Público") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = s.isEditing
                )
                
                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Nombre de Fantasía / Empresa") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = s.isEditing
                )

                OutlinedTextField(
                    value = specialty,
                    onValueChange = { specialty = it },
                    label = { Text("Especialidad") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = s.isEditing
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    enabled = s.isEditing
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = s.isEditing
                )

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Ciudad") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = s.isEditing
                )

                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duración Cita (min)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = s.isEditing
                )

                Spacer(Modifier.height(16.dp))

                if (s.isEditing) {
                    Button(
                        onClick = {
                            vm.saveProfile(displayName, businessName, specialty, description, address, city, duration.toIntOrNull() ?: 30)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !s.isLoading
                    ) {
                        if (s.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        else Text("Guardar Cambios")
                    }
                    TextButton(onClick = { vm.toggleEdit() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancelar")
                    }
                } else {
                    Button(onClick = { vm.toggleEdit() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Editar Perfil")
                    }
                }

                s.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                }
                s.successMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}
