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
import androidx.compose.ui.graphics.Brush
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
import com.pointcheck.core.presentation.components.AppButton
import com.pointcheck.core.presentation.components.AppTextField

@OptIn(ExperimentalMaterial3Api::class)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    )
                )
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo Section
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = Color.White
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    "PointCheck",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    "Professional Management Suite",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
                
                Spacer(Modifier.height(48.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(32.dp)) {
                        Text(
                            "Bienvenido",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Inicia sesión para continuar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        
                        Spacer(Modifier.height(32.dp))
                        
                        AppTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Correo electrónico",
                            leadingIcon = Icons.Default.AlternateEmail,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            enabled = !s.isLoading
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña", style = MaterialTheme.typography.bodyMedium) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.LockOpen, null, tint = MaterialTheme.colorScheme.primary) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            enabled = !s.isLoading,
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(Modifier.height(32.dp))
                        
                        AppButton(
                            text = "Acceder",
                            onClick = {
                                vm.login(email.trim(), password) { ok ->
                                    if (ok) {
                                        nav.navigate(Screen.Main.route) {
                                            popUpTo(Screen.Login.route) { inclusive = true }
                                        }
                                    }
                                }
                            },
                            isLoading = s.isLoading,
                            enabled = email.isNotBlank() && password.isNotBlank(),
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                TextButton(
                    onClick = { nav.navigate(Screen.Register.route) },
                    enabled = !s.isLoading
                ) {
                    Text(
                        "¿No tienes cuenta? Crea una aquí",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
