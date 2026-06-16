package com.pointcheck.features.dashboard.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.pointcheck.core.navigation.Screen
import com.pointcheck.core.presentation.components.AppButton
import com.pointcheck.core.presentation.components.AppTextField
import com.pointcheck.core.ui.components.PCCard
import com.pointcheck.core.ui.components.PCOutlinedButton
import com.pointcheck.features.dashboard.data.dto.DashboardMetricsDto

@Composable
fun AdminDashboard(
    m: DashboardMetricsDto,
    nav: NavController,
    onShowUsers: () -> Unit,
    onShowAudit: () -> Unit,
    onShowFinance: () -> Unit,
    onShowSettings: () -> Unit
) {
    var showAppointmentsInfo by remember { mutableStateOf(false) }

    if (showAppointmentsInfo) {
        AlertDialog(
            onDismissRequest = { showAppointmentsInfo = false },
            confirmButton = {
                TextButton(onClick = { showAppointmentsInfo = false }) {
                    Text("Entendido")
                }
            },
            title = { Text("Módulo en Desarrollo") },
            text = { Text("El sistema de supervisión global de agendas para administradores está siendo implementado y estará disponible en la próxima actualización.") },
            icon = { Icon(Icons.Default.Info, null) }
        )
    }

    Column(Modifier.fillMaxWidth()) {
        Text(
            text = "Panel de Control Maestro",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Supervisión global de la plataforma",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        
        // Fila 1: Usuarios e Ingresos
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                label = "Usuarios Totales",
                value = (m.totalUsers ?: 0).toString(),
                icon = Icons.Default.People,
                modifier = Modifier.weight(1f),
                onClick = onShowUsers
            )
            MetricCard(
                label = "Ingresos Netos",
                value = "$${String.format("%,.0f", m.totalRevenue ?: 0.0)}",
                icon = Icons.Default.MonetizationOn,
                modifier = Modifier.weight(1f),
                onClick = onShowFinance
            )
        }
        
        Spacer(Modifier.height(12.dp))
        
        // Fila 2: Operaciones y Seguridad
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                label = "Citas Hoy",
                value = (m.appointmentsToday ?: 0).toString(),
                icon = Icons.AutoMirrored.Filled.EventNote,
                modifier = Modifier.weight(1f),
                onClick = { nav.navigate(Screen.Scheduled.createRoute("today")) }
            )
            MetricCard(
                label = "Alertas Sist.",
                value = (m.systemAlerts ?: 0).toString(),
                icon = Icons.Default.Security,
                modifier = Modifier.weight(1f),
                onClick = onShowAudit
            )
        }

        Spacer(Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Balances Financieros", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                FinancialRow("Recaudación Real", "$${String.format("%,.0f", m.totalRevenue ?: 0.0)}", Color(0xFF4CAF50))
                FinancialRow("Pendiente de Cobro", "$${String.format("%,.0f", m.pendingRevenue ?: 0.0)}", MaterialTheme.colorScheme.error)
                FinancialRow("Especialistas Activos", (m.activeSpecialists ?: 0).toString())
                
                // Gráfico de Ingresos (Admin)
                val series = m.revenueSeries ?: emptyList()
                if (series.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text("Ingresos Últimos 7 Días", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    val model = entryModelOf(*series.map { (it.value ?: 0.0).toFloat() }.toTypedArray())
                    Chart(
                        chart = lineChart(),
                        model = model,
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis(),
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))
                PCOutlinedButton(
                    text = "Gestionar Parámetros", 
                    icon = Icons.Default.Settings, 
                    onClick = onShowSettings,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Accesos Directos Administrativos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        
        AdminActionButton("Auditoría de Usuarios", Icons.Default.SupervisorAccount, "Ver estados y roles", onShowUsers)
        AdminActionButton("Historial de Cambios", Icons.Default.HistoryEdu, "Log de acciones administrativas", onShowAudit)
        AdminActionButton("Configuración de Red", Icons.Default.Settings, "Parámetros del sistema", onShowSettings)
    }
}

@Composable
fun AdminActionButton(title: String, icon: ImageVector, subtitle: String, onClick: () -> Unit) {
    PCCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogsBottomSheet(onDismiss: () -> Unit) {
    val vm: DashboardViewModel = viewModel()
    val s by vm.state.collectAsState()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                "Historial de Auditoría",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold
            )

            if (s.auditLogs.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No hay registros de auditoría", color = MaterialTheme.colorScheme.secondary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    s.auditLogs.forEach { log ->
                        AuditLogItem(log)
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AuditLogItem(log: com.pointcheck.features.admin.data.dto.AuditLogDto) {
    var expanded by remember { mutableStateOf(false) }
    
    // Blindaje de acción
    val actionText = (log.action ?: "LOG").replace("_", " ")
    
    val icon = when (log.action) {
        "ACTIVAR" -> Icons.Default.PersonAdd
        "DESACTIVAR" -> Icons.Default.PersonRemove
        "EDITAR" -> Icons.Default.SettingsSuggest
        "CREAR" -> Icons.Default.AddCircle
        else -> Icons.Default.History
    }

    Surface(
        onClick = { expanded = !expanded },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                icon, 
                null, 
                tint = if (log.action == "DESACTIVAR") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, 
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = actionText, 
                        fontWeight = FontWeight.ExtraBold, 
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(8.dp))
                    if (expanded) {
                        Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                            Text(log.targetType ?: "General", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                
                Text(
                    text = log.details ?: "Sin detalles adicionales", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                Spacer(Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AdminPanelSettings, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(4.dp))
                    
                    // SEGURIDAD ABSOLUTA: Evitar isBlank() y llamadas directas sobre nulos de Gson
                    val emailSeguro = log.performedByEmail ?: ""
                    val adminAlias = if (emailSeguro.contains("@")) {
                        emailSeguro.substringBefore("@")
                    } else if (emailSeguro.isNotEmpty()) {
                        emailSeguro
                    } else {
                        val name = log.performedByName ?: ""
                        if (name.isNotEmpty()) name else "Sistema"
                    }

                    Text(
                        "Admin: $adminAlias",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(4.dp))
                    
                    val formattedTime = try {
                        val ts = log.timestamp ?: ""
                        if (ts.contains("T")) {
                            val parts = ts.split("T")
                            val date = parts[0]
                            val time = parts[1].substringBefore(".")
                            "$date $time"
                        } else if (ts.isNotEmpty()) {
                            ts.substringBefore(".")
                        } else {
                            "Reciente"
                        }
                    } catch (e: Exception) {
                        "Reciente"
                    }

                    Text(
                        formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                if (expanded) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "ID Objetivo: ${log.targetId ?: "N/A"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersBottomSheet(onDismiss: () -> Unit) {
    val vm: DashboardViewModel = viewModel()
    val s by vm.state.collectAsState()
    
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            Text("Gestión de Usuarios", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
            
            if (s.adminUsers.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No se encontraron usuarios", color = MaterialTheme.colorScheme.secondary)
                }
            } else {
                s.adminUsers.forEach { user ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(user.name ?: "Usuario sin nombre", fontWeight = FontWeight.Bold)
                            Text("${user.role ?: "SIN_ROL"} • ${user.email ?: "Sin email"}", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = user.active ?: false,
                            onCheckedChange = { vm.toggleUserStatus(user.id ?: "") }
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFinanceBottomSheet(onDismiss: () -> Unit) {
    val vm: DashboardViewModel = viewModel()
    val s by vm.state.collectAsState()
    val report = s.financialReport

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(bottom = 40.dp).padding(horizontal = 20.dp)) {
            Text("Balance Global", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            
            if (report == null) {
                Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (s.error != null) {
                            Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                            Text("Error al cargar datos", color = MaterialTheme.colorScheme.error)
                            Button(onClick = { vm.loadAdminData() }, modifier = Modifier.padding(top = 8.dp)) {
                                Text("Reintentar")
                            }
                        } else {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(8.dp))
                            Text("Cargando reporte financiero...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            } else {
                val totalRevenue = report["totalRevenue"]?.toString() ?: "0"
                val pendingRevenue = report["pendingRevenue"]?.toString() ?: "0"
                val totalTransactions = report["totalTransactions"]?.toString() ?: "0"
                val paidTransactions = report["paidTransactions"]?.toString() ?: "0"

                MetricCard("Ingresos Totales", "$${String.format("%,.0f", totalRevenue.toDoubleOrNull() ?: 0.0)}", Icons.Default.MonetizationOn, Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                FinancialRow("Transacciones", totalTransactions)
                FinancialRow("Cobros Pendientes", "$${String.format("%,.0f", pendingRevenue.toDoubleOrNull() ?: 0.0)}", MaterialTheme.colorScheme.error)
                FinancialRow("Pagos Realizados", paidTransactions, Color(0xFF4CAF50))
                
                Spacer(Modifier.height(32.dp))
                AppButton("Descargar Reporte PDF", onClick = { /* Simular descarga */ })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsBottomSheet(onDismiss: () -> Unit) {
    val vm: DashboardViewModel = viewModel()
    val s by vm.state.collectAsState()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Configuración Global",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            if (s.adminSettings.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                s.adminSettings.forEach { setting ->
                    SettingItem(setting) { newValue ->
                        vm.updateSetting(setting.key ?: "", newValue)
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                }
            }

            Spacer(Modifier.height(24.dp))
            AppButton("Cerrar", onClick = onDismiss)
        }
    }
}

@Composable
fun SettingItem(
    setting: com.pointcheck.features.dashboard.data.dto.GlobalSettingDto,
    onUpdate: (String) -> Unit
) {
    val keySegura = setting.key ?: return
    val valueSeguro = setting.value ?: ""

    Column(Modifier.fillMaxWidth()) {
        Text(
            keySegura.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        val desc = setting.description ?: ""
        if (desc.isNotEmpty()) {
            Text(desc, style = MaterialTheme.typography.bodySmall)
        }
        
        Spacer(Modifier.height(8.dp))
        
        var isSaving by remember { mutableStateOf(false) }

        if (valueSeguro == "true" || valueSeguro == "false") {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (valueSeguro == "true") "Activado" else "Desactivado")
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Switch(
                        checked = valueSeguro == "true",
                        onCheckedChange = { 
                            isSaving = true
                            onUpdate(it.toString()) 
                        }
                    )
                }
            }
        } else {
            var textValue by remember { mutableStateOf(valueSeguro) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    label = "",
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                )
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(8.dp))
                } else {
                    IconButton(onClick = { 
                        isSaving = true
                        onUpdate(textValue) 
                    }) {
                        Icon(Icons.Default.Save, "Guardar", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
