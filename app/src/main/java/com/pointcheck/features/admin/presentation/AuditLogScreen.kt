package com.pointcheck.features.admin.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointcheck.core.ui.components.PointCheckCard
import com.pointcheck.core.ui.components.PointCheckTopBar
import com.pointcheck.features.admin.data.dto.AuditLogDto

/**
 * Pantalla para la visualización de los registros de auditoría del sistema.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PointCheckTopBar(
                title = "PointCheck | Auditoría",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "El historial se conserva por 30 días para auditoría.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (state.isLoading && state.auditLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.auditLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay registros de auditoría", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.auditLogs) { log ->
                        AuditLogItem(log)
                    }

                    if (!state.isLastAuditPage) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (state.isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    OutlinedButton(
                                        onClick = { viewModel.loadAuditLogs(isNextPage = true) },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Cargar más registros")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuditLogItem(log: AuditLogDto) {
    var expanded by remember { mutableStateOf(false) }
    val actionStr = log.action ?: ""
    val actionColor = when (actionStr.uppercase()) {
        "ACCESO" -> Color(0xFF9C27B0)
        "CREAR", "CREAR_RESERVA" -> Color(0xFF4CAF50)
        "EDITAR", "CONFIRMAR_PAGO" -> Color(0xFFFF9800)
        "ELIMINAR", "EXPIRAR" -> Color(0xFFF44336)
        "ACTIVAR" -> Color(0xFF2196F3)
        "DESACTIVAR" -> Color(0xFF607D8B)
        else -> MaterialTheme.colorScheme.primary
    }

    PointCheckCard(
        title = actionStr.replace("_", " "),
        subtitle = log.timestamp?.take(16)?.replace("T", " ") ?: "Reciente",
        icon = when (actionStr.uppercase()) {
            "ELIMINAR" -> Icons.Default.Delete
            "CREAR", "CREAR_RESERVA" -> Icons.Default.AddCircle
            "EDITAR" -> Icons.Default.Edit
            "ACCESO" -> Icons.Default.Login
            "EXPIRAR" -> Icons.Default.TimerOff
            "CONFIRMAR_PAGO" -> Icons.Default.Payments
            else -> Icons.Default.History
        },
        iconColor = actionColor,
        badgeText = log.targetType,
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = log.performedByName ?: "Sistema",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = log.performedByEmail ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            val displayName = if (!log.targetName.isNullOrBlank()) {
                log.targetName
            } else if (!log.targetId.isNullOrBlank()) {
                "ID: ${log.targetId?.take(8)}..."
            } else null

            displayName?.let {
                Text(
                    text = "Objetivo: $it",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            log.details?.let {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified
                        ),
                        modifier = Modifier.padding(10.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (expanded) Int.MAX_VALUE else 3,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            if (expanded) {
                log.ipAddress?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Dirección IP: $it",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                
                if (!log.targetId.isNullOrBlank()) {
                    Text(
                        text = "ID Interno: ${log.targetId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
