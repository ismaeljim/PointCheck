package com.pointcheck.features.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.features.dashboard.data.dto.DailyMetricDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyReportScreen(nav: NavController, vm: WeeklyReportViewModel = viewModel()) {
    val s by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reporte Semanal") },
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
                .padding(16.dp)
        ) {
            // Selector de Semana
            WeekSelector(
                weekNumber = s.report?.weekNumber ?: 0,
                year = s.report?.year ?: 0,
                onPrevious = { vm.changeWeek(-1) },
                onNext = { vm.changeWeek(1) },
                isLoading = s.isLoading
            )

            if (s.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))
            }

            s.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
            }

            s.report?.let { report ->
                Spacer(Modifier.height(16.dp))
                
                // Resumen de la Semana
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
                            Text("$${report.totalRevenue}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.AutoMirrored.Filled.TrendingUp, null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("Desglose Diario", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(report.dailyBreakdown) { day ->
                        DailyMetricItem(day)
                    }
                }
            }
        }
    }
}

@Composable
fun WeekSelector(weekNumber: Int, year: Int, onPrevious: () -> Unit, onNext: () -> Unit, isLoading: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious, enabled = !isLoading) {
            Icon(Icons.Default.ChevronLeft, "Semana anterior")
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Semana $weekNumber", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Año $year", style = MaterialTheme.typography.labelSmall)
        }

        IconButton(onClick = onNext, enabled = !isLoading) {
            Icon(Icons.Default.ChevronRight, "Semana siguiente")
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
