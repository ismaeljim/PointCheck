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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.scale
import com.pointcheck.core.ui.components.PointCheckTextField
import com.pointcheck.core.ui.components.PointCheckTopBar
import com.pointcheck.core.ui.components.PointCheckCard
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
    val state by viewModel.state.collectAsStateWithLifecycle()

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PointCheckTopBar(
                title = "PointCheck | Usuarios",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            PointCheckTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = "Buscar",
                placeholder = "Nombre, email o RUT",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
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
    PointCheckCard(
        title = user.name ?: "",
        subtitle = user.email ?: "",
        icon = if (user.role == "ADMIN") Icons.Default.CheckCircle else Icons.Default.Edit,
        onClick = onEdit
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "RUT: ${user.rut ?: ""}", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "Rol: ${user.role ?: "CLIENT"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isActive = user.active ?: true
                    Text(
                        text = if (isActive) "Activo" else "Inactivo",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isActive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Switch(
                        checked = isActive,
                        onCheckedChange = { onToggleStatus() },
                        modifier = Modifier.scale(0.85f),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF4CAF50),
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    }
}

// Extension to help with Switch scaling if needed, or just use standard Switch
// Assuming PointCheckCard is better used with its internal structure.
// Let's refine UserItem to use PointCheckCard correctly.

