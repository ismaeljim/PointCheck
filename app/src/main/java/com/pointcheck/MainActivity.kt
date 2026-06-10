package com.pointcheck

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.pointcheck.core.navigation.AppNavigation
import com.pointcheck.core.ui.theme.PointCheckTheme

/**
 * Actividad principal de la aplicación.
 * Actúa como el punto de entrada único (Single Activity Architecture).
 * Configura el contenedor principal y delega la navegación a AppNavigation.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "Iniciando aplicación y configurando setContent")
        setContent {
            PointCheckApp()
        }
    }
}

/**
 * Composable raíz de la aplicación.
 * Gestiona el Tema Global, Scaffold y el SnackbarHost para notificaciones.
 */
@Composable
fun PointCheckApp() {
    // Estado para mostrar mensajes (Snackbars) en toda la aplicación
    val snackbarHostState = remember { SnackbarHostState() }
    
    PointCheckTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            // El Box con padding asegura que el contenido no quede oculto tras barras de sistema
            Box(modifier = Modifier.padding(innerPadding)) {
                AppNavigation(snackbarHostState)
            }
        }
    }
}
