package com.pointcheck.features.subscriptions.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.core.presentation.components.AppButton
import com.pointcheck.core.presentation.components.AppOutlinedButton
import com.pointcheck.core.presentation.components.AppTopBar
import com.pointcheck.features.subscriptions.data.dto.SubscriptionResponseDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(nav: NavController, vm: SubscriptionViewModel = viewModel()) {
    val s by vm.state.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(s.error) {
        s.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }

    LaunchedEffect(s.successMessage) {
        s.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "Mi Suscripción",
                onBack = { nav.popBackStack() }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Gestiona tu plan comercial",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                "Asegura la operatividad de tu negocio eligiendo el plan que mejor se adapte a tus necesidades.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(Modifier.height(24.dp))

            if (s.isLoading) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                if (s.currentSubscription != null) {
                    ActiveSubscriptionCard(
                        sub = s.currentSubscription!!,
                        onCancel = { vm.cancelSubscription() },
                        isLoading = s.isLoading
                    )
                } else {
                    NoSubscriptionView(
                        onSelectPlan = { vm.createSubscription(it) },
                        isLoading = s.isLoading
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
            
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Nota: Esta es una gestión básica de plan comercial. Los cobros automáticos no están habilitados en esta versión.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveSubscriptionCard(
    sub: SubscriptionResponseDto,
    onCancel: () -> Unit,
    isLoading: Boolean
) {
    val isActive = sub.status == "ACTIVE"
    val statusColor = if (isActive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFFF1F8E9) else Color(0xFFFFF1F0)
        )
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Info,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = sub.planName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Surface(
                        color = statusColor.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            text = sub.status,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = statusColor.copy(alpha = 0.2f))
            Spacer(Modifier.height(16.dp))

            SubDetailRow(Icons.Default.Info, "Vigencia desde", sub.startDate)
            SubDetailRow(Icons.Default.Info, "Vigencia hasta", sub.endDate)
            
            if (isActive) {
                Spacer(Modifier.height(32.dp))
                AppOutlinedButton(
                    text = "CANCELAR SUSCRIPCIÓN",
                    onClick = onCancel,
                    isLoading = isLoading
                )
            }
        }
    }
}

@Composable
fun SubDetailRow(icon: ImageVector, label: String, value: String) {
    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.width(8.dp))
        Text("$label: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun NoSubscriptionView(onSelectPlan: (String) -> Unit, isLoading: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Selecciona un plan para comenzar",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        PlanCard(
            title = "PLAN BASIC",
            desc = "Ideal para profesionales independientes. Gestión de agenda básica y reportes mensuales.",
            price = "GRATUITO",
            onSelect = { onSelectPlan("BASIC") },
            isLoading = isLoading
        )
        
        Spacer(Modifier.height(16.dp))
        
        PlanCard(
            title = "PLAN PREMIUM",
            desc = "Para negocios en crecimiento. Agenda ilimitada, reportes avanzados, analítica y soporte prioritario.",
            price = "$9.990 CLP / mes",
            isPremium = true,
            onSelect = { onSelectPlan("PREMIUM") },
            isLoading = isLoading
        )
    }
}

@Composable
fun PlanCard(title: String, desc: String, price: String, isPremium: Boolean = false, onSelect: () -> Unit, isLoading: Boolean) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = if (!isLoading) onSelect else ({}),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isPremium) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title, 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold,
                    color = if (isPremium) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                if (isPremium) {
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.Stars, contentDescription = null, tint = Color(0xFFFBC02D))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                desc, 
                style = MaterialTheme.typography.bodyMedium,
                color = if (isPremium) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Text(
                price, 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold,
                color = if (isPremium) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(16.dp))
            AppButton(
                text = "Elegir este plan",
                onClick = onSelect,
                enabled = !isLoading,
                isLoading = isLoading,
                containerColor = if (isPremium) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
        }
    }
}

