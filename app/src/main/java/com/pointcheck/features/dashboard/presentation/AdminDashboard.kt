package com.pointcheck.features.dashboard.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.pointcheck.core.ui.components.*
import com.pointcheck.features.dashboard.data.dto.DashboardMetricsDto
import java.util.Locale

@Composable
fun AdminDashboard(
    m: DashboardMetricsDto,
    chartType: AdminChartType = AdminChartType.LINE,
    onChartTypeChange: (AdminChartType) -> Unit = {},
    onShowUsers: () -> Unit,
    onShowAudit: () -> Unit,
    onShowFinance: () -> Unit,
    onShowSettings: () -> Unit
) {
    var showInDevAlert by remember { mutableStateOf(false) }

    if (showInDevAlert) {
        AlertDialog(
            onDismissRequest = { showInDevAlert = false },
            confirmButton = {
                TextButton(onClick = { showInDevAlert = false }) {
                    Text("Entendido")
                }
            },
            title = { Text("Módulo en Desarrollo") },
            text = { Text("El sistema de supervisión global de agendas para administradores está siendo implementado y estará disponible en la próxima actualización.") },
            icon = { Icon(Icons.Default.Info, null) }
        )
    }
                                                                                                                                                                                                                                                            
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 8.dp)
    ) {
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
        Spacer(Modifier.height(16.dp))
        
        // Fila 1: Usuarios e Ingresos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricCard(
                label = "Usuarios Totales",
                value = (m.totalUsers ?: 0).toString(),
                icon = Icons.Default.People,
                modifier = Modifier.weight(1f),
                onClick = onShowUsers
            )
            MetricCard(
                label = "Ingresos Netos",
                value = "$${String.format(Locale.getDefault(), "%,.0f", m.totalRevenue ?: 0.0)}",
                icon = Icons.Default.MonetizationOn,
                modifier = Modifier.weight(1f),
                onClick = onShowFinance
            )
        }
        
        Spacer(Modifier.height(16.dp))

        // Fila 2: Operaciones y Seguridad
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricCard(
                label = "Citas Hoy",
                value = (m.appointmentsToday ?: 0).toString(),
                icon = Icons.AutoMirrored.Filled.EventNote,
                modifier = Modifier.weight(1f),
                onClick = { showInDevAlert = true }
            )
            MetricCard(
                label = "Alertas Sist.",
                value = (m.systemAlerts ?: 0).toString(),
                icon = Icons.Default.Security,
                modifier = Modifier.weight(1f),
                onClick = onShowAudit
            )
        }

        Spacer(Modifier.height(16.dp))

        PointCheckCard(
            title = "Balances Financieros",
            subtitle = "Resumen de recaudación y estado de cobros",
            icon = Icons.Default.Assessment
        ) {
            Column(Modifier.fillMaxWidth()) {
                FinancialRow("Recaudación Real", "$${String.format(Locale.getDefault(), "%,.0f", m.totalRevenue ?: 0.0)}", Color(0xFF4CAF50))
                FinancialRow("Pendiente de Cobro", "$${String.format(Locale.getDefault(), "%,.0f", m.pendingRevenue ?: 0.0)}", MaterialTheme.colorScheme.error)
                FinancialRow("Especialistas Activos", (m.activeSpecialists ?: 0).toString())
                
                // Gráfico de Ingresos (Admin) con Selector Dual
                val series = m.revenueSeries ?: emptyList()
                if (series.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = chartType == AdminChartType.LINE,
                            onClick = { onChartTypeChange(AdminChartType.LINE) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            icon = { SegmentedButtonDefaults.Icon(active = chartType == AdminChartType.LINE) { Icon(Icons.AutoMirrored.Filled.ShowChart, null) } }
                        ) {
                            Text("Tendencia", style = MaterialTheme.typography.labelSmall)
                        }
                        SegmentedButton(
                            selected = chartType == AdminChartType.BAR,
                            onClick = { onChartTypeChange(AdminChartType.BAR) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            icon = { SegmentedButtonDefaults.Icon(active = chartType == AdminChartType.BAR) { Icon(Icons.Default.BarChart, null) } }
                        ) {
                            Text("Comparativa", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = if (chartType == AdminChartType.LINE) "Ingresos Últimos 7 Días (Tendencia)" else "Ingresos Diarios (Acumulado)",
                        style = MaterialTheme.typography.labelMedium, 
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    
                    val model = entryModelOf(*series.map { (it.value ?: 0.0).toFloat() }.toTypedArray())
                    
                    // Renderizado dinámico según ChartType
                    if (chartType == AdminChartType.LINE) {
                        Chart(
                            chart = com.patrykandpatrick.vico.compose.chart.line.lineChart(),
                            model = model,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                            modifier = Modifier.fillMaxWidth().height(140.dp)
                        )
                    } else {
                        Chart(
                            chart = com.patrykandpatrick.vico.compose.chart.column.columnChart(),
                            model = model,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                            modifier = Modifier.fillMaxWidth().height(140.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                PointCheckButton(
                    text = "Gestionar Parámetros", 
                    icon = Icons.Default.Settings, 
                    onClick = onShowSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Accesos Directos Administrativos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminActionButton("Auditoría de Usuarios", Icons.Default.SupervisorAccount, "Ver estados y roles", onShowUsers)
            AdminActionButton("Historial de Cambios", Icons.Default.HistoryEdu, "Log de acciones administrativas", onShowAudit)
            AdminActionButton("Configuración de Red", Icons.Default.Settings, "Parámetros del sistema", onShowSettings)
        }
        
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun AdminActionButton(title: String, icon: ImageVector, subtitle: String, onClick: () -> Unit) {
    PointCheckCard(
        title = title,
        subtitle = subtitle,
        icon = icon,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Gestionar",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogsBottomSheet(onDismiss: () -> Unit) {
    val vm: DashboardViewModel = viewModel()
    val s by vm.state.collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "PointCheck | Auditoría",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { vm.loadDashboard(silent = true) }) {
                    Icon(Icons.Default.Refresh, "Refrescar")
                }
            }

            if (s is DashboardUiState.Loading) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (s is DashboardUiState.Success) {
                val successState = s as DashboardUiState.Success
                if (successState.auditLogs.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("No hay registros de auditoría", color = MaterialTheme.colorScheme.secondary)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        successState.auditLogs.take(15).forEach { log ->
                            AuditLogItem(log)
                        }
                        
                        TextButton(
                            onClick = { 
                                onDismiss()
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
                        ) {
                            Text("Ver historial completo")
                        }
                    }
                }
            } else if (s is DashboardUiState.Error) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("Error al cargar auditoría", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AuditLogItem(log: com.pointcheck.features.admin.data.dto.AuditLogDto) {
    var expanded by remember { mutableStateOf(false) }
    val actionText = (log.action ?: "LOG").replace("_", " ")
    
    val icon = when (log.action) {
        "ACTIVAR" -> Icons.Default.PersonAdd
        "DESACTIVAR" -> Icons.Default.PersonRemove
        "EDITAR" -> Icons.Default.SettingsSuggest
        "CREAR" -> Icons.Default.AddCircle
        "ACCESO" -> Icons.AutoMirrored.Filled.Login
        "ELIMINAR" -> Icons.Default.DeleteForever
        else -> Icons.Default.History
    }

    val actionColor = when (log.action) {
        "DESACTIVAR", "ELIMINAR" -> MaterialTheme.colorScheme.error
        "CREAR", "ACTIVAR" -> Color(0xFF4CAF50)
        "EDITAR" -> Color(0xFFFF9800)
        else -> MaterialTheme.colorScheme.primary
    }

    PointCheckCard(
        title = actionText,
        subtitle = log.timestamp ?: "Reciente",
        icon = icon,
        iconColor = actionColor,
        badgeText = log.targetType,
        onClick = { expanded = !expanded }
    ) {
        Column(Modifier.fillMaxWidth()) {
            val displayName = if (!log.targetName.isNullOrBlank()) {
                log.targetName
            } else if (!log.targetId.isNullOrBlank()) {
                "ID: ${log.targetId.take(8)}..."
            } else null

            if (displayName != null) {
                Text(
                    text = "Objetivo: $displayName",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
            }

            log.details?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            
            if (expanded) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AdminPanelSettings, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(4.dp))
                    
                    val adminAlias = log.performedByName ?: log.performedByEmail?.substringBefore("@") ?: "Sistema"

                    Text(
                        "Ejecutado por: $adminAlias",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                if (!log.targetId.isNullOrBlank()) {
                    Text(
                        "ID Objetivo: ${log.targetId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 4.dp)
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
    val s by vm.state.collectAsStateWithLifecycle()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            Text(
                "PointCheck | Gestión de Usuarios", 
                style = MaterialTheme.typography.titleLarge, 
                modifier = Modifier.padding(16.dp), 
                fontWeight = FontWeight.Bold
            )
            
            if (s is DashboardUiState.Success) {
                val successState = s as DashboardUiState.Success
                if (successState.adminUsers.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("No se encontraron usuarios", color = MaterialTheme.colorScheme.secondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(successState.adminUsers) { user ->
                            PointCheckCard(
                                title = user.name ?: "Usuario sin nombre",
                                subtitle = "${user.role ?: "SIN_ROL"} • ${user.email ?: "Sin email"}",
                                icon = if (user.role == "ADMIN") Icons.Default.AdminPanelSettings else Icons.Default.Person
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        if (user.active == true) "Cuenta Activa" else "Cuenta Inactiva",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (user.active == true) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                                    )
                                    
                                    val isActive = user.active ?: false
                                    var isUpdating by remember(user.id, isActive) { mutableStateOf(false) }

                                    if (isUpdating) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    } else {
                                        Switch(
                                            checked = isActive,
                                            onCheckedChange = { 
                                                isUpdating = true
                                                vm.toggleUserStatus(user.id ?: "")
                                            },
                                            modifier = Modifier.scale(0.8f)
                                        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFinanceBottomSheet(onDismiss: () -> Unit) {
    val vm: DashboardViewModel = viewModel()
    val s by vm.state.collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(Modifier.fillMaxWidth().padding(bottom = 40.dp).padding(horizontal = 20.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PointCheck | Finanzas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = { vm.loadDashboard(silent = true) }) {
                    Icon(Icons.Default.Refresh, "Refrescar")
                }
            }
            Spacer(Modifier.height(24.dp))
            
            if (s is DashboardUiState.Loading) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (s is DashboardUiState.Success) {
                val report = (s as DashboardUiState.Success).financialReport
                if (report == null) {
                    Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        Text("No hay datos financieros disponibles", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    val totalRevenue = report["totalRevenue"]?.toString() ?: "0"
                    val pendingRevenue = report["pendingRevenue"]?.toString() ?: "0"
                    val totalTransactions = report["totalTransactions"]?.toString() ?: "0"
                    val paidTransactions = report["paidTransactions"]?.toString() ?: "0"

                    MetricCard(
                        label = "Ingresos Totales", 
                        value = "$${String.format(Locale.getDefault(), "%,.0f", totalRevenue.toDoubleOrNull() ?: 0.0)}", 
                        icon = Icons.Default.MonetizationOn, 
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    Text("Resumen de Operaciones", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    
                    FinancialRow("Total Transacciones", totalTransactions)
                    FinancialRow("Cobros Pendientes", "$${String.format(Locale.getDefault(), "%,.0f", pendingRevenue.toDoubleOrNull() ?: 0.0)}", MaterialTheme.colorScheme.error)
                    FinancialRow("Pagos Realizados", paidTransactions, Color(0xFF4CAF50))
                    
                    Spacer(Modifier.height(32.dp))
                    PointCheckButton("Descargar Reporte PDF", onClick = { /* Simular descarga */ }, modifier = Modifier.fillMaxWidth())
                }
            } else if (s is DashboardUiState.Error) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Error al cargar finanzas", color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = { vm.loadDashboard() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsBottomSheet(onDismiss: () -> Unit) {
    val vm: DashboardViewModel = viewModel()
    val s by vm.state.collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            Modifier.fillMaxWidth().padding(bottom = 40.dp).padding(horizontal = 20.dp).verticalScroll(rememberScrollState())
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PointCheck | Configuración", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = { vm.loadDashboard(silent = true) }) {
                    Icon(Icons.Default.Refresh, "Refrescar")
                }
            }
            Spacer(Modifier.height(16.dp))

            when (val currentState = s) {
                is DashboardUiState.Loading -> {
                    Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is DashboardUiState.Success -> {
                    val settings = currentState.adminSettings
                    if (settings.isEmpty()) {
                        Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                            Text("No hay configuraciones disponibles", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        settings.forEach { setting ->
                            SettingItem(setting) { newValue ->
                                vm.updateSetting(setting.key ?: "", newValue)
                            }
                            HorizontalDivider(Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                        }
                    }
                }
                is DashboardUiState.Error -> {
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error)
                        Text(
                            text = "Error al cargar configuraciones",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = { vm.loadDashboard() }) {
                            Text("Reintentar")
                        }
                    }
                }
                else -> {}
            }

            Spacer(Modifier.height(24.dp))
            PointCheckButton("Cerrar", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
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
    var isSaving by remember { mutableStateOf(false) }
    
    // Sincronización de estado para evitar bloqueos si el backend no responde a tiempo
    LaunchedEffect(valueSeguro) { isSaving = false }

    Column(Modifier.fillMaxWidth()) {
        Text(
            text = try {
                keySegura.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
            } catch (e: Exception) {
                keySegura
            },
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(setting.description ?: "Sin descripción", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))

        // Manejo Seguro de Tipos: Fallback a TextField si el parseo falla o es ambiguo
        val isBoolean = try {
            valueSeguro.trim().lowercase().let { it == "true" || it == "false" }
        } catch (_: Exception) {
            false
        }

        if (isBoolean) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (valueSeguro.trim().lowercase() == "true") "Activado" else "Desactivado",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Switch(
                        checked = valueSeguro.trim().lowercase() == "true",
                        onCheckedChange = { 
                            isSaving = true
                            onUpdate(it.toString()) 
                        }
                    )
                }
            }
        } else {
            var textValue by remember(valueSeguro) { mutableStateOf(valueSeguro) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                PointCheckTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    label = "Valor del parámetro",
                    placeholder = "Ingrese el nuevo valor",
                    leadingIcon = Icons.Default.Edit,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                )
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(8.dp), strokeWidth = 2.dp)
                } else {
                    IconButton(
                        onClick = { 
                            isSaving = true
                            onUpdate(textValue) 
                        },
                        enabled = textValue != valueSeguro // Solo permitir guardar si hubo cambios
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save, 
                            contentDescription = "Guardar", 
                            tint = if (textValue != valueSeguro) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}
