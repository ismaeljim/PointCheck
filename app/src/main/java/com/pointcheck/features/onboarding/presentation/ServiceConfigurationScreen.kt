package com.pointcheck.features.onboarding.presentation

import com.pointcheck.features.auth.data.dto.ServiceOfferingDto
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.core.navigation.Screen
import com.pointcheck.core.presentation.components.*
import com.pointcheck.features.auth.presentation.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceConfigurationScreen(
    categoryId: String,
    nav: NavController,
    authVm: UserViewModel,
    vm: CategoryViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val authState by authVm.state.collectAsState()
    val selectedServices = remember { mutableStateMapOf<String, ServiceOfferingDto>() }

    LaunchedEffect(categoryId) {
        vm.loadTemplates(categoryId)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Configuración",
                onBack = { nav.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(24.dp)
            ) {
                Text(
                    "Define los precios para los servicios que vas a ofrecer.",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.templates) { template ->
                        ServiceConfigItem(
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

                Box(Modifier.padding(16.dp)) {
                    AppButton(
                        text = "Finalizar Registro",
                        onClick = {
                            authVm.onServicesSelected(selectedServices.values.toList())
                            authVm.save {
                                nav.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Register.route) { inclusive = true }
                                }
                            }
                        },
                        enabled = selectedServices.isNotEmpty(),
                        isLoading = authState.isLoading
                    )
                }
            }
        }
    }
}

@Composable
fun ServiceConfigItem(
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

    AppCard(onClick = { enabled = !enabled }) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = enabled, 
                onCheckedChange = { enabled = it },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            
            Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(template.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(template.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (enabled) {
                AppTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = "Precio",
                    modifier = Modifier.width(90.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            } else {
                Text(
                    "$${template.defaultPrice}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
