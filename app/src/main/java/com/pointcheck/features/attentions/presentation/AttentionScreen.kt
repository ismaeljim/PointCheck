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
import com.pointcheck.core.presentation.components.AppButton
import com.pointcheck.core.presentation.components.AppOutlinedButton
import com.pointcheck.core.presentation.components.AppTextField
import com.pointcheck.core.presentation.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttentionScreen(
    nav: NavController,
    reservationId: String,
    clientId: String,
    specialistId: String,
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
            AppTopBar(
                title = "Módulo de Atención",
                onBack = { nav.popBackStack() }
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

                AppTextField(
                    value = s.observations,
                    onValueChange = { vm.setObservations(it) },
                    label = "Observaciones iniciales",
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    enabled = !s.isLoading
                )
                
                Spacer(Modifier.height(24.dp))
                
                AppButton(
                    text = "Iniciar Atención",
                    onClick = { vm.startAttention(reservationId) },
                    enabled = !s.isLoading,
                    isLoading = s.isLoading
                )
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

                AppTextField(
                    value = s.observations,
                    onValueChange = { vm.setObservations(it) },
                    label = "Bitácora de la sesión",
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5,
                    enabled = !isFinished && !s.isLoading,
                    leadingIcon = Icons.AutoMirrored.Filled.Notes
                )

                Spacer(Modifier.height(32.dp))

                if (!isFinished) {
                    AppButton(
                        text = "Finalizar y Guardar",
                        onClick = { vm.finishAttention() },
                        enabled = !s.isLoading,
                        isLoading = s.isLoading
                    )
                } else {
                    AppButton(
                        text = "Ir a Registrar Cobro",
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
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    AppOutlinedButton(
                        text = "Volver a Agenda",
                        onClick = { nav.popBackStack() }
                    )
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

