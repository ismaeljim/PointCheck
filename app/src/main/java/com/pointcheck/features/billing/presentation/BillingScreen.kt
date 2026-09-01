package com.pointcheck.features.billing.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.text.style.TextAlign
import com.pointcheck.core.ui.components.*

/**
 * Empty state component for Billing lists or summaries.
 */
@Composable
fun BillingEmptyState(
    message: String = "No hay registros de cobro para mostrar.",
    icon: ImageVector = Icons.AutoMirrored.Filled.ReceiptLong
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Screen for managing the billing process for a service attention.
 *
 * It allows specialists to:
 * - Create a billing record with an amount and payment method.
 * - View the summary of a transaction.
 * - Mark a pending billing as paid by providing an external reference.
 * - Cancel or void a billing record.
 *
 * @param nav Navigation controller.
 * @param reservationId The ID of the reservation associated with the billing.
 * @param attentionId The optional ID of the attention session.
 * @param vm ViewModel managing the billing state and operations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    nav: NavController,
    reservationId: String,
    attentionId: String?,
    vm: BillingViewModel = viewModel()
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Sprint 3: Suscripción al canal de errores del ViewModel (Resiliencia de Negocio)
    LaunchedEffect(Unit) {
        vm.errorEvents.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long,
                withDismissAction = true
            )
        }
    }

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
            PointCheckTopBar(
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
            if (s.isLoading && s.currentBilling == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (s.currentBilling == null && !s.isLoading) {
                // If it's a new billing, show form. If it was a failed load, maybe an empty state?
                // For now, if reservationId is provided, we assume we want to create or load it.
                // If the VM finished loading and currentBilling is still null, we show the creation form.

                val isPaid = s.currentBilling?.status == "PAID"

                // Formulario de creación
                Text(
                    "Información del Cobro",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (isPaid) "Este cobro ya ha sido procesado." else "Complete los datos para generar el registro de pago.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isPaid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(24.dp))

                PointCheckCard(
                    title = "Información del Cobro",
                    subtitle = if (isPaid) "Este cobro ya ha sido procesado." else "Complete los datos para generar el registro de pago.",
                    icon = Icons.Default.Payments,
                    iconColor = if (isPaid) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        PointCheckTextField(
                            value = s.amount,
                            onValueChange = { vm.setAmount(it) },
                            label = "Monto a Cobrar ($)",
                            placeholder = "0.0",
                            leadingIcon = Icons.Default.Payments,
                            enabled = !s.isLoading && !isPaid
                        )

                        Spacer(Modifier.height(20.dp))

                        Text(
                            "Método de Pago",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isPaid) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
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
                                        onClick = { if (!s.isLoading && !isPaid) vm.setPaymentMethod(code) }
                                    )
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = s.paymentMethod == code,
                                    onClick = { if (!s.isLoading && !isPaid) vm.setPaymentMethod(code) },
                                    enabled = !s.isLoading && !isPaid
                                )
                                Text(
                                    label, 
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isPaid) MaterialTheme.colorScheme.outline else Color.Unspecified
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        PointCheckTextField(
                            value = s.notes,
                            onValueChange = { vm.setNotes(it) },
                            label = "Notas (Opcional)",
                            placeholder = "Comentarios adicionales",
                            leadingIcon = Icons.Default.Description,
                            enabled = !s.isLoading && !isPaid
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                if (isPaid) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "SERVICIO PAGADO",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    PointCheckOutlinedButton(
                        text = "Volver al Listado",
                        onClick = { nav.popBackStack() },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    PointCheckButton(
                        text = "Generar Cobro",
                        onClick = { vm.createBillingRecord(reservationId, attentionId) },
                        enabled = !s.isLoading && s.amount.isNotBlank(),
                        isLoading = s.isLoading
                    )
                }
            } else {
                // Detalle del cobro creado
                val billing = s.currentBilling!!
                
                if (billing.status == "PENDING") {
                    // Botón para abrir el Modal de Selección de Pago
                    PointCheckButton(
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

                PointCheckCard(
                    title = "Resumen de Transacción",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    modifier = Modifier.fillMaxWidth(),
                    badgeText = billing.status,
                    badgeColor = if (billing.status == "PAID") Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                ) {
                    Column {
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
                    
                    PointCheckTextField(
                        value = s.externalReference,
                        onValueChange = { vm.setExternalReference(it) },
                        label = "Número de Referencia / Comprobante",
                        placeholder = "Ej: 12345678",
                        leadingIcon = Icons.Default.ConfirmationNumber,
                        enabled = !s.isLoading
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Row(Modifier.fillMaxWidth()) {
                        PointCheckButton(
                            text = "Registrar Pago",
                            onClick = { vm.markAsPaid(billing.id) },
                            modifier = Modifier.weight(1f),
                            isLoading = s.isLoading
                        )
                        Spacer(Modifier.width(12.dp))
                        PointCheckOutlinedButton(
                            text = "Anular",
                            onClick = { vm.cancelBillingRecord(billing.id) },
                            modifier = Modifier.weight(1f),
                            enabled = !s.isLoading
                        )
                    }
                } else {
                    PointCheckButton(
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

/**
 * Modal dialog for selecting a payment method.
 *
 * @param amount The total amount to be paid.
 * @param onSelectMethod Callback triggered when a payment method is selected.
 * @param onDismiss Callback to close the modal.
 */
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

/**
 * Represents an individual payment option within the selection modal.
 *
 * @param id Unique identifier for the payment method.
 * @param label Human-readable name of the payment method.
 * @param icon Icon representing the payment method.
 * @param onSelect Callback triggered when this option is selected.
 */
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

/**
 * Displays a single row of information in the billing summary.
 *
 * @param icon Icon for the information type.
 * @param label Label for the data.
 * @param value The actual data value.
 * @param color Optional text color for the value.
 */
@Composable
fun BillingInfoRow(icon: ImageVector, label: String, value: String, color: Color = Color.Unspecified) {
    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text("$label: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}
