package com.pointcheck.features.subscriptions.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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
import com.pointcheck.core.prefs.UserPreferences
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(nav: NavController, vm: SubscriptionViewModel = viewModel()) {
    val s by vm.state.collectAsState()
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    var profileId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        profileId = prefs.professionalProfileId.first()
        profileId?.let { vm.loadSubscription(it) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mi Suscripción") }) }
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
            } else if (s.subscription == null) {
                Text("No tienes una suscripción activa.", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(24.dp))
                PlanCard("Plan Gratuito", "Ideal para empezar", "0 CLP", onSelect = {
                    profileId?.let { vm.upgradePlan(it, "FREE") }
                })
                Spacer(Modifier.height(16.dp))
                PlanCard("Plan Premium", "Agenda ilimitada y reportes", "9.990 CLP / mes", isPremium = true, onSelect = {
                    profileId?.let { vm.upgradePlan(it, "PREMIUM") }
                })
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (s.subscription?.status == "ACTIVE") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    )
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Plan: ${s.subscription?.planName}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Estado: ${s.subscription?.status}")
                        Text("Desde: ${s.subscription?.startDate}")
                        Text("Hasta: ${s.subscription?.endDate}")
                    }
                }
                
                Spacer(Modifier.height(32.dp))
                Text("¿Deseas cambiar de plan?", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                
                if (s.subscription?.planName == "FREE") {
                    Button(onClick = { profileId?.let { vm.upgradePlan(it, "PREMIUM") } }) {
                        Text("Cambiar a PREMIUM")
                    }
                } else {
                    OutlinedButton(onClick = { profileId?.let { vm.upgradePlan(it, "FREE") } }) {
                        Text("Volver al plan FREE")
                    }
                }
            }

            s.error?.let {
                Spacer(Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun PlanCard(title: String, desc: String, price: String, isPremium: Boolean = false, onSelect: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onSelect
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(desc, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Text(price, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}
