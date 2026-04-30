package com.pointcheck.features.dashboard.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.core.navigation.Screen
import com.pointcheck.features.dashboard.data.dto.DashboardMetricsDto
import com.pointcheck.features.dashboard.data.dto.ReportSummaryResponseDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(nav: NavController, vm: DashboardViewModel = viewModel()) {
    val s by vm.state.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PointCheck") },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Mi Perfil") },
                            onClick = { nav.navigate(Screen.Profile.route); showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Refrescar") },
                            onClick = { vm.loadDashboard(); showMenu = false }
                        )
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hola, ${s.userName}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Rol: ${s.userRole}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            if (s.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))
            }

            Spacer(Modifier.height(24.dp))

            if (s.userRole == "SPECIALIST" || s.userRole == "PROFESSIONAL") {
                ProfessionalDashboard(s.reportSummary, nav)
            } else {
                ClientDashboard(s.metrics, nav)
            }

            s.error?.let {
                Spacer(Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
                Button(onClick = { vm.loadDashboard() }) {
                    Text("Reintentar")
                }
            }
        }
    }
}

@Composable
fun ClientDashboard(m: DashboardMetricsDto, nav: NavController) {
    Column(Modifier.fillMaxWidth()) {
        Text("Resumen de Actividad", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard("Próximas", m.upcomingReservationsCount.toString(), Icons.Default.Event, Modifier.weight(1f))
            MetricCard("Recientes", m.recentReservationsCount.toString(), Icons.Default.History, Modifier.weight(1f))
        }
        
        Spacer(Modifier.height(24.dp))
        
        DashboardButton("Mis Citas", Icons.Default.CalendarMonth) { nav.navigate(Screen.Scheduled.route) }
        DashboardButton("Nueva Reserva", Icons.Default.AddCircle) { nav.navigate(Screen.Booking.route) }
    }
}

@Composable
fun ProfessionalDashboard(r: ReportSummaryResponseDto?, nav: NavController) {
    Column(Modifier.fillMaxWidth()) {
        Text("Reporte de Desempeño", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        
        if (r != null) {
            // Grid de Métricas Operacionales
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("Reservas Total", r.totalReservations.toString(), Icons.Default.Assessment, Modifier.weight(1f))
                MetricCard("Hoy", r.todayReservations.toString(), Icons.Default.Today, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("Atenciones", r.completedAttentions.toString(), Icons.Default.CheckCircle, Modifier.weight(1f))
                MetricCard("Promedio", "${r.averageAttentionMinutes.toInt()} min", Icons.Default.Timer, Modifier.weight(1f))
            }
            
            Spacer(Modifier.height(24.dp))
            Text("Resumen Financiero", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    FinancialRow("Total Cobrado", "$${r.totalCharged}", MaterialTheme.colorScheme.primary)
                    FinancialRow("Monto Pendiente", "$${r.pendingAmount}", MaterialTheme.colorScheme.error)
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    FinancialRow("Cobros Pagados", r.paidBillingCount.toString())
                    FinancialRow("Cobros Pendientes", r.pendingBillingCount.toString())
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Accesos Rápidos", style = MaterialTheme.typography.titleMedium)
        
        DashboardButton("Mi Agenda", Icons.Default.CalendarMonth) { nav.navigate(Screen.Scheduled.route) }
        DashboardButton("Servicios", Icons.AutoMirrored.Filled.List) { nav.navigate(Screen.ServiceManagement.route) }
        DashboardButton("Suscripción", Icons.Default.Star) { nav.navigate(Screen.Subscription.route) }
    }
}

@Composable
fun FinancialRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun MetricCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
}

@Composable
fun DashboardButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        Icon(icon, null)
        Spacer(Modifier.width(12.dp))
        Text(text, fontSize = 16.sp)
    }
}
