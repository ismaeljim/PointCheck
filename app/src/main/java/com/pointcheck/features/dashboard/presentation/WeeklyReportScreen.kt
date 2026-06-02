package com.pointcheck.features.dashboard.presentation

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.core.presentation.components.AppTopBar
import com.pointcheck.features.dashboard.data.dto.DailyMetricDto
import com.pointcheck.features.dashboard.data.dto.WeeklySummaryDto
import com.pointcheck.features.services.data.dto.ServiceResponseDto
import androidx.compose.foundation.lazy.LazyRow
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyReportScreen(nav: NavController, vm: WeeklyReportViewModel = viewModel()) {
    val s by vm.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle CSV Export
    LaunchedEffect(s.exportContent) {
        s.exportContent?.let { content ->
            val fileName = if (s.period == ReportPeriod.WEEKLY) "Reporte_Semanal.csv" else "Reporte_Mensual.csv"
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, content)
                putExtra(Intent.EXTRA_SUBJECT, fileName)
                type = "text/csv"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Exportar $fileName")
            context.startActivity(shareIntent)
            vm.clearExport()
        }
    }

    LaunchedEffect(s.error) {
        s.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = if (s.period == ReportPeriod.WEEKLY) "Reporte Semanal" else "Reporte Mensual",
                onBack = { nav.popBackStack() },
                actions = {
                    IconButton(onClick = { vm.exportReport() }, enabled = !s.isLoading) {
                        Icon(Icons.Default.Download, "Descargar")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Selector de Periodo
            TabRow(
                selectedTabIndex = if (s.period == ReportPeriod.WEEKLY) 0 else 1,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Tab(
                    selected = s.period == ReportPeriod.WEEKLY,
                    onClick = { vm.setPeriod(ReportPeriod.WEEKLY) },
                    text = { Text("Semanal") }
                )
                Tab(
                    selected = s.period == ReportPeriod.MONTHLY,
                    onClick = { vm.setPeriod(ReportPeriod.MONTHLY) },
                    text = { Text("Mensual") }
                )
            }

            // Filtro de Servicios
            if (s.services.isNotEmpty()) {
                ServiceFilterRow(
                    services = s.services,
                    selectedServiceId = s.selectedServiceId,
                    onServiceSelected = { vm.setServiceFilter(it) }
                )
                Spacer(Modifier.height(8.dp))
            }

            // Selector de Fecha (Semana o Mes)
            DateSelector(
                label = if (s.period == ReportPeriod.WEEKLY) "Semana ${s.report?.weekNumber ?: ""}" else "${s.monthlyReport?.monthName ?: ""} ${s.monthlyReport?.year ?: ""}",
                subLabel = if (s.period == ReportPeriod.WEEKLY) "Año ${s.report?.year ?: ""}" else "Reporte del Mes",
                onPrevious = { vm.changeOffset(1) },
                onNext = { vm.changeOffset(-1) },
                isLoading = s.isLoading
            )

            if (s.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))
            }

            val totalRevenue = if (s.period == ReportPeriod.WEEKLY) s.report?.totalRevenue else s.monthlyReport?.totalRevenue
            val prevRevenue = if (s.period == ReportPeriod.WEEKLY) s.report?.previousPeriodRevenue else s.monthlyReport?.previousPeriodRevenue
            val totalHours = if (s.period == ReportPeriod.WEEKLY) s.report?.totalHoursWorked else s.monthlyReport?.totalHoursWorked
            
            if (totalRevenue != null) {
                Spacer(Modifier.height(16.dp))
                
                // Resumen Principal
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Ingresos Totales", style = MaterialTheme.typography.labelLarge)
                                Text("$${totalRevenue}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            }
                            
                            val growth = if (prevRevenue != null && prevRevenue > 0) {
                                ((totalRevenue - prevRevenue) / prevRevenue) * 100
                            } else 0.0

                            TrendIndicator(growth)
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            val maxRevenueDay = s.report?.dailyBreakdown?.maxByOrNull { it.revenue }
                            val maxRevenueWeek = s.monthlyReport?.weeklyBreakdown?.maxByOrNull { it.revenue }

                            StatMiniItem(
                                icon = Icons.Default.CalendarToday,
                                label = "Día más fuerte",
                                value = maxRevenueDay?.dayOfWeek ?: maxRevenueWeek?.dateRange ?: "N/A"
                            )
                            StatMiniItem(
                                icon = Icons.Default.AccessTime,
                                label = "Horas totales",
                                value = String.format(Locale.getDefault(), "%.1f h", totalHours ?: 0.0)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                
                // Gráfico de Tendencia (Line Chart)
                Text("Tendencia de Ingresos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                val chartData = if (s.period == ReportPeriod.WEEKLY) {
                    s.report?.dailyBreakdown?.map { it.revenue.toFloat() } ?: emptyList()
                } else {
                    s.monthlyReport?.weeklyBreakdown?.map { it.revenue.toFloat() } ?: emptyList()
                }
                
                if (chartData.isNotEmpty()) {
                    TrendLineChart(
                        data = chartData,
                        modifier = Modifier.fillMaxWidth().height(180.dp).padding(vertical = 8.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))
                Text(if (s.period == ReportPeriod.WEEKLY) "Desglose Diario" else "Desglose Semanal", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (s.period == ReportPeriod.WEEKLY) {
                        s.report?.dailyBreakdown?.let { items ->
                            items(items) { day -> DailyMetricItem(day) }
                        }
                    } else {
                        s.monthlyReport?.weeklyBreakdown?.let { items ->
                            items(items) { week -> WeeklySummaryItem(week) }
                        }
                    }
                }
            } else if (s.summary != null) {
                val report = s.summary!!
                Spacer(Modifier.height(24.dp))
                Text("Estadísticas de Atención", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatRow("Total Reservas", report.totalReservations.toString())
                    StatRow("Atenciones Completadas", report.completedAttentions.toString())
                    StatRow("Promedio Atención (min)", String.format(Locale.getDefault(), "%.1f", report.averageAttentionMinutes))
                    StatRow("Cobros Realizados", report.paidBillingCount.toString())
                    StatRow("Cobros Pendientes", report.pendingBillingCount.toString())
                    StatRow("Pendiente de Cobro", "$${report.pendingAmount}")
                }
            }
        }
    }
}

@Composable
fun TrendIndicator(growth: Double) {
    val isPositive = growth >= 0
    val color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
    val icon = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown
    
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(4.dp))
        Text(
            text = String.format(Locale.getDefault(), "%+.1f%%", growth),
            color = color,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatMiniItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TrendLineChart(data: List<Float>, modifier: Modifier = Modifier) {
    val maxVal = (data.maxOrNull() ?: 0f).coerceAtLeast(1f)
    val color = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)

    Box(
        modifier = modifier
            .background(surfaceColor, MaterialTheme.shapes.medium)
            .padding(top = 16.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val spacing = width / (data.size - 1).coerceAtLeast(1)

            val points = data.mapIndexed { index, value ->
                val x = index * spacing
                val y = height - (value / maxVal) * height
                androidx.compose.ui.geometry.Offset(x, y)
            }

            val path = Path().apply {
                if (points.isNotEmpty()) {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }
            }

            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw points
            points.forEach { point ->
                drawCircle(color = color, radius = 4.dp.toPx(), center = point)
                drawCircle(color = Color.White, radius = 2.dp.toPx(), center = point)
            }
        }
    }
}

@Composable
fun ServiceFilterRow(
    services: List<ServiceResponseDto>,
    selectedServiceId: String?,
    onServiceSelected: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        item {
            FilterChip(
                selected = selectedServiceId == null,
                onClick = { onServiceSelected(null) },
                label = { Text("Todos") }
            )
        }
        items(services) { service ->
            FilterChip(
                selected = selectedServiceId == service.id,
                onClick = { onServiceSelected(service.id) },
                label = { Text(service.name) }
            )
        }
    }
}

@Composable
fun DateSelector(label: String, subLabel: String, onPrevious: () -> Unit, onNext: () -> Unit, isLoading: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious, enabled = !isLoading) {
            Icon(Icons.Default.ChevronLeft, "Anterior")
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subLabel, style = MaterialTheme.typography.labelSmall)
        }

        IconButton(onClick = onNext, enabled = !isLoading) {
            Icon(Icons.Default.ChevronRight, "Siguiente")
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}


@Composable
fun DailyMetricItem(day: DailyMetricDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(day.dayOfWeek, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(day.date, style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$${day.revenue}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("${day.reservationsCount} citas", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun WeeklySummaryItem(week: WeeklySummaryDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Semana ${week.weekNumber}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(week.dateRange, style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$${week.revenue}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("${week.reservationsCount} citas", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
