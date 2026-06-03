package com.pointcheck.features.admin.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointcheck.core.presentation.components.AppTopBar
import com.pointcheck.features.admin.data.dto.AuditLogDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Logs de Auditoría",
                onBack = onBack
            )
        }
    ) { padding ->
        if (state.isLoading && state.auditLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                items(state.auditLogs) { log ->
                    AuditLogItem(log)
                }
            }
        }
    }
}

@Composable
fun AuditLogItem(log: AuditLogDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = log.action,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = log.timestamp.take(16).replace("T", " "),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Por: ${log.performedBy}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Destino: ${log.targetType} (${log.targetId})", style = MaterialTheme.typography.bodySmall)
            log.details?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Detalles: $it", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
