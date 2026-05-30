package com.pointcheck.features.dashboard.presentation

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.core.presentation.components.HeaderIcon
import com.pointcheck.core.presentation.components.SectionHeader
import com.pointcheck.features.dashboard.data.dto.DailyMetricDto
import com.pointcheck.features.dashboard.data.dto.WeeklySummaryDto
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyReportScreen(nav: NavController, vm: WeeklyReportViewModel = viewModel()) {
    val s by vm.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- HEADER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HeaderIcon(Icons.AutoMirrored.Filled.ArrowBack) { nav.popBackStack() }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "Análisis de Negocio",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                HeaderIcon(Icons.Default.Download) { vm.exportReport() }
            }
        }

        // --- CONTENT ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-20).dp)
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(Modifier.height(24.dp))
                    
                    // Period Selector
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TabRow(
                            selectedTabIndex = if (s.period == ReportPeriod.WEEKLY) 0 else 1,
                            containerColor = Color.Transparent,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            divider = {}
                        ) {
                            Tab(
                                selected = s.period == ReportPeriod.WEEKLY,
                                onClick = { vm.setPeriod(ReportPeriod.WEEKLY) },
                                text = { Text("Semanal", fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = s.period == ReportPeriod.MONTHLY,
                                onClick = { vm.setPeriod(ReportPeriod.MONTHLY) },
                                text = { Text("Mensual", fontWeight = FontWeight.Bold) }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    DateSelector(
                        label = if (s.period == ReportPeriod.WEEKLY) "Semana ${s.report?.weekNumber ?: ""}" else "${s.monthlyReport?.monthName ?: ""} ${s.monthlyReport?.year ?: ""}",
                        subLabel = if (s.period == ReportPeriod.WEEKLY) "Año ${s.report?.year ?: ""}" else "Reporte Mensual",
                        onPrevious = { vm.changeOffset(1) },
                        onNext = { vm.changeOffset(-1) },
                        isLoading = s.isLoading
                    )

                    if (s.isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), color = MaterialTheme.colorScheme.secondary)
                    }

                    val totalRevenue = if (s.period == ReportPeriod.WEEKLY) s.report?.totalRevenue else s.monthlyReport?.totalRevenue
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        if (totalRevenue != null) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Ingresos totales", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f))
                                            Text("$${totalRevenue}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color.White)
                                        }
                                        Icon(Icons.AutoMirrored.Filled.TrendingUp, null, modifier = Modifier.size(48.dp), tint = Color.White.copy(alpha = 0.3f))
                                    }
                                }
                            }

                            item {
                                SectionHeader("Rendimiento")
                                val chartData = if (s.period == ReportPeriod.WEEKLY) {
                                    s.report?.dailyBreakdown?.map { it.revenue.toFloat() } ?: emptyList()
                                } else {
                                    s.monthlyReport?.weeklyBreakdown?.map { it.revenue.toFloat() } ?: emptyList()
                                }
                                
                                if (chartData.isNotEmpty()) {
                                    SimpleBarChart(
                                        data = chartData,
                                        modifier = Modifier.fillMaxWidth().height(160.dp)
                                    )
                                }
                            }

                            item { SectionHeader(if (s.period == ReportPeriod.WEEKLY) "Detalle diario" else "Detalle semanal") }

                            if (s.period == ReportPeriod.WEEKLY) {
                                items(s.report?.dailyBreakdown ?: emptyList()) { day -> DailyMetricItem(day) }
                            } else {
                                items(s.monthlyReport?.weeklyBreakdown ?: emptyList()) { week -> WeeklySummaryItem(week) }
                            }
                        } else if (s.summary != null) {
                            item {
                                SectionHeader("Estadísticas Generales")
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        val report = s.summary!!
                                        StatRow("Total Reservas", report.totalReservations.toString())
                                        StatRow("Atenciones Completadas", report.completedAttentions.toString())
                                        StatRow("Cobros Realizados", report.paidBillingCount.toString())
                                        StatRow("Pendiente de Cobro", "$${report.pendingAmount}")
                                    }
                                }
                            }
                        }
                        item { Spacer(Modifier.height(100.dp)) }
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
            Text(subLabel, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }

        IconButton(onClick = onNext, enabled = !isLoading) {
            Icon(Icons.Default.ChevronRight, "Siguiente")
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}


@Composable
fun DailyMetricItem(day: DailyMetricDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(day.dayOfWeek, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(day.date, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Text("$${day.revenue}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun SimpleBarChart(data: List<Float>, modifier: Modifier = Modifier) {
    val maxVal = (data.maxOrNull() ?: 0f).coerceAtLeast(1f)
    val barColor = MaterialTheme.colorScheme.secondary
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val spaceBetweenBars = 8.dp.toPx()
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
}

@Composable
fun WeeklySummaryItem(week: WeeklySummaryDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Semana ${week.weekNumber}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(week.dateRange, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Text("$${week.revenue}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
        }
    }
}
