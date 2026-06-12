package com.pointcheck.features.admin.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pointcheck.core.presentation.components.AppButton
import com.pointcheck.core.presentation.components.AppTextField
import com.pointcheck.features.admin.data.dto.AdminUserUpdateRequestDto
import com.pointcheck.features.auth.data.dto.UserResponseDto
import com.pointcheck.features.onboarding.presentation.dto.CategoryDto

/**
 * Diálogo modal para la edición de datos de usuario por parte de un administrador.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserDialog(
    user: UserResponseDto,
    categories: List<CategoryDto>,
    onDismiss: () -> Unit,
    onConfirm: (AdminUserUpdateRequestDto) -> Unit,
    isSaving: Boolean = false
) {
    var name by remember { mutableStateOf(user.name) }
    var phone by remember { mutableStateOf(user.phone) }
    var address by remember { mutableStateOf(user.address ?: "") }
    var role by remember { mutableStateOf(user.role) }
    var selectedCategoryId by remember { mutableStateOf(user.categoryId ?: "") }
    var expandedRole by remember { mutableStateOf(false) }
    var expandedCat by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text("Editar Usuario") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre Completo",
                    enabled = !isSaving
                )

                AppTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Teléfono",
                    enabled = !isSaving
                )

                AppTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Dirección",
                    enabled = !isSaving
                )

                // Selector de Rol
                ExposedDropdownMenuBox(
                    expanded = expandedRole,
                    onExpandedChange = { if (!isSaving) expandedRole = !expandedRole }
                ) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedRole,
                        onDismissRequest = { expandedRole = false }
                    ) {
                        listOf("CLIENT", "SPECIALIST", "ADMIN").forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r) },
                                onClick = {
                                    role = r
                                    expandedRole = false
                                }
                            )
                        }
                    }
                }

                // Selector de Categoría (Solo si es Specialist)
                if (role == "SPECIALIST") {
                    ExposedDropdownMenuBox(
                        expanded = expandedCat,
                        onExpandedChange = { if (!isSaving) expandedCat = !expandedCat }
                    ) {
                        val currentCatName = categories.find { it.id == selectedCategoryId }?.name ?: "Seleccionar Categoría"
                        OutlinedTextField(
                            value = currentCatName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoría Profesional") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCat) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCat,
                            onDismissRequest = { expandedCat = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        selectedCategoryId = cat.id
                                        expandedCat = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            AppButton(
                text = "Guardar",
                onClick = {
                    onConfirm(
                        AdminUserUpdateRequestDto(
                            name = name,
                            phone = phone,
                            address = address,
                            role = role,
                            categoryId = if (role == "SPECIALIST") selectedCategoryId else null
                        )
                    )
                },
                enabled = name.isNotBlank() && !isSaving,
                isLoading = isSaving
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancelar")
            }
        }
    )
}
