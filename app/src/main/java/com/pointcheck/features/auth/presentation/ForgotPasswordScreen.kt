package com.pointcheck.features.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pointcheck.core.ui.components.PointCheckButton
import com.pointcheck.core.ui.components.PointCheckTextField
import com.pointcheck.core.ui.components.PointCheckTopBar

/**
 * Pantalla para la recuperación de contraseña.
 * Permite al usuario ingresar su correo para recibir instrucciones.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { PointCheckTopBar(title = "Recuperar contraseña", onBack = onBack) }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "¿Olvidaste tu contraseña?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Ingresa tu correo electrónico y te enviaremos las instrucciones para restablecerla.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            Spacer(Modifier.height(32.dp))

            PointCheckTextField(
                value = email,
                onValueChange = { email = it },
                label = "Correo electrónico",
                placeholder = "ejemplo@correo.com",
                leadingIcon = Icons.Default.Email,
                enabled = !isLoading
            )

            if (message != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = message!!,
                    color = if (message!!.contains("error", true)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(24.dp))

            PointCheckButton(
                text = "Enviar instrucciones",
                onClick = {
                    isLoading = true
                    // Simulación de envío de correo para el Bloque 1
                    message = "Si el correo existe en nuestro sistema, recibirás las instrucciones en breve."
                    isLoading = false
                },
                isLoading = isLoading,
                enabled = email.isNotBlank() && !isLoading
            )
        }
    }
}
