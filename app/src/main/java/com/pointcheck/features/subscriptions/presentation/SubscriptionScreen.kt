package com.pointcheck.features.subscriptions.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(nav: NavController, vm: SubscriptionViewModel = viewModel()) {
    val s by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Suscripción") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (s.isLoading) {
                CircularProgressIndicator()
            } else {
                if (s.currentSubscription != null) {
                    ActiveSubscriptionCard(
                        sub = s.currentSubscription!!,
                        onCancel = { vm.cancelSubscription() }
                    )
                } else {
                    NoSubscriptionView(
                        onSelectPlan = { vm.createSubscription(it) }
                    )
                }
            }

            s.error?.let {
                Spacer(Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            s.successMessage?.let {
                Spacer(Modifier.height(16.dp))
                Text(it, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.weight(1f))
            
            Text(
                "Gestión básica de plan comercial. No incluye cobro automático.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ActiveSubscriptionCard(
    sub: com.pointcheck.features.subscriptions.data.dto.SubscriptionResponseDto,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (sub.status == "ACTIVE") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        )
    ) {
        Column(Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (sub.status == "ACTIVE") Icons.Default.CheckCircle else Icons.Default.Info,
                    contentDescription = null,
                    tint = if (sub.status == "ACTIVE") Color(0xFF2E7D32) else Color.Red
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Plan: ${sub.planName}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(16.dp))
            Text("Estado: ${sub.status}", fontWeight = FontWeight.Medium)
            Text("Desde: ${sub.startDate}")
            Text("Hasta: ${sub.endDate}")
            
            if (sub.status == "ACTIVE") {
                Spacer(Modifier.height(24.dp))
                OutlinedButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CANCELAR SUSCRIPCIÓN")
                }
            }
        }
    }
}

@Composable
fun NoSubscriptionView(onSelectPlan: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "No tienes una suscripción activa.",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(24.dp))
        
        PlanCard(
            title = "BASIC",
            desc = "Gestión de agenda y reportes básicos",
            price = "GRATIS",
            onSelect = { onSelectPlan("BASIC") }
        )
        
        Spacer(Modifier.height(16.dp))
        
        PlanCard(
            title = "PREMIUM",
            desc = "Agenda ilimitada, reportes avanzados y soporte",
            price = "9.990 CLP / mes",
            isPremium = true,
            onSelect = { onSelectPlan("PREMIUM") }
        )
    }
}

@Composable
fun PlanCard(title: String, desc: String, price: String, isPremium: Boolean = false, onSelect: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onSelect
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (isPremium) {
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBC02D))
                }
            }
            Text(desc, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Text(price, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}
