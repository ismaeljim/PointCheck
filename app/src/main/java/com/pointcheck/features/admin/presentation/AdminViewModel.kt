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

/**
 * Representa el estado de la interfaz de usuario para el panel de administración.
 *
 * @property users Lista completa de usuarios registrados en el sistema.
 * @property filteredUsers Lista de usuarios que coinciden con los criterios de búsqueda actuales.
 * @property auditLogs Registro de auditoría con las acciones realizadas en el sistema.
 * @property isLoading Indica si se está realizando una operación de carga de datos.
 * @property error Mensaje de error a mostrar en caso de fallo en las operaciones.
 * @property searchQuery Cadena de texto utilizada para filtrar la lista de usuarios.
 */
data class AdminUiState(
    val users: List<UserResponseDto> = emptyList(),
    val filteredUsers: List<UserResponseDto> = emptyList(),
    val auditLogs: List<AuditLogDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

/**
 * ViewModel encargado de la lógica de negocio para el panel de administración.
 * Proporciona funcionalidades para la gestión de usuarios y visualización de registros de auditoría.
 *
 * @property repository Repositorio para acceder a los datos de administración y usuarios.
 */
class AdminViewModel(
    private val repository: AdminRepository = AdminRepository(ApiClient.instance)
) : ViewModel() {

    private val _state = MutableStateFlow(AdminUiState())
    val state: StateFlow<AdminUiState> = _state.asStateFlow()

    init {
        loadUsers()
        loadAuditLogs()
    }

    /**
     * Obtiene la lista completa de usuarios desde el repositorio.
     * Actualiza [AdminUiState.users] y [AdminUiState.filteredUsers] tras una carga exitosa.
     */
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

    /**
     * Obtiene los registros de auditoría del sistema.
     * Estos registros incluyen acciones críticas realizadas por usuarios y administradores.
     */
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

    /**
     * Actualiza la consulta de búsqueda y filtra la lista de usuarios.
     *
     * @param query Texto a buscar en el nombre, email o RUT de los usuarios.
     */
    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        filterUsers(query)
    }

    /**
     * Filtra localmente la lista de usuarios basada en una cadena de búsqueda.
     *
     * @param query Término de búsqueda.
     */
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

    /**
     * Cambia el estado de activación de un usuario (activar/desactivar).
     *
     * @param userId Identificador único (UUID) del usuario.
     */
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

    /** Limpia el mensaje de error del estado. */
    fun clearError() = _state.update { it.copy(error = null) }
}
