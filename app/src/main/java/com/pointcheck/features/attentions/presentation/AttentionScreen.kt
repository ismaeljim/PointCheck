package com.pointcheck.features.attentions.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.core.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttentionScreen(
    nav: NavController,
    reservationId: Long,
    clientId: Long,
    specialistId: Long,
    vm: AttentionViewModel = viewModel()
) {
    val s by vm.state.collectAsState()

    // Reseteamos el estado al entrar si es necesario o cargamos datos previos si existiera el endpoint
    // Como el backend no tiene GET by reservationId directo para atenciones activas sin conocer el ID de atención,
    // confiamos en el flujo de la UI.

    Scaffold(
        topBar = { TopAppBar(title = { Text("Atención al Cliente") }) }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Reserva #$reservationId", style = MaterialTheme.typography.labelLarge)
            
            Spacer(Modifier.height(16.dp))

            if (s.isLoading) {
                CircularProgressIndicator()
            } else if (s.currentAttention == null) {
                // Estado: Sin iniciar
                OutlinedTextField(
                    value = s.observations,
                    onValueChange = { vm.setObservations(it) },
                    label = { Text("Observaciones iniciales (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { vm.startAttention(reservationId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Iniciar Atención")
                }
            } else {
                // Estado: En progreso o Finalizada
                val att = s.currentAttention!!
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Atención ID: ${att.id}", style = MaterialTheme.typography.titleSmall)
                        Text("Estado: ${att.status}", color = MaterialTheme.colorScheme.primary)
                        Text("Inicio: ${att.startedAt}")
                        att.finishedAt?.let { Text("Fin: $it") }
                        att.durationMinutes?.let { Text("Duración: $it min") }
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = s.observations,
                    onValueChange = { vm.setObservations(it) },
                    label = { Text("Observaciones de la sesión") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    enabled = att.status != "FINISHED" && att.status != "COMPLETED"
                )

                Spacer(Modifier.height(24.dp))

                if (att.status != "FINISHED" && att.status != "COMPLETED") {
                    Button(
                        onClick = { vm.finishAttention() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Finalizar Atención")
                    }
                } else {
                    // Estado: Finalizada - Navegar a Billing
                    Text("La atención ha concluido.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { 
                            nav.navigate(
                                Screen.Billing.createRoute(
                                    reservationId,
                                    clientId,
                                    specialistId,
                                    att.id
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Registrar Cobro")
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = { nav.popBackStack() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Volver")
                    }
                }
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
