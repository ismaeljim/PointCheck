package com.pointcheck.features.services.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.core.presentation.components.HeaderIcon
import com.pointcheck.core.presentation.components.SectionHeader

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- HEADER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderIcon(Icons.AutoMirrored.Filled.ArrowBack) { nav.popBackStack() }
                Spacer(Modifier.width(16.dp))
                Text(
                    "Catálogo de Servicios",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        // --- CONTENT ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-20).dp)
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                if (s.isLoading && s.services.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                    }
                } else if (s.services.isEmpty()) {
                    EmptyServicesState(onAdd = { showAddDialog = true })
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item { SectionHeader("Mis servicios publicados") }
                        
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
                        item { Spacer(Modifier.height(100.dp)) }
                    }
                }
            }
            
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Nuevo Servicio")
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = onDelete, 
                    enabled = enabled,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar")
                }
            }
            
            Spacer(Modifier.height(20.dp))
            
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ServiceInfoChip(Icons.Outlined.AttachMoney, "$${price ?: 0.0}")
                Spacer(Modifier.width(24.dp))
                ServiceInfoChip(Icons.Outlined.AccessTime, "${duration ?: 0} min")
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
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
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
            Icons.Outlined.MedicalServices,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Tu catálogo está vacío",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Registra los servicios que ofreces para que tus clientes puedan agendar citas.",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onAdd, shape = RoundedCornerShape(12.dp)) {
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
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = desc, 
                    onValueChange = { desc = it }, 
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = price, 
                        onValueChange = { price = it }, 
                        label = { Text("Precio") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        prefix = { Text("$") },
                        enabled = !isLoading
                    )
                    OutlinedTextField(
                        value = duration, 
                        onValueChange = { duration = it }, 
                        label = { Text("Minutos") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
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
                enabled = name.isNotBlank() && price.isNotBlank() && !isLoading,
                shape = RoundedCornerShape(12.dp)
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
