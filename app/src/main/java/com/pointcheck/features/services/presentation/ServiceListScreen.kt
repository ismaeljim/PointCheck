package com.pointcheck.features.services.presentation

import com.pointcheck.core.presentation.components.AppTopBar
import com.pointcheck.core.presentation.components.AppButton
import com.pointcheck.core.presentation.components.AppTextField
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

/**
 * Screen for managing the catalog of services offered by a professional.
 *
 * This screen provides a list of all services registered by the current user. It allows
 * adding new services through a dialog and deleting existing ones. It observes the
 * state from [ServiceViewModel].
 *
 * Features:
 * - Lazy list of services with name, description, price, and duration.
 * - Floating action button to trigger the "Add Service" dialog.
 * - Integrated error and success message handling via Snackbar.
 * - Empty state illustration when no services are configured.
 *
 * @param nav [NavController] used for navigating back.
 * @param vm [ServiceViewModel] that manages the service data and operations.
 */
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
            AppTopBar(
                title = "Catálogo de Servicios",
                onBack = { nav.popBackStack() }
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
        AppButton(text = "Añadir primer servicio", onClick = onAdd)
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
                AppTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = "Nombre del servicio",
                    enabled = !isLoading
                )
                AppTextField(
                    value = desc, 
                    onValueChange = { desc = it }, 
                    label = "Descripción",
                    enabled = !isLoading
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppTextField(
                        value = price, 
                        onValueChange = { price = it }, 
                        label = "Precio",
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    AppTextField(
                        value = duration, 
                        onValueChange = { duration = it }, 
                        label = "Minutos",
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
            }
        },
        confirmButton = {
            AppButton(
                text = "Crear",
                onClick = { 
                    onConfirm(name, desc, price.toDoubleOrNull() ?: 0.0, duration.toIntOrNull() ?: 30) 
                },
                modifier = Modifier.width(100.dp),
                enabled = name.isNotBlank() && price.isNotBlank() && !isLoading,
                isLoading = isLoading
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text("Cancelar") }
        }
    )
}
