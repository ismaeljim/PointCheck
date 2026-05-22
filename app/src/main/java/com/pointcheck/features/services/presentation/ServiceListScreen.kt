package com.pointcheck.features.services.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MedicalServices
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
fun ServiceListScreen(
    nav: NavController,
    vm: ServiceViewModel = viewModel()
) {
    val s by vm.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Catálogo de Servicios") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo Servicio") },
                expanded = !s.isLoading
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            if (s.isLoading && s.services.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (s.services.isEmpty()) {
                EmptyServicesState(onAdd = { showAddDialog = true })
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(s.services) { service ->
                        ServiceCard(
                            name = service.name,
                            description = service.description ?: "Sin descripción",
                            price = service.price,
                            duration = service.durationMinutes,
                            onDelete = { vm.deleteService(service.id) },
                            enabled = !s.isLoading
                        )
                    }
                }
            }

            if (showAddDialog) {
                AddServiceDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { name, desc, price, dur ->
                        vm.addService(name, desc, price, dur)
                        showAddDialog = false
                    },
                    isLoading = s.isLoading
                )
            }
        }
    }
}

@Composable
fun ServiceCard(
    name: String,
    description: String,
    price: Double?,
    duration: Int?,
    onDelete: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete, enabled = enabled) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = if (enabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline)
                }
            }
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(16.dp))
            
            Row(Modifier.fillMaxWidth()) {
                ServiceInfoChip(Icons.Default.AttachMoney, "$${price ?: 0.0}")
                Spacer(Modifier.width(12.dp))
                ServiceInfoChip(Icons.Default.AccessTime, "${duration ?: 0} min")
            }
        }
    }
}

@Composable
fun ServiceInfoChip(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun EmptyServicesState(onAdd: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.MedicalServices,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Tu catálogo está vacío",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Registra los servicios que ofreces para que tus clientes puedan agendar citas.",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAdd) {
            Text("Añadir primer servicio")
        }
    }
}

@Composable
fun AddServiceDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, Int) -> Unit,
    isLoading: Boolean = false
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("30") }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Nuevo Servicio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Nombre del servicio") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = desc, 
                    onValueChange = { desc = it }, 
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    enabled = !isLoading
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = price, 
                        onValueChange = { price = it }, 
                        label = { Text("Precio") },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                        prefix = { Text("$") },
                        enabled = !isLoading
                    )
                    OutlinedTextField(
                        value = duration, 
                        onValueChange = { duration = it }, 
                        label = { Text("Minutos") },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                        enabled = !isLoading
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onConfirm(name, desc, price.toDoubleOrNull() ?: 0.0, duration.toIntOrNull() ?: 30) 
                },
                enabled = name.isNotBlank() && price.isNotBlank() && !isLoading
            ) { 
                if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Crear") 
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text("Cancelar") }
        }
    )
}
