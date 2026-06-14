package com.pointcheck.features.admin.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointcheck.core.presentation.components.AppTextField
import com.pointcheck.core.presentation.components.AppTopBar
import com.pointcheck.features.auth.data.dto.UserResponseDto

/**
 * Pantalla para la gestión administrativa de usuarios.
 * 
 * Permite a los administradores buscar usuarios por nombre, email o RUT, corregir sus datos
 * y alternar su estado de activación.
 * 
 * @param onBack Callback para navegar a la pantalla anterior.
 * @param viewModel ViewModel que gestiona el estado administrativo y la lista de usuarios.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    // Mostrar diálogo de edición si hay un usuario seleccionado
    state.selectedUserForEdit?.let { user ->
        EditUserDialog(
            user = user,
            categories = state.categories,
            onDismiss = { viewModel.selectUserForEdit(null) },
            onConfirm = { request -> viewModel.updateUser(user.id ?: "", request) },
            isSaving = state.isSaving
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Gestión de Usuarios",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            AppTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = "Buscar por nombre, email o RUT",
                leadingIcon = Icons.Default.Search
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn {
                    items(state.filteredUsers) { user ->
                        UserItem(
                            user = user,
                            onEdit = { viewModel.selectUserForEdit(user) },
                            onToggleStatus = { viewModel.toggleUserStatus(user.id?: "") }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Renders an individual user item in the management list.
 *
 * @param user The user data to display.
 * @param onEdit Callback triggered when the edit button is clicked.
 * @param onToggleStatus Callback triggered when the activation toggle is clicked.
 */
@Composable
fun UserItem(
    user: UserResponseDto,
    onEdit: () -> Unit,
    onToggleStatus: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.name ?: "", style = MaterialTheme.typography.titleMedium)
                Text(text = user.email ?: "", style = MaterialTheme.typography.bodySmall)
                Text(text = "RUT: ${user.rut ?: ""}", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "Rol: ${user.role ?: "CLIENT"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onToggleStatus) {
                    val isActive = user.active ?: true
                    Icon(
                        imageVector = if (isActive) Icons.Default.Block else Icons.Default.CheckCircle,
                        contentDescription = if (isActive) "Banear" else "Activar",
                        tint = if (isActive) Color.Red else Color.Green
                    )
                }
            }
        }
    }
}
