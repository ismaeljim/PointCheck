package com.pointcheck.features.billing.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.pointcheck.core.presentation.components.AppButton
import com.pointcheck.core.presentation.components.AppOutlinedButton
import com.pointcheck.core.presentation.components.AppTextField
import com.pointcheck.core.presentation.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    nav: NavController,
    reservationId: String,
    attentionId: String?,
    vm: BillingViewModel = viewModel()
) {
    val s by vm.state.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(reservationId) {
        vm.loadBillingByReservation(reservationId)
    }

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
            AppTopBar(
                title = "Gestión de Cobro",
                onBack = { nav.popBackStack() }
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
                        AppTextField(
                            value = s.amount,
                            onValueChange = { vm.setAmount(it) },
                            label = "Monto a Cobrar",
                            leadingIcon = Icons.Default.AttachMoney,
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

                        AppTextField(
                            value = s.notes,
                            onValueChange = { vm.setNotes(it) },
                            label = "Notas (Opcional)",
                            leadingIcon = Icons.Default.Description,
                            enabled = !s.isLoading
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                AppButton(
                    text = "Generar Cobro",
                    onClick = { vm.createBillingRecord(reservationId, attentionId) },
                    enabled = !s.isLoading && s.amount.isNotBlank(),
                    isLoading = s.isLoading
                )
            } else {
                // Detalle del cobro creado
                val billing = s.currentBilling!!
                
                if (billing.status == "PENDING") {
                    // Botón para abrir el Modal de Selección de Pago
                    AppButton(
                        text = "Seleccionar Método de Pago",
                        onClick = { vm.setShowPaymentModal(true) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(24.dp))
                }

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
                        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        BillingInfoRow(Icons.Default.Person, "Cliente", billing.client.name)
                        BillingInfoRow(Icons.Default.Wallet, "Especialista", billing.specialist.name)
                        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
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
                    
                    AppTextField(
                        value = s.externalReference,
                        onValueChange = { vm.setExternalReference(it) },
                        label = "Número de Referencia / Comprobante",
                        enabled = !s.isLoading
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Row(Modifier.fillMaxWidth()) {
                        AppButton(
                            text = "Registrar Pago",
                            onClick = { vm.markAsPaid(billing.id) },
                            modifier = Modifier.weight(1f),
                            isLoading = s.isLoading
                        )
                        Spacer(Modifier.width(12.dp))
                        AppOutlinedButton(
                            text = "Anular",
                            onClick = { vm.cancelBillingRecord(billing.id) },
                            modifier = Modifier.weight(1f),
                            enabled = !s.isLoading
                        )
                    }
                } else {
                    AppButton(
                        text = "Finalizar y Volver",
                        onClick = { nav.popBackStack() }
                    )
                }
            }
        }

        if (s.showPaymentModal && s.currentBilling != null) {
            PaymentSelectionModal(
                amount = s.currentBilling!!.amount,
                onSelectMethod = { method ->
                    vm.setPaymentMethod(method)
                    if (method != "CASH") {
                        vm.markAsPaid(s.currentBilling!!.id)
                    }
                    vm.setShowPaymentModal(false)
                },
                onDismiss = { vm.setShowPaymentModal(false) }
            )
        }
    }
}

@Composable
fun PaymentSelectionModal(
    amount: Double,
    onSelectMethod: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Pago ($amount CLP)") },
        text = {
            Column {
                PaymentOptionItem("TRANSFER", "Transferencia Bancaria", Icons.Default.AccountBalance, onSelectMethod)
                PaymentOptionItem("CARD", "Tarjeta Débito/Crédito", Icons.Default.CreditCard, onSelectMethod)
                PaymentOptionItem("CASH", "Efectivo", Icons.Default.Payments, onSelectMethod)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun PaymentOptionItem(id: String, label: String, icon: ImageVector, onSelect: (String) -> Unit) {
    Surface(
        onClick = { onSelect(id) },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun BillingInfoRow(icon: ImageVector, label: String, value: String, color: Color = Color.Unspecified) {
    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text("$label: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}
