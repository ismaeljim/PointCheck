package com.pointcheck.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.pointcheck.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(nav: NavController) {
    Scaffold(topBar = { TopAppBar(title = { Text("Detalle del Servicio") }) }) { pad ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = { nav.navigate(Screen.Profile.route) }) {
                Text("Ir al Perfil")
            }
        }
    }
}
