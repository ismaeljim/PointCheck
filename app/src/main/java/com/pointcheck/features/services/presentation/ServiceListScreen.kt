package com.pointcheck.features.services.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceListScreen(
    nav: NavController,
    vm: ServiceViewModel = viewModel()
) {
    val s by vm.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Servicios") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Servicio")
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            if (s.isLoading && s.services.isEmpty()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else if (s.services.isEmpty()) {
                Text("No tienes servicios creados.", Modifier.align(Alignment.Center))
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(s.services) { service ->
                        ListItem(
                            headlineContent = { Text(service.name) },
                            supportingContent = { Text("${service.description ?: ""} - ${service.durationMinutes} min") },
                            trailingContent = {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("$${service.price}", style = MaterialTheme.typography.titleMedium)
                                    IconButton(onClick = { vm.deleteService(service.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }

            if (showAddDialog) {
                AddServiceDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { name, desc, price, dur ->
                        vm.addService(name, desc, price, dur)
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun AddServiceDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("30") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Servicio") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre del servicio") })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Descripción") })
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Precio") })
                OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duración (min)") })
            }
        },
        confirmButton = {
            Button(onClick = { 
                onConfirm(name, desc, price.toDoubleOrNull() ?: 0.0, duration.toIntOrNull() ?: 30) 
            }) { Text("Añadir") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
