package com.pointcheck.features.admin.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
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
import com.pointcheck.features.auth.data.dto.UserResponseDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Usuarios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        // Icono de volver
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por nombre, email o RUT") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
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
                            onToggleStatus = { viewModel.toggleUserStatus(user.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserItem(user: UserResponseDto, onToggleStatus: () -> Unit) {
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
                Text(text = user.name, style = MaterialTheme.typography.titleMedium)
                Text(text = user.email, style = MaterialTheme.typography.bodySmall)
                Text(text = "RUT: ${user.rut}", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "Rol: ${user.role}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onToggleStatus) {
                Icon(
                    imageVector = if (user.active) Icons.Default.Block else Icons.Default.CheckCircle,
                    contentDescription = if (user.active) "Banear" else "Activar",
                    tint = if (user.active) Color.Red else Color.Green
                )
            }
        }
    }
}
