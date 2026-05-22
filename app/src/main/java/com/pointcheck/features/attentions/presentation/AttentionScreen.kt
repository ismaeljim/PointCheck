package com.pointcheck.features.attentions.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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

    val scrollState = rememberScrollState()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Módulo de Atención") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Información de Reserva #$reservationId",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))

            if (s.isLoading) {
                CircularProgressIndicator()
            } else if (s.currentAttention == null) {
                // Estado: Sin iniciar
                Text(
                    "Iniciar Nueva Atención",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Text(
                    "Registre observaciones iniciales antes de comenzar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = s.observations,
                    onValueChange = { vm.setObservations(it) },
                    label = { Text("Observaciones iniciales") },
                    placeholder = { Text("Ej: Motivo de consulta, estado inicial...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = MaterialTheme.shapes.medium,
                    enabled = !s.isLoading
                )
                
                Spacer(Modifier.height(24.dp))
                
                Button(
                    onClick = { vm.startAttention(reservationId) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !s.isLoading
                ) {
                    if (s.isLoading) CircularProgressIndicator(Modifier.size(24.dp))
                    else Text("Iniciar Atención")
                }
            } else {
                // Estado: En progreso o Finalizada
                val att = s.currentAttention!!
                val isFinished = att.status == "FINISHED" || att.status == "COMPLETED"
                
                Text(
                    if (isFinished) "Atención Completada" else "Atención en Progreso",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isFinished) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(Modifier.height(16.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        AttentionDetailRow(Icons.Default.Timer, "Inicio", att.startedAt)
                        if (isFinished) {
                            att.finishedAt?.let { AttentionDetailRow(Icons.Default.Timer, "Fin", it) }
                            att.durationMinutes?.let { AttentionDetailRow(Icons.Default.Timer, "Duración", "$it minutos") }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = s.observations,
                    onValueChange = { vm.setObservations(it) },
                    label = { Text("Bitácora de la sesión") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5,
                    enabled = !isFinished && !s.isLoading,
                    shape = MaterialTheme.shapes.medium,
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, null) }
                )

                Spacer(Modifier.height(32.dp))

                if (!isFinished) {
                    Button(
                        onClick = { vm.finishAttention() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = !s.isLoading
                    ) {
                        if (s.isLoading) CircularProgressIndicator(Modifier.size(24.dp))
                        else Text("Finalizar y Guardar")
                    }
                } else {
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
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Ir a Registrar Cobro")
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = { nav.popBackStack() },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Volver a Agenda")
                    }
                }
            }

            // Eliminados mensajes estáticos de error/éxito duplicados
        }
    }
}

@Composable
fun AttentionDetailRow(icon: ImageVector, label: String, value: String) {
    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.width(8.dp))
        Text("$label: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

