package com.pointcheck.features.attentions.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

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
    var observations by remember { mutableStateOf("") }

    LaunchedEffect(reservationId) {
        vm.loadAttentionForReservation(reservationId)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Atención en curso") }) }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (s.isLoading) {
                CircularProgressIndicator()
            } else if (s.currentAttention == null) {
                Text("No hay una atención activa para esta reserva.")
                Spacer(Modifier.height(16.dp))
                Button(onClick = { vm.startAttention(reservationId, clientId, specialistId) }) {
                    Text("Iniciar Atención Ahora")
                }
            } else {
                Text("Atención ID: ${s.currentAttention?.id}", style = MaterialTheme.typography.labelSmall)
                Text("Iniciada a las: ${s.currentAttention?.startedAt}", style = MaterialTheme.typography.bodyMedium)
                
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = observations,
                    onValueChange = { observations = it },
                    label = { Text("Observaciones / Notas de la sesión") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5,
                    enabled = !s.isFinished
                )

                Spacer(Modifier.height(24.dp))

                if (!s.isFinished) {
                    Button(
                        onClick = { vm.finishAttention(observations) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Finalizar Atención")
                    }
                } else {
                    Text("Atención Finalizada", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineSmall)
                    Text("Duración: ${s.currentAttention?.durationMinutes} min", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { nav.popBackStack() }) {
                        Text("Volver a la Agenda")
                    }
                }
            }

            s.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}
