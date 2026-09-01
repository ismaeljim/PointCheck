package com.pointcheck.features.profile.presentation

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.core.navigation.Screen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointcheck.features.auth.presentation.UserViewModel
import com.pointcheck.core.ui.components.PointCheckButton
import com.pointcheck.core.ui.components.PointCheckCard
import com.pointcheck.core.ui.components.PointCheckTextField
import com.pointcheck.core.ui.components.PointCheckTopBar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(nav: NavController, vm: UserViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val prefs = UserPreferences(context)
    
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.fetchUserProfile()
    }

    LaunchedEffect(state.userName, state.userPhone, state.userAddress) {
        name = state.userName
        phone = state.userPhone
        address = state.userAddress ?: ""
    }

    Scaffold(topBar = { PointCheckTopBar(title = "Mi Perfil") }) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            PointCheckCard(
                title = "Datos de Usuario",
                subtitle = "Información básica de tu cuenta",
                icon = Icons.Default.AccountCircle
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (isEditing) {
                        PointCheckTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = "Nombre Completo",
                            placeholder = "Ej: Juan Pérez",
                            leadingIcon = Icons.Default.Person
                        )
                        PointCheckTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = "Teléfono",
                            placeholder = "Ej: +569 1234 5678",
                            leadingIcon = Icons.Default.Phone
                        )

                        // Solo permitir edición de dirección si NO es Administrador
                        if (!state.userRole.equals("ADMIN", ignoreCase = true)) {
                            PointCheckTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = "Dirección / Comuna",
                                placeholder = "Ej: Providencia, Santiago",
                                leadingIcon = Icons.Default.Home
                            )
                        }

                        PointCheckButton(
                            text = "Guardar Cambios",
                            onClick = {
                                vm.updateProfile(name, phone, address)
                                isEditing = false
                            },
                            isLoading = state.isLoading
                        )

                        OutlinedButton(
                            onClick = {
                                name = state.userName
                                phone = state.userPhone
                                address = state.userAddress ?: ""
                                isEditing = false
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancelar")
                        }
                    } else {
                        ProfileInfoItem(Icons.Default.Person, "Nombre", state.userName)
                        ProfileInfoItem(Icons.Default.Email, "Email", state.userEmail)
                        ProfileInfoItem(Icons.Default.Phone, "Teléfono", if (state.userPhone.isBlank()) "No registrado" else state.userPhone)
                        
                        // Solo mostrar Dirección si NO es Administrador
                        if (!state.userRole.equals("ADMIN", ignoreCase = true)) {
                            ProfileInfoItem(
                                icon = Icons.Default.Home,
                                label = "Dirección",
                                value = if (state.userAddress.isNullOrBlank()) "No registrada" else state.userAddress!!
                            )
                        }

                        ProfileInfoItem(Icons.Default.Badge, "Rol", state.userRole, color = MaterialTheme.colorScheme.primary)

                        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        TextButton(
                            onClick = { showPasswordDialog = true },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Cambiar Contraseña")
                        }

                        PointCheckButton(
                            text = "Editar Perfil",
                            onClick = { isEditing = true }
                        )
                    }
                }
            }

            if (showPasswordDialog) {
                ChangePasswordDialog(
                    onDismiss = { showPasswordDialog = false },
                    onConfirm = { current, new ->
                        vm.changePassword(current, new) { success, error ->
                            if (success) {
                                showPasswordDialog = false
                                Toast.makeText(context, "Contraseña actualizada exitosamente", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, error ?: "Error al cambiar contraseña", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    isLoading = state.isLoading
                )
            }

            Spacer(Modifier.height(12.dp))

            if (state.userRole.equals("SPECIALIST", ignoreCase = true) || state.userRole.equals("PROFESSIONAL", ignoreCase = true)) {
                PointCheckCard(
                    title = "Gestión Profesional",
                    subtitle = "Configura tus servicios y agenda",
                    icon = Icons.Default.BusinessCenter
                ) {
                    PointCheckButton(
                        text = "Configurar Perfil de Especialista",
                        onClick = { nav.navigate(Screen.ProfessionalProfile.route) }
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            Box(Modifier.padding(horizontal = 12.dp)) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            prefs.clear()
                            nav.navigate(Screen.Login.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar Sesión")
                }
            }
        }
    }
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    isLoading: Boolean
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar Contraseña", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PointCheckTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = "Contraseña Actual",
                    placeholder = "********",
                    leadingIcon = Icons.Default.Lock,
                    visualTransformation = PasswordVisualTransformation()
                )
                PointCheckTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "Nueva Contraseña",
                    placeholder = "Mínimo 6 caracteres",
                    leadingIcon = Icons.Default.Password,
                    supportingText = "Mínimo 6 caracteres",
                    visualTransformation = PasswordVisualTransformation()
                )
                PointCheckTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirmar Nueva Contraseña",
                    placeholder = "Repita la contraseña",
                    leadingIcon = Icons.Default.Lock,
                    visualTransformation = PasswordVisualTransformation()
                )
                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            PointCheckButton(
                text = "Actualizar",
                onClick = {
                    when {
                        newPassword.length < 6 -> validationError = "La nueva contraseña es muy corta"
                        newPassword != confirmPassword -> validationError = "Las contraseñas no coinciden"
                        else -> {
                            validationError = null
                            onConfirm(currentPassword, newPassword)
                        }
                    }
                },
                modifier = Modifier.width(120.dp),
                isLoading = isLoading,
                enabled = currentPassword.isNotBlank() && newPassword.isNotBlank()
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ProfileInfoItem(icon: ImageVector, label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(value, style = MaterialTheme.typography.bodyLarge, color = color)
        }
    }
}
