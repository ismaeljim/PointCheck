package com.pointcheck.features.external.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(serviceName: String, nav: NavController, vm: ExternalApiViewModel = viewModel()) {
    
    // Si el servicio es clima, lo cargamos automáticamente
    LaunchedEffect(serviceName) {
        if (serviceName.lowercase().contains("clima") || serviceName.lowercase().contains("weather")) {
            vm.fetchWeather("Madrid") // Por defecto Madrid para la demo
        }
    }

    val weather = vm.weatherState.value

    Scaffold(
        topBar = { TopAppBar(title = { Text("Detalle: $serviceName") }) }
    ) { pad ->
        Column(
            modifier = Modifier.padding(pad).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (weather != null) {
                Text(text = "Información del Clima:", style = MaterialTheme.typography.headlineSmall)
                Text(text = weather, modifier = Modifier.padding(16.dp))
            } else {
                Text("Cargando información de $serviceName...")
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { nav.popBackStack() }) {
                Text("Volver")
            }
        }
    }
}
