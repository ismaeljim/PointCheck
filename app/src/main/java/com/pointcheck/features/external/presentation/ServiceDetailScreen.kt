package com.pointcheck.features.external.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.core.presentation.components.AppButton
import com.pointcheck.core.presentation.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    serviceName: String,
    nav: NavController,
    vm: ExternalApiViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearSuccess()
        }
    }

    // Si el servicio es clima, lo cargamos automáticamente
    LaunchedEffect(serviceName) {
        if (serviceName.lowercase().contains("clima") || serviceName.lowercase().contains("weather")) {
            vm.fetchWeather("Madrid") // Por defecto Madrid para la demo
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Detalle: $serviceName",
                onBack = { nav.popBackStack() }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { pad ->
        Box(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val weather = state.weatherData
                    if (weather != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Información del Clima para ${weather.name}",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "Temperatura: ${weather.main.temp}°C")
                                Text(text = "Condición: ${weather.weather.firstOrNull()?.description ?: "N/A"}")
                                Text(text = "Humedad: ${weather.main.humidity}%")
                            }
                        }
                    } else {
                        Text(
                            text = "No hay información adicional disponible para $serviceName",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AppButton(
                        text = "Volver",
                        onClick = { nav.popBackStack() },
                        enabled = !state.isLoading
                    )
                }
            }
        }
    }
}
