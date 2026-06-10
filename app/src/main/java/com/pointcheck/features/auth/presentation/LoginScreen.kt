package com.pointcheck.features.auth.presentation

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointcheck.core.navigation.Screen
import com.pointcheck.core.presentation.components.AppButton
import com.pointcheck.core.presentation.components.AppTextField
import com.pointcheck.core.presentation.components.AppTopBar

/**
 * Pantalla de inicio de sesión para la autenticación de usuarios.
 *
 * Proporciona campos para el correo electrónico y la contraseña, y maneja la navegación
 * al panel de control (Dashboard) tras un inicio de sesión exitoso.
 *
 * @param nav Controlador de navegación para las transiciones entre pantallas.
 * @param vm ViewModel que gestiona el estado y la lógica de la autenticación.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    nav: NavController,
    vm: UserViewModel = viewModel()
) {
    Log.d("LoginScreen", "Renderizando LoginScreen - Punto de control de acceso")
    
    // Observamos el estado del ViewModel (isLoading, error, etc.)
    val s by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Estados locales para el formulario
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Efecto para reaccionar a errores provenientes del backend (ej: 401 Unauthorized)
    LaunchedEffect(s.error) {
        s.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError() // Limpiamos para evitar que el mensaje se repita en recomposiciones
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Iniciar sesión") },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Branding de la aplicación
            Text(
                "PointCheck",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Gestión inteligente de servicios",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Spacer(Modifier.height(40.dp))

            // Tarjeta contenedora del formulario de login
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "Bienvenido",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(16.dp))
                    
                    // Input de Correo con validación de tipo teclado
                    AppTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Correo electrónico",
                        leadingIcon = Icons.Default.Email,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        enabled = !s.isLoading
                    )
                    Spacer(Modifier.height(12.dp))
                    
                    // Input de Contraseña con toggle de visibilidad
                    AppTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Contraseña",
                        leadingIcon = Icons.Default.Lock,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        enabled = !s.isLoading,
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Cambiar visibilidad de contraseña"
                                )
                            }
                        }
                    )
                    Spacer(Modifier.height(24.dp))
                    
                    // Botón de acción principal
                    AppButton(
                        text = "Iniciar Sesión",
                        onClick = {
                            // Trim en email para evitar errores de espacios accidentales
                            vm.login(email.trim(), password) { ok ->
                                if (ok) {
                                    // Navegación al Dashboard limpiando el stack de login
                                    nav.navigate(Screen.Dashboard.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                            }
                        },
                        isLoading = s.isLoading,
                        enabled = email.isNotBlank() && password.isNotBlank()
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Link para registro de nuevos usuarios
            TextButton(onClick = { nav.navigate(Screen.Register.route) }, enabled = !s.isLoading) {
                Text("¿No tienes cuenta? Regístrate aquí")
            }
        }
    }
}
