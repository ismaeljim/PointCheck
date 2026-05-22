package com.pointcheck.features.dashboard.presentation

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.features.dashboard.data.dto.DailyMetricDto
import com.pointcheck.features.dashboard.data.dto.WeeklySummaryDto

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
            TopAppBar(
                title = { Text(if (s.period == ReportPeriod.WEEKLY) "Reporte Semanal" else "Reporte Mensual") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
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
            
            if (totalRevenue != null) {
                Spacer(Modifier.height(16.dp))
                
                // Resumen
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Ingresos Totales", style = MaterialTheme.typography.labelLarge)
                            Text("$${totalRevenue}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.AutoMirrored.Filled.TrendingUp, null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(Modifier.height(24.dp))
                
                // Gráfico Visual Simple
                Text("Tendencia de Ingresos", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                val chartData = if (s.period == ReportPeriod.WEEKLY) {
                    s.report?.dailyBreakdown?.map { it.revenue.toFloat() } ?: emptyList()
                } else {
                    s.monthlyReport?.weeklyBreakdown?.map { it.revenue.toFloat() } ?: emptyList()
                }
                
                if (chartData.isNotEmpty()) {
                    SimpleBarChart(
                        data = chartData,
                        modifier = Modifier.fillMaxWidth().height(150.dp).padding(vertical = 8.dp)
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
            }
        }
    }
}

@Composable
fun DateSelector(label: String, subLabel: String, onPrevious: () -> Unit, onNext: () -> Unit, isLoading: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
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
fun SimpleBarChart(data: List<Float>, modifier: Modifier = Modifier) {
    val maxVal = (data.maxOrNull() ?: 0f).coerceAtLeast(1f)
    val barColor = MaterialTheme.colorScheme.primary
    
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val spaceBetweenBars = 12.dp.toPx()
            val barWidth = (size.width - (data.size - 1) * spaceBetweenBars) / data.size
            
            data.forEachIndexed { index, value ->
                val barHeight = (value / maxVal) * size.height
                drawRect(
                    color = barColor,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        x = index * (barWidth + spaceBetweenBars),
                        y = size.height - barHeight
                    ),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                )
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
