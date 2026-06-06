package com.pointcheck.features.auth.presentation

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointcheck.core.navigation.Screen
import com.pointcheck.core.presentation.components.*

@Composable
fun LoginScreen(
    nav: NavController,
    vm: UserViewModel = viewModel()
) {
    val s by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(s.error) {
        s.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header con estilo Mercado Pago
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.35f)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.padding(16.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "PointCheck",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Tu plataforma de servicios",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Cuerpo del login
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppCard {
                    Column(Modifier.padding(24.dp)) {
                        Text(
                            "Ingresa a tu cuenta",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(24.dp))
                        
                        AppTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "E-mail",
                            leadingIcon = Icons.Default.AlternateEmail,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            enabled = !s.isLoading
                        )
                        Spacer(Modifier.height(16.dp))
                        
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
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )
                        Spacer(Modifier.height(32.dp))
                        
                        AppButton(
                            text = "Entrar",
                            onClick = {
                                vm.login(email.trim(), password) { ok ->
                                    if (ok) {
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
                
                Spacer(Modifier.weight(1f))
                
                TextButton(
                    onClick = { nav.navigate(Screen.Register.route) }, 
                    enabled = !s.isLoading,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        "¿No tienes cuenta? Regístrate aquí",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
