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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.pointcheck.core.navigation.Screen
import com.pointcheck.core.ui.components.PointCheckButton
import com.pointcheck.core.ui.components.PointCheckTextField
import com.pointcheck.core.ui.components.PointCheckTopBar

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
    vm: LoginViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Forzar el foco inicial y levantar teclado (Fix para Xiaomi)
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    // Reaccionar al éxito del login
    LaunchedEffect(state) {
        if (state is LoginUiState.Success) {
            nav.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    // Manejo de errores mediante Snackbar
    if (state is LoginUiState.Input) {
        val inputState = state as LoginUiState.Input
        LaunchedEffect(inputState.error) {
            inputState.error?.let {
                snackbarHostState.showSnackbar(it)
                vm.clearError()
            }
        }
    }

    Scaffold(
        topBar = { PointCheckTopBar(title = "Iniciar sesión") },
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

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                val input = (state as? LoginUiState.Input) ?: LoginUiState.Input()
                val isLoading = state is LoginUiState.Loading

                Column(Modifier.padding(20.dp)) {
                    Text(
                        "Bienvenido",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(16.dp))
                    
                    PointCheckTextField(
                        value = input.email,
                        onValueChange = { vm.onValueChange("email", it) },
                        label = "Correo electrónico",
                        placeholder = "ejemplo@correo.com",
                        leadingIcon = Icons.Default.Email,
                        modifier = Modifier.focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        enabled = !isLoading
                    )
                    Spacer(Modifier.height(12.dp))
                    
                    var passwordVisible by remember { mutableStateOf(false) }
                    PointCheckTextField(
                        value = input.password,
                        onValueChange = { vm.onValueChange("password", it) },
                        label = "Contraseña",
                        placeholder = "••••••••",
                        leadingIcon = Icons.Default.Lock,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        enabled = !isLoading,
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                    Spacer(Modifier.height(24.dp))
                    
                    PointCheckButton(
                        text = "Iniciar Sesión",
                        onClick = { vm.login() },
                        isLoading = isLoading,
                        enabled = input.isValid && !isLoading
                    )

                    Spacer(Modifier.height(8.dp))

                    TextButton(
                        onClick = { nav.navigate(Screen.ForgotPassword.route) },
                        modifier = Modifier.align(Alignment.End),
                        enabled = !isLoading
                    ) {
                        Text(
                            "¿Olvidaste tu contraseña?",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            TextButton(onClick = { nav.navigate(Screen.Register.route) }, enabled = !(state is LoginUiState.Loading)) {
                Text("¿No tienes cuenta? Regístrate aquí")
            }
        }
    }
}
