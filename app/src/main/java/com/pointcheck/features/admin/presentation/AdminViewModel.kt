package com.pointcheck.features.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.features.admin.data.dto.AuditLogDto
import com.pointcheck.features.admin.data.repository.AdminRepository
import com.pointcheck.features.auth.data.dto.UserResponseDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUiState(
    val users: List<UserResponseDto> = emptyList(),
    val filteredUsers: List<UserResponseDto> = emptyList(),
    val auditLogs: List<AuditLogDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

class AdminViewModel(
    private val repository: AdminRepository = AdminRepository(ApiClient.instance)
) : ViewModel() {

    private val _state = MutableStateFlow(AdminUiState())
    val state: StateFlow<AdminUiState> = _state.asStateFlow()

    init {
        loadUsers()
        loadAuditLogs()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getAllUsers()
                .onSuccess { users ->
                    _state.update { it.copy(users = users, filteredUsers = users, isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun loadAuditLogs() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getAuditLogs()
                .onSuccess { logs ->
                    _state.update { it.copy(auditLogs = logs, isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        filterUsers(query)
    }

    private fun filterUsers(query: String) {
        val filtered = if (query.isBlank()) {
            _state.value.users
        } else {
            _state.value.users.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.email.contains(query, ignoreCase = true) ||
                it.rut.contains(query)
            }
        }
        _state.update { it.copy(filteredUsers = filtered) }
    }

    fun toggleUserStatus(userId: String) {
        viewModelScope.launch {
            repository.toggleUserStatus(userId)
                .onSuccess { updatedUser ->
                    val updatedList = _state.value.users.map {
                        if (it.id == updatedUser.id) updatedUser else it
                    }
                    _state.update { it.copy(users = updatedList) }
                    filterUsers(_state.value.searchQuery)
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message) }
                }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}
