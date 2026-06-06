package com.pointcheck.features.auth.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.pointcheck.core.navigation.Screen
import com.pointcheck.core.presentation.components.*
import com.pointcheck.core.util.RutUtils

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

    LaunchedEffect(s.error) {
        s.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }

    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { 
            AppTopBar(
                title = "Crear Cuenta",
                onBack = { nav.popBackStack() }
            ) 
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
        ) {
            // Header decorativo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .offset(y = (-40).dp)
            ) {
                // Selección de Avatar
                Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Surface(
                        modifier = Modifier
                            .size(100.dp)
                            .clickable { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        shape = CircleShape,
                        color = Color.White,
                        tonalElevation = 4.dp,
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        if (s.avatarUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(s.avatarUri),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.AddAPhoto, 
                                null, 
                                modifier = Modifier.padding(24.dp), 
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                AppCard {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            "Tus Datos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        AppTextField(
                            value = s.name,
                            onValueChange = { vm.onValueChange("name", it) },
                            label = "Nombre Completo",
                            leadingIcon = Icons.Default.Person,
                            enabled = !s.isLoading
                        )

                        AppTextField(
                            value = s.email,
                            onValueChange = { vm.onValueChange("email", it) },
                            label = "Correo Electrónico",
                            leadingIcon = Icons.Default.Email,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            enabled = !s.isLoading
                        )
                        
                        val isRutValid = remember(s.rut) { RutUtils.validateRut(s.rut) || s.rut.isEmpty() }
                        AppTextField(
                            value = s.rut,
                            onValueChange = { vm.onValueChange("rut", it) },
                            label = "RUT",
                            leadingIcon = Icons.Default.Badge,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            enabled = !s.isLoading,
                            isError = !isRutValid
                        )

                        AppTextField(
                            value = s.phone,
                            onValueChange = { vm.onValueChange("phone", it) },
                            label = "Teléfono",
                            leadingIcon = Icons.Default.Phone,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            enabled = !s.isLoading
                        )

                        AppTextField(
                            value = s.password,
                            onValueChange = { vm.onValueChange("password", it) },
                            label = "Contraseña",
                            leadingIcon = Icons.Default.Lock,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                AppCard {
                    Column(Modifier.padding(20.dp)) {
                        Text("Tipo de Cuenta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Perfil Profesional", fontWeight = FontWeight.SemiBold)
                                Text("¿Vas a ofrecer servicios?", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = s.role == "SPECIALIST",
                                onCheckedChange = { isSpec -> vm.onValueChange("role", if (isSpec) "SPECIALIST" else "CLIENT") }
                            )
                        }

                        if (s.role == "SPECIALIST") {
                            Spacer(Modifier.height(24.dp))
                            Text("Ubicación", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            AppTextField(
                                value = s.city,
                                onValueChange = { vm.onValueChange("city", it) },
                                label = "Ciudad",
                                leadingIcon = Icons.Default.LocationCity
                            )
                            Spacer(Modifier.height(12.dp))
                            AppTextField(
                                value = s.address,
                                onValueChange = { vm.onValueChange("address", it) },
                                label = "Dirección",
                                leadingIcon = Icons.Default.Place
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                AppButton(
                    text = if (s.role == "SPECIALIST") "Siguiente: Especialidad" else "Crear cuenta",
                    onClick = {
                        if (s.role == "SPECIALIST") {
                            nav.navigate("category_selection")
                        } else {
                            vm.save {
                                nav.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Register.route) { inclusive = true }
                                }
                            }
                        }
                    },
                    enabled = (if (s.role == "SPECIALIST") (s.name.isNotBlank() && RutUtils.validateRut(s.rut) && s.email.isNotBlank() && s.password.length >= 6) else s.isValid),
                    isLoading = s.isLoading
                )
                
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}
