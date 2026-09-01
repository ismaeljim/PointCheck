package com.pointcheck.features.auth.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.pointcheck.core.navigation.Screen
import com.pointcheck.core.ui.components.PointCheckButton
import com.pointcheck.core.ui.components.PointCheckCard
import com.pointcheck.core.ui.components.PointCheckTextField
import com.pointcheck.core.ui.components.PointCheckTopBar
import com.pointcheck.core.util.RutUtils
import com.pointcheck.core.util.RutVisualTransformation

/**
 * Pantalla para el registro de nuevos usuarios.
 *
 * Soporta el registro tanto para Clientes regulares como para Especialistas. Para los especialistas,
 * captura detalles adicionales de la ubicación del negocio antes de proceder a la selección de categoría.
 *
 * @param nav Controlador de navegación para las transiciones entre pantallas.
 * @param vm ViewModel que gestiona el estado y la lógica del registro.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    nav: NavController,
    vm: UserViewModel = viewModel()
) {
    val s by vm.state.collectAsStateWithLifecycle()
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
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { 
            PointCheckTopBar(
                title = "Crear Cuenta",
                onBack = { nav.popBackStack() }
            ) 
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PointCheckCard(
                title = "Información Personal",
                subtitle = "Tus datos básicos para la cuenta",
                icon = Icons.Default.Person
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PointCheckTextField(
                        value = s.name,
                        onValueChange = { vm.onValueChange("name", it) },
                        label = "Nombre Completo",
                        placeholder = "Ej: Juan Pérez",
                        leadingIcon = Icons.Default.Person,
                        enabled = !s.isLoading
                    )

                    PointCheckTextField(
                        value = s.email,
                        onValueChange = { vm.onValueChange("email", it) },
                        label = "Correo Electrónico",
                        placeholder = "ejemplo@correo.com",
                        leadingIcon = Icons.Default.Email,
                        enabled = !s.isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    
                    val isRutValid = remember(s.rut) { RutUtils.validateRut(s.rut) || s.rut.isEmpty() }
                    PointCheckTextField(
                        value = s.rut,
                        onValueChange = { vm.onValueChange("rut", it) },
                        label = "RUT",
                        placeholder = "12.345.678-9",
                        leadingIcon = Icons.Default.Badge,
                        enabled = !s.isLoading,
                        isError = !isRutValid,
                        supportingText = if (!isRutValid) "RUT inválido" else null,
                        visualTransformation = RutVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )

                    PointCheckTextField(
                        value = s.phone,
                        onValueChange = { vm.onValueChange("phone", it) },
                        label = "Teléfono de contacto",
                        placeholder = "+569 1234 5678",
                        leadingIcon = Icons.Default.Phone,
                        enabled = !s.isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    PointCheckTextField(
                        value = s.password,
                        onValueChange = { vm.onValueChange("password", it) },
                        label = "Contraseña",
                        placeholder = "Mínimo 6 caracteres",
                        leadingIcon = Icons.Default.Lock,
                        enabled = !s.isLoading,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null)
                            }
                        }
                    )

                    PointCheckTextField(
                        value = s.confirm,
                        onValueChange = { vm.onValueChange("confirm", it) },
                        label = "Confirmar Contraseña",
                        placeholder = "Repita su contraseña",
                        leadingIcon = Icons.Default.Lock,
                        enabled = !s.isLoading,
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

            Spacer(Modifier.height(12.dp))

            PointCheckCard(
                title = "Configuración de Cuenta",
                subtitle = "Define tu rol en la plataforma",
                icon = Icons.Default.Settings
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Perfil de Especialista", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text("Activa esto si ofrecerás servicios", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        }
                        Switch(
                            checked = s.role == "SPECIALIST",
                            onCheckedChange = { isSpec -> vm.onValueChange("role", if (isSpec) "SPECIALIST" else "CLIENT") }
                        )
                    }

                    if (s.role == "SPECIALIST") {
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(Modifier.height(16.dp))
                        
                        Text("Ubicación del Servicio", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        PointCheckTextField(
                            value = s.city,
                            onValueChange = { vm.onValueChange("city", it) },
                            label = "Ciudad",
                            placeholder = "Ej: Santiago",
                            leadingIcon = Icons.Default.LocationCity
                        )
                        Spacer(Modifier.height(8.dp))
                        PointCheckTextField(
                            value = s.address,
                            onValueChange = { vm.onValueChange("address", it) },
                            label = "Dirección",
                            placeholder = "Ej: Av. Providencia 1234",
                            leadingIcon = Icons.Default.Place
                        )
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Subir Foto")
                        }
                        Spacer(Modifier.width(16.dp))
                        if (s.avatarUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(s.avatarUri),
                                contentDescription = null,
                                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Surface(
                                modifier = Modifier.size(64.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Person, null, modifier = Modifier.padding(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Box(Modifier.padding(horizontal = 12.dp)) {
                PointCheckButton(
                    text = if (s.role == "SPECIALIST") "Siguiente: Especialidad" else "Crear mi cuenta",
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
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}
