package com.pointcheck.features.billing.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.core.navigation.Screen
import com.pointcheck.core.ui.components.PointCheckCard
import com.pointcheck.core.ui.components.PointCheckTopBar
import com.pointcheck.core.ui.components.PCStatusChip
import com.pointcheck.features.billing.data.dto.BillingRecordResponseDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingListScreen(
    nav: NavController,
    vm: BillingViewModel = viewModel()
) {
    val s by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.loadBillingBySpecialist()
    }

    Scaffold(
        topBar = {
            PointCheckTopBar(
                title = "Gestión Financiera",
                onBack = { nav.popBackStack() }
            )
        }
    ) { pad ->
        if (s.isLoading && s.billings.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (s.billings.isEmpty()) {
            BillingEmptyState()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    Text(
                        "Historial de Cobros",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(s.billings) { billing ->
                    BillingItem(billing) {
                        nav.navigate(Screen.Billing.createRoute(billing.reservationId, billing.attentionId))
                    }
                }
            }
        }
    }
}

@Composable
fun BillingItem(billing: BillingRecordResponseDto, onClick: () -> Unit) {
    PointCheckCard(
        title = billing.client.name,
        subtitle = billing.createdAt,
        icon = Icons.Default.AttachMoney,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        badgeText = billing.status,
        badgeColor = if (billing.status == "PAID") 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${billing.amount} ${billing.currency}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}
