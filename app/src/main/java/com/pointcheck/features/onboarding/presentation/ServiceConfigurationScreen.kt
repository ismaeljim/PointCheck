package com.pointcheck.features.onboarding.presentation

import com.pointcheck.features.auth.data.dto.ServiceOfferingDto
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.core.navigation.Screen
import com.pointcheck.features.auth.presentation.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceConfigurationScreen(
    categoryId: Long,
    nav: NavController,
    authVm: UserViewModel,
    vm: CategoryViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val selectedServices = remember { mutableStateMapOf<Long, ServiceOfferingDto>() }

    LaunchedEffect(categoryId) {
        vm.loadTemplates(categoryId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configura tus Servicios") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(Modifier.padding(padding).fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "Selecciona los servicios que ofreces y ajusta sus precios.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(state.templates) { template ->
                        ServiceTemplateItem(
                            template = template,
                            onServiceChanged = { offering ->
                                if (offering != null) {
                                    selectedServices[template.id] = offering
                                } else {
                                    selectedServices.remove(template.id)
                                }
                            }
                        )
                    }
                }

                Button(
                    onClick = {
                        authVm.onServicesSelected(selectedServices.values.toList())
                        authVm.save {
                            nav.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        }
                    },
                    enabled = selectedServices.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp)
                ) {
                    Text("Finalizar Registro")
                }
            }
        }
    }
}

@Composable
fun ServiceTemplateItem(
    template: com.pointcheck.features.onboarding.presentation.dto.ServiceTemplateDto,
    onServiceChanged: (ServiceOfferingDto?) -> Unit
) {
    var enabled by remember { mutableStateOf(false) }
    var price by remember { mutableStateOf(template.defaultPrice.toString()) }

    LaunchedEffect(enabled, price) {
        if (enabled) {
            onServiceChanged(ServiceOfferingDto(template.id, price.toDoubleOrNull() ?: 0.0, template.unit))
        } else {
            onServiceChanged(null)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (enabled) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = enabled, onCheckedChange = { enabled = it })
            
            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                Text(template.name, fontWeight = FontWeight.Bold)
                Text(template.description, style = MaterialTheme.typography.bodySmall)
            }

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Precio") },
                modifier = Modifier.width(100.dp),
                suffix = { Text(if (template.unit == "SESSION") "Ses" else "Hr") },
                singleLine = true,
                enabled = enabled
            )
        }
    }
}
