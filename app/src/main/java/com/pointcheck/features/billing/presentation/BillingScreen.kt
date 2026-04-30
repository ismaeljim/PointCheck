package com.pointcheck.features.billing.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    nav: NavController,
    reservationId: Long,
    @Suppress("UNUSED_PARAMETER") clientId: Long,
    @Suppress("UNUSED_PARAMETER") specialistId: Long,
    attentionId: Long?,
    vm: BillingViewModel = viewModel()
) {
    val s by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Cobro") },
                navigationIcon = {
                    TextButton(onClick = { nav.popBackStack() }) {
                        Text("Atrás")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            if (s.currentBilling == null) {
                // Formulario de creación
                Text("Crear Registro de Cobro", style = MaterialTheme.typography.titleMedium)
                Text("Reserva #$reservationId", style = MaterialTheme.typography.bodySmall)
                attentionId?.let { Text("Atención #$it", style = MaterialTheme.typography.bodySmall) }
                
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = s.amount,
                    onValueChange = { vm.setAmount(it) },
                    label = { Text("Monto ($)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                Text("Método de Pago:")
                val methods = listOf("CASH", "TRANSFER", "CARD", "OTHER")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    methods.forEach { method ->
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            RadioButton(
                                selected = s.paymentMethod == method,
                                onClick = { vm.setPaymentMethod(method) }
                            )
                            Text(method, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = s.notes,
                    onValueChange = { vm.setNotes(it) },
                    label = { Text("Notas / Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { vm.createBillingRecord(reservationId, attentionId) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !s.isLoading
                ) {
                    Text("Registrar Cobro")
                }
            } else {
                // Detalle del cobro creado
                val billing = s.currentBilling!!
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Detalle del Cobro", style = MaterialTheme.typography.headlineSmall)
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        Text("ID Cobro: ${billing.id}")
                        Text("Estado: ${billing.status}", 
                             color = if (billing.status == "PAID") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        Text("Monto: ${billing.amount} ${billing.currency}")
                        Text("Método inicial: ${billing.paymentMethod ?: "No definido"}")
                        Text("Creado: ${billing.createdAt}")
                        
                        billing.paidAt?.let { Text("Pagado el: $it") }
                        billing.externalReference?.let { Text("Ref: $it") }
                        billing.notes?.let { Text("Notas: $it") }
                    }
                }

                Spacer(Modifier.height(24.dp))

                if (billing.status == "PENDING") {
                    Text("Completar Pago", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = s.externalReference,
                        onValueChange = { vm.setExternalReference(it) },
                        label = { Text("Referencia Externa (N° Transacción)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Row(Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { vm.markAsPaid(billing.id) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Pagar")
                        }
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { vm.cancelBillingRecord(billing.id) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Cancelar")
                        }
                    }
                } else {
                    Button(
                        onClick = { nav.popBackStack() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Finalizar y Volver")
                    }
                }
            }

            if (s.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
            }

            s.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
            s.successMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}
