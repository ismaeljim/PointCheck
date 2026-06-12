package com.pointcheck.features.profile.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.navigation.NavController
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.core.navigation.Screen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointcheck.features.auth.presentation.UserViewModel
import com.pointcheck.core.presentation.components.AppButton
import com.pointcheck.core.presentation.components.AppOutlinedButton
import com.pointcheck.core.presentation.components.AppTextField
import com.pointcheck.core.presentation.components.AppTopBar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(nav: NavController, vm: UserViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val prefs = UserPreferences(context)
    
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.userName, state.userPhone, state.userAddress) {
        name = state.userName
        phone = state.userPhone
        address = state.userAddress ?: ""
    }

    Scaffold(topBar = { AppTopBar(title = "Mi Perfil") }) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Datos de Usuario", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { 
                            if (isEditing) {
                                // Cancelar: restaurar valores
                                name = state.userName
                                phone = state.userPhone
                                address = state.userAddress ?: ""
                            }
                            isEditing = !isEditing 
                        }) {
                            Icon(if (isEditing) Icons.Default.Close else Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }

                    if (isEditing) {
                        AppTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = "Nombre Completo",
                            leadingIcon = Icons.Default.Person
                        )
                        AppTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = "Teléfono",
                            leadingIcon = Icons.Default.Phone
                        )
                        AppTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = "Dirección / Comuna",
                            leadingIcon = Icons.Default.Home
                        )

                        AppButton(
                            text = "Guardar Cambios",
                            onClick = {
                                vm.updateProfile(name, phone, address)
                                isEditing = false
                            },
                            isLoading = state.isLoading
                        )
                    } else {
                        ProfileInfoItem(Icons.Default.Person, "Nombre", state.userName)
                        ProfileInfoItem(Icons.Default.Email, "Email", state.userEmail)
                        ProfileInfoItem(Icons.Default.Phone, "Teléfono", if (state.userPhone.isBlank()) "No registrado" else state.userPhone)
                        ProfileInfoItem(Icons.Default.Home, "Dirección", if (state.userAddress.isNullOrBlank()) "No registrada" else state.userAddress!!)
                        ProfileInfoItem(Icons.Default.Badge, "Rol", state.userRole, color = MaterialTheme.colorScheme.primary)

                        HorizontalDivider(Modifier.padding(vertical = 8.dp))

                        TextButton(
                            onClick = { showPasswordDialog = true },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Cambiar Contraseña")
                        }
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

            Spacer(Modifier.height(24.dp))

            if (state.userRole.equals("SPECIALIST", ignoreCase = true) || state.userRole.equals("PROFESSIONAL", ignoreCase = true)) {
                Text("Gestión Profesional", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                AppButton(
                    text = "Configurar Perfil de Especialista",
                    onClick = { nav.navigate(Screen.ProfessionalProfile.route) }
                )
                Spacer(Modifier.height(16.dp))
            }

            AppOutlinedButton(
                text = "Cerrar Sesión",
                icon = Icons.AutoMirrored.Filled.Logout,
                onClick = {
                    scope.launch {
                        prefs.clear()
                        nav.navigate(Screen.Login.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                }
            )
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
        title = { Text("Cambiar Contraseña") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Contraseña Actual") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Nueva Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text("Mínimo 6 caracteres") }
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmar Nueva Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
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
            Button(
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
                enabled = !isLoading && currentPassword.isNotBlank() && newPassword.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Actualizar")
                }
            }
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
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Text(value, style = MaterialTheme.typography.bodyLarge, color = color)
        }
    }
}
