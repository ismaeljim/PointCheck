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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.core.navigation.Screen
import com.pointcheck.core.ui.components.PointCheckButton
import com.pointcheck.core.ui.components.PointCheckOutlinedButton
import com.pointcheck.core.ui.components.PointCheckTextField
import com.pointcheck.core.ui.components.PointCheckTopBar

/**
 * Screen for managing a service attention session.
 *
 * This screen allows specialists to:
 * - Start a new attention for a reservation.
 * - Log observations during the session.
 * - Finalize the attention and calculate duration.
 * - Navigate to the billing module once finished.
 *
 * @param nav Navigation controller.
 * @param reservationId The ID of the reservation being attended.
 * @param vm ViewModel managing the attention state and lifecycle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttentionScreen(
    nav: NavController,
    reservationId: String,
    vm: AttentionViewModel = viewModel()
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Sprint 3: Suscripción al canal de errores del ViewModel
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
        vm.loadAttentionByReservation(reservationId)
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
            PointCheckTopBar(
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
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Información de Reserva #$reservationId",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    s.currentAttention?.let { att ->
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Cliente: ${att.client.name}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "RUT: ${att.client.rut}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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

                PointCheckTextField(
                    value = s.observations,
                    onValueChange = { vm.setObservations(it) },
                    label = "Observaciones iniciales",
                    placeholder = "Ingrese observaciones previas al servicio...",
                    leadingIcon = Icons.AutoMirrored.Filled.Notes,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    enabled = !s.isLoading
                )
                
                Spacer(Modifier.height(24.dp))
                
                PointCheckButton(
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

                PointCheckTextField(
                    value = s.observations,
                    onValueChange = { vm.setObservations(it) },
                    label = "Bitácora de la sesión",
                    placeholder = "Registro de avances y novedades...",
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5,
                    enabled = !isFinished && !s.isLoading,
                    leadingIcon = Icons.AutoMirrored.Filled.Notes
                )

                Spacer(Modifier.height(32.dp))

                if (!isFinished) {
                    PointCheckButton(
                        text = "Finalizar y Guardar",
                        onClick = { vm.finishAttention() },
                        enabled = !s.isLoading,
                        isLoading = s.isLoading
                    )
                } else {
                    PointCheckButton(
                        text = "Ir a Registrar Cobro",
                        onClick = { 
                            nav.navigate(
                                Screen.Billing.createRoute(
                                    reservationId,
                                    att.id
                                )
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    PointCheckOutlinedButton(
                        text = "Volver a Agenda",
                        onClick = { nav.popBackStack() }
                    )
                }
            }

            // Eliminados mensajes estáticos de error/éxito duplicados
        }
    }
}

/**
 * A simple row to display a specific detail of the attention session.
 *
 * @param icon Icon representing the data type.
 * @param label Label for the detail.
 * @param value The actual data value.
 */
@Composable
fun AttentionDetailRow(icon: ImageVector, label: String, value: String) {
    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.width(8.dp))
        Text("$label: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

