package com.pointcheck.features.auth.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import com.pointcheck.core.navigation.Screen

import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    nav: NavController,
    vm: UserViewModel = viewModel()
) {
    val s by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.setAvatar(uri)
    }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Crear Cuenta") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            ) 
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Información Personal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = s.name,
                        onValueChange = { vm.onValueChange("name", it) },
                        label = { Text("Nombre Completo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = s.email,
                        onValueChange = { vm.onValueChange("email", it) },
                        label = { Text("Correo Electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = s.password,
                        onValueChange = { vm.onValueChange("password", it) },
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
                            }
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = s.confirm,
                        onValueChange = { vm.onValueChange("confirm", it) },
                        label = { Text("Confirmar Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Configuración de Cuenta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Perfil de Especialista", style = MaterialTheme.typography.bodyLarge)
                            Text("Activa esto si ofrecerás servicios", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                        Switch(
                            checked = s.role == "SPECIALIST",
                            onCheckedChange = { isSpec -> vm.onValueChange("role", if (isSpec) "SPECIALIST" else "CLIENT") }
                        )
                    }
                    
                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Subir Foto")
                        }
                        Spacer(Modifier.width(16.dp))
                        if (s.avatarUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(s.avatarUri),
                                contentDescription = null,
                                modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.small),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Surface(
                                modifier = Modifier.size(56.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small
                            ) { }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            if (s.error != null) {
                Text(s.error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 12.dp))
            }

            Button(
                onClick = {
                    vm.save {
                        nav.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                },
                enabled = s.isValid && !s.isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (s.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Crear mi cuenta", fontSize = 16.sp)
                }
            }
            
            Spacer(Modifier.height(24.dp))
        }
    }
}
