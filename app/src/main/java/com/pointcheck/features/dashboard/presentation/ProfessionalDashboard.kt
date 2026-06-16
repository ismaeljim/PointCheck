package com.pointcheck.features.dashboard.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.pointcheck.core.navigation.Screen
import com.pointcheck.core.ui.components.PCCard
import com.pointcheck.core.ui.components.PCOutlinedButton
import com.pointcheck.features.dashboard.data.dto.ReportSummaryResponseDto

@Composable
fun ProfessionalDashboard(r: ReportSummaryResponseDto?, nav: NavController, s: DashboardUiState) {
    Column(Modifier.fillMaxWidth()) {
        Text("Panel de Control", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        
        if (r != null) {
            // Métricas operacionales rápidas (Clickable para navegación filtrada)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard(
                    label = "Citas Mes", 
                    value = (r.totalReservations).toString(), 
                    icon = Icons.Default.Assessment, 
                    modifier = Modifier.weight(1f),
                    onClick = { nav.navigate(Screen.Scheduled.createRoute("month")) }
                )
                MetricCard(
                    label = "Hoy", 
                    value = (r.todayReservations).toString(), 
                    icon = Icons.Default.Today, 
                    modifier = Modifier.weight(1f),
                    onClick = { nav.navigate(Screen.Scheduled.createRoute("today")) }
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Card Principal de Reporte: Único punto de acceso a detalles y finanzas
            PCCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { nav.navigate(Screen.WeeklyReport.route) }
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Insights, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Reporte de Desempeño", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    FinancialRow("Ingresos del Mes", "$${r.totalCharged ?: 0.0}", MaterialTheme.colorScheme.primary)
                    FinancialRow("Por Cobrar", "$${r.pendingAmount ?: 0.0}", MaterialTheme.colorScheme.error)
                    FinancialRow("Tiempo Promedio", "${(r.averageAttentionMinutes ?: 0.0).toInt()} min")
                    
                    // Gráfico de Actividad Reciente (Especialista)
                    if (!s.metrics.activitySeries.isNullOrEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text("Actividad Últimos 7 Días", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        val model = entryModelOf(*s.metrics.activitySeries.map { (it.value ?: 0.0).toFloat() }.toTypedArray())
                        Chart(
                            chart = columnChart(),
                            model = model,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                            modifier = Modifier.fillMaxWidth().height(120.dp)
                        )
                    }

                    HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Ver desglose semanal y estadísticas",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        } else {
            // Efecto Shimmer granular cuando los datos aún no están listos pero ya no se está en carga global
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f).height(100.dp).clip(MaterialTheme.shapes.medium).shimmerEffect())
                Box(Modifier.weight(1f).height(100.dp).clip(MaterialTheme.shapes.medium).shimmerEffect())
            }
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().height(220.dp).clip(MaterialTheme.shapes.medium).shimmerEffect())
        }

        Spacer(Modifier.height(24.dp))
        Text("Accesos Rápidos", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        
        PCOutlinedButton(text = "Perfil Profesional", icon = Icons.Default.Person, onClick = { nav.navigate(Screen.ProfessionalProfile.route) }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        PCOutlinedButton(text = "Mi Agenda", icon = Icons.Default.CalendarMonth, onClick = { nav.navigate(Screen.Scheduled.route) }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        PCOutlinedButton(text = "Mis Cobros", icon = Icons.Default.Payments, onClick = { nav.navigate(Screen.BillingList.route) }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        PCOutlinedButton(text = "Servicios", icon = Icons.AutoMirrored.Filled.List, onClick = { nav.navigate(Screen.ServiceManagement.route) }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        PCOutlinedButton(text = "Suscripción", icon = Icons.Default.Star, onClick = { nav.navigate(Screen.Subscription.route) }, modifier = Modifier.fillMaxWidth())
    }
}
