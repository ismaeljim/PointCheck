package com.pointcheck.features.billing.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.features.billing.data.dto.BillingRecordRequestDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    nav: NavController,
    reservationId: Long,
    clientId: Long,
    specialistId: Long,
    attentionId: Long?,
    vm: BillingViewModel = viewModel()
) {
    val s by vm.state.collectAsState()
    var amount by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("Efectivo") }
    var notes by remember { mutableStateOf("") }

    LaunchedEffect(reservationId) {
        vm.loadBillingByReservation(reservationId)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Registro de Cobro") }) }
    ) { pad ->
        Column(modifier = Modifier.padding(pad).padding(16.dp).fillMaxSize()) {
            
            if (s.record == null) {
                Text("Registrar nuevo cobro externo", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Monto ($)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text("Método de Pago:")
                Row {
                    RadioButton(selected = method == "Efectivo", onClick = { method = "Efectivo" })
                    Text("Efectivo", modifier = Modifier.padding(top = 12.dp))
                    Spacer(Modifier.width(16.dp))
                    RadioButton(selected = method == "Transferencia", onClick = { method = "Transferencia" })
                    Text("Transferencia", modifier = Modifier.padding(top = 12.dp))
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas / Referencia Externa") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        val req = BillingRecordRequestDto(
                            reservationId = reservationId,
                            attentionId = attentionId,
                            clientId = clientId,
                            specialistId = specialistId,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            paymentMethod = method,
                            notes = notes
                        )
                        vm.createBilling(req)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !s.isLoading
                ) {
                    Text("Registrar Cobro")
                }
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Detalle del Cobro", style = MaterialTheme.typography.headlineSmall)
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        Text("Estado: ${s.record?.status}")
                        Text("Monto: ${s.record?.amount} ${s.record?.currency}")
                        Text("Método: ${s.record?.paymentMethod}")
                        Text("Fecha Pago: ${s.record?.paidAt ?: "Pendiente"}")
                        s.record?.notes?.let { Text("Notas: $it") }
                        
                        if (s.record?.status == "PENDING") {
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { vm.updateStatus(s.record!!.id, "PAID") }) {
                                Text("Marcar como PAGADO")
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                Button(onClick = { nav.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Cerrar")
                }
            }

            if (s.error != null) {
                Text(s.error!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
