package com.pointcheck.features.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.features.admin.data.dto.AuditLogDto
import com.pointcheck.features.admin.data.repository.AdminRepository
import com.pointcheck.features.auth.data.dto.UserResponseDto
import com.pointcheck.core.util.MockDataProvider
import kotlinx.coroutines.CancellationException
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
    val auditPage: Int = 0,
    val isLastAuditPage: Boolean = false,
    val categories: List<com.pointcheck.features.onboarding.presentation.dto.CategoryDto> = emptyList(),
    val selectedUserForEdit: UserResponseDto? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
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
        loadCategories()
    }

    /**
     * Carga la lista de categorías disponibles para la edición de especialistas.
     */
    fun loadCategories() {
        viewModelScope.launch {
            try {
                // Reutilizamos el ApiClient para obtener categorías
                val api = ApiClient.retrofitInstance.create(com.pointcheck.features.onboarding.presentation.CategoryApi::class.java)
                val cats = api.getCategories()
                _state.update { it.copy(categories = cats) }
            } catch (e: Exception) {
                if (e is com.pointcheck.core.network.ApiException && (e.code == 401 || e.code == 403)) return@launch
                if (e is CancellationException) throw e
                println("Error loading categories for admin: ${e.message}")
            }
        }
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
                    // SPRINT 4 FIX: Eliminamos fallback a MockDataProvider para asegurar datos reales
                    _state.update { it.copy(users = users, filteredUsers = users, isLoading = false) }
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    if (e is com.pointcheck.core.network.ApiException && (e.code == 401 || e.code == 403)) return@onFailure
                    // SPRINT 4 FIX: Reportamos el error real en lugar de mostrar mocks
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    /**
     * Obtiene los registros de auditoría del sistema con soporte para paginación.
     */
    fun loadAuditLogs(isNextPage: Boolean = false) {
        if (_state.value.isLoading || (isNextPage && _state.value.isLastAuditPage)) return

        val nextPage = if (isNextPage) _state.value.auditPage + 1 else 0

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getAuditLogs(nextPage)
                .onSuccess { pageDto ->
                    _state.update { 
                        it.copy(
                            auditLogs = if (isNextPage) it.auditLogs + pageDto.content else pageDto.content,
                            auditPage = pageDto.number,
                            isLastAuditPage = pageDto.last,
                            isLoading = false
                        )
                    }
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    if (e is com.pointcheck.core.network.ApiException && e.code in listOf(401, 403)) return@onFailure
                    // SPRINT 4 FIX: Se elimina la inyección de MockDataProvider.mockAuditLogs
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Error al cargar logs") }
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
            _state.value.users.filter { user ->
                val name = user.name ?: ""
                val email = user.email ?: ""
                val rut = user.rut ?: ""
                name.contains(query, ignoreCase = true) || 
                email.contains(query, ignoreCase = true) ||
                rut.contains(query, ignoreCase = true)
            }
        }
        _state.update { it.copy(filteredUsers = filtered) }
    }

    /**
     * Selecciona un usuario para iniciar el proceso de edición.
     */
    fun selectUserForEdit(user: UserResponseDto?) {
        _state.update { it.copy(selectedUserForEdit = user) }
    }

    /**
     * Envía la solicitud de actualización de usuario al backend.
     */
    fun updateUser(userId: String, request: com.pointcheck.features.admin.data.dto.AdminUserUpdateRequestDto) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            repository.updateUser(userId, request)
                .onSuccess { updatedUser ->
                    val updatedList = _state.value.users.map {
                        if (it.id == updatedUser.id) updatedUser else it
                    }
                    _state.update { it.copy(
                        users = updatedList, 
                        isSaving = false, 
                        selectedUserForEdit = null 
                    ) }
                    filterUsers(_state.value.searchQuery)
                    loadAuditLogs() // Recargamos logs para ver el cambio reflejado
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    if (e is com.pointcheck.core.network.ApiException && e.code in listOf(401, 403)) return@onFailure
                    _state.update { it.copy(error = e.message, isSaving = false) }
                }
        }
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
                    if (e is CancellationException) throw e
                    if (e is com.pointcheck.core.network.ApiException && e.code in listOf(401, 403)) return@onFailure
                    _state.update { it.copy(error = e.message) }
                }
        }
    }

    /** Limpia el mensaje de error del estado. */
    fun clearError() = _state.update { it.copy(error = null) }
}
