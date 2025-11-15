package com.pointcheck.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.viewmodel.ScheduledViewModel
import com.pointcheck.viewmodel.ScheduledViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduledScreen(nav: NavController, reservationId: Int?) {
    val application = LocalContext.current.applicationContext as Application
    val factory = ScheduledViewModelFactory(application, reservationId ?: -1)
    val vm: ScheduledViewModel = viewModel(factory = factory)

    val reservation by vm.reservation.collectAsState(initial = null)

    Scaffold(
        topBar = { TopAppBar(title = { Text("Detalle de la Cita") }) }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (reservation == null) {
                CircularProgressIndicator()
            } else {
                Text(
                    text = reservation!!.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val dateString = dateFormat.format(Date(reservation!!.epochMillis))
                Text(
                    text = "Fecha: $dateString",
                    style = MaterialTheme.typography.bodyLarge
                )
                // Aquí puedes añadir más detalles y los botones de editar/borrar
            }
        }
    }
}
