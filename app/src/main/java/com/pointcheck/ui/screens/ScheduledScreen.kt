package com.pointcheck.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.viewmodel.ScheduledViewModel
import com.pointcheck.viewmodel.ScheduledViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduledScreen(nav: NavController, reservationId: Int?) {
    val application = LocalContext.current.applicationContext as Application
    val factory = ScheduledViewModelFactory(application, reservationId ?: -1)
    val vm: ScheduledViewModel = viewModel(factory = factory)
    val scope = rememberCoroutineScope()

    val reservation by vm.reservation.collectAsState(initial = null)
    var reservationName by remember { mutableStateOf("") }

    // Actualizar el nombre en el estado local cuando la reserva se carga
    LaunchedEffect(reservation) {
        reservation?.let { reservationName = it.name }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Detalle de la Cita") }) }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (reservation == null) {
                CircularProgressIndicator()
            } else {
                OutlinedTextField(
                    value = reservationName,
                    onValueChange = { reservationName = it },
                    label = { Text("Nombre de la reserva") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = {
                        scope.launch {
                            vm.updateReservationName(reservationName)
                            nav.popBackStack()
                        }
                    }) {
                        Text("Guardar")
                    }
                    Button(onClick = {
                        scope.launch {
                            vm.deleteReservation()
                            nav.popBackStack()
                        }
                    }) {
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}
