package com.pointcheck.features.services.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.core.ui.components.*
import com.pointcheck.core.utils.FormatUtils
import com.pointcheck.features.services.data.dto.ServiceResponseDto

/**
 * Pantalla para gestionar el catálogo de servicios ofrecidos por un profesional.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceListScreen(
    nav: NavController,
    vm: ServiceViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }

    // Obtenemos la lista de servicios directamente del estado si es Success
    val services = (state as? ServiceUiState.Success)?.services ?: emptyList()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            PointCheckTopBar(
                title = "Mis Servicios",
                onBack = { nav.popBackStack() }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Nuevo Servicio") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { pad ->
        Box(modifier = Modifier.padding(pad).fillMaxSize()) {
            when (val s = state) {
                is ServiceUiState.Loading -> {
                    if (services.isEmpty()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
                is ServiceUiState.Error -> {
                    LaunchedEffect(s.message) {
                        snackbarHostState.showSnackbar(s.message)
                    }
                }
                is ServiceUiState.Success -> {
                    s.successMessage?.let {
                        LaunchedEffect(it) {
                            snackbarHostState.showSnackbar(it)
                        }
                    }
                }
            }

            if (services.isEmpty() && state !is ServiceUiState.Loading) {
                EmptyServicesState()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(services) { service ->
                        ServiceItem(
                            service = service,
                            enabled = state !is ServiceUiState.Loading,
                            onDelete = { vm.deleteService(service.id) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

        if (showAddDialog) {
            ServiceDialog(
                onDismiss = { showAddDialog = false },
                isLoading = state is ServiceUiState.Loading,
                onConfirm = { name, desc, price, dur ->
                    vm.addService(name, desc, price, dur)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun ServiceItem(
    service: ServiceResponseDto,
    enabled: Boolean,
    onDelete: () -> Unit
) {
    PointCheckCard(
        title = service.name,
        subtitle = service.description ?: "Sin descripción",
        icon = Icons.Default.MedicalServices,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(Modifier.weight(1f)) {
                    ServiceInfoChip(Icons.Default.AttachMoney, FormatUtils.formatCurrency(service.price))
                    Spacer(Modifier.width(12.dp))
                    ServiceInfoChip(Icons.Default.AccessTime, "${service.durationMinutes} min")
                }
                IconButton(onClick = onDelete, enabled = enabled) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar",
                        tint = if (enabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun ServiceInfoChip(icon: ImageVector, text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyServicesState() {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.MedicalServices,
            null,
            Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "No tienes servicios",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Crea tu primer servicio para empezar a recibir reservas.",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDialog(
    onDismiss: () -> Unit,
    isLoading: Boolean,
    onConfirm: (String, String, Double, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("60") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Servicio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PointCheckTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre del servicio",
                    placeholder = "Ej: Corte de Cabello",
                    leadingIcon = Icons.Default.MedicalServices
                )
                PointCheckTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = "Descripción",
                    placeholder = "Breve descripción del servicio",
                    leadingIcon = Icons.Default.Description
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PointCheckTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = "Precio (CLP)",
                        placeholder = "0",
                        leadingIcon = Icons.Default.AttachMoney,
                        modifier = Modifier.weight(1f)
                    )
                    PointCheckTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = "Minutos",
                        placeholder = "60",
                        leadingIcon = Icons.Default.AccessTime,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    onConfirm(name, desc, price.toDoubleOrNull() ?: 0.0, duration.toIntOrNull() ?: 30) 
                },
                enabled = name.isNotBlank() && price.isNotBlank() && !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(24.dp))
                else Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
