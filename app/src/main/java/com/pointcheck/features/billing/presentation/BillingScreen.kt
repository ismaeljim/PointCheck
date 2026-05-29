package com.pointcheck.features.billing.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
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
fun BillingScreen(
    nav: NavController,
    reservationId: String,
    @Suppress("UNUSED_PARAMETER") clientId: String,
    @Suppress("UNUSED_PARAMETER") specialistId: String,
    attentionId: String?,
    vm: BillingViewModel = viewModel()
) {
    val s by vm.state.collectAsState()
    val scrollState = rememberScrollState()
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Cobro") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            if (s.currentBilling == null) {
                // Formulario de creación
                Text(
                    "Información del Cobro",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Complete los datos para generar el registro de pago.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = s.amount,
                            onValueChange = { vm.setAmount(it) },
                            label = { Text("Monto a Cobrar") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                            shape = MaterialTheme.shapes.medium,
                            prefix = { Text("$") },
                            enabled = !s.isLoading
                        )

                        Spacer(Modifier.height(20.dp))

                        Text(
                            "Método de Pago",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        val methods = listOf(
                            "CASH" to "Efectivo", 
                            "TRANSFER" to "Transferencia", 
                            "CARD" to "Tarjeta", 
                            "OTHER" to "Otro"
                        )
                        
                        methods.forEach { (code, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = s.paymentMethod == code,
                                        onClick = { if (!s.isLoading) vm.setPaymentMethod(code) }
                                    )
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = s.paymentMethod == code,
                                    onClick = { if (!s.isLoading) vm.setPaymentMethod(code) },
                                    enabled = !s.isLoading
                                )
                                Text(label, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = s.notes,
                            onValueChange = { vm.setNotes(it) },
                            label = { Text("Notas (Opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Description, null) },
                            shape = MaterialTheme.shapes.medium,
                            enabled = !s.isLoading
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = { vm.createBillingRecord(reservationId, attentionId) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !s.isLoading && s.amount.isNotBlank()
                ) {
                    if (s.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    else Text("Generar Cobro")
                }
            } else {
                // Detalle del cobro creado
                val billing = s.currentBilling!!
                
                Text(
                    "Resumen de Transacción",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                ) {
                    Column(Modifier.padding(20.dp)) {
                        BillingInfoRow(Icons.Default.Receipt, "ID Cobro", "#${billing.id}")
                        BillingInfoRow(
                            Icons.Default.Payment, 
                            "Estado", 
                            billing.status,
                            color = if (billing.status == "PAID") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        BillingInfoRow(Icons.Default.AttachMoney, "Monto", "${billing.amount} ${billing.currency}")
                        BillingInfoRow(Icons.Default.CreditCard, "Método", billing.paymentMethod ?: "No definido")
                        BillingInfoRow(Icons.Default.History, "Fecha", billing.createdAt)
                        
                        billing.paidAt?.let { BillingInfoRow(Icons.Default.History, "Pagado el", it) }
                        billing.externalReference?.let { BillingInfoRow(Icons.Default.Description, "Ref", it) }
                    }
                }

                Spacer(Modifier.height(32.dp))

                if (billing.status == "PENDING") {
                    Text(
                        "Confirmar Pago",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = s.externalReference,
                        onValueChange = { vm.setExternalReference(it) },
                        label = { Text("Número de Referencia / Comprobante") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        enabled = !s.isLoading
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Row(Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { vm.markAsPaid(billing.id) },
                            modifier = Modifier.weight(1f).height(56.dp),
                            enabled = !s.isLoading
                        ) {
                            if (s.isLoading) CircularProgressIndicator(Modifier.size(24.dp))
                            else Text("Registrar Pago")
                        }
                        Spacer(Modifier.width(12.dp))
                        OutlinedButton(
                            onClick = { vm.cancelBillingRecord(billing.id) },
                            modifier = Modifier.weight(1f).height(56.dp),
                            enabled = !s.isLoading,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Anular")
                        }
                    }
                } else {
                    Button(
                        onClick = { nav.popBackStack() },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Finalizar y Volver")
                    }
                }
            }

            // Eliminados mensajes estáticos redundantes
        }
    }
}

@Composable
fun BillingInfoRow(icon: ImageVector, label: String, value: String, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface) {
    Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text("$label:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.width(4.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = color)
    }
}

