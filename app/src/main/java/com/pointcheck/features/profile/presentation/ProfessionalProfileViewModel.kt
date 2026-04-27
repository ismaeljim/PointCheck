package com.pointcheck.features.profile.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.features.profile.data.dto.ProfessionalProfileRequestDto
import com.pointcheck.features.profile.data.dto.ProfessionalProfileResponseDto
import com.pointcheck.features.profile.data.repository.ProfessionalProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfessionalProfileUiState(
    val profile: ProfessionalProfileResponseDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val isEditing: Boolean = false
)

class ProfessionalProfileViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = ProfessionalProfileRepository(ApiClient.instance)
    private val prefs = UserPreferences(application)
    
    private val _state = MutableStateFlow(ProfessionalProfileUiState())
    val state: StateFlow<ProfessionalProfileUiState> = _state

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            val userId = prefs.userId.first() ?: return@launch
            _state.update { it.copy(isLoading = true, error = null) }
            
            repository.getProfileByUserId(userId)
                .onSuccess { profile ->
                    _state.update { it.copy(profile = profile, isLoading = false) }
                    // Guardar el profileId para futuras referencias (reservas, servicios)
                    prefs.saveProfessionalProfileId(profile.id)
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false) } // No ponemos error porque puede que no tenga perfil aún
                }
        }
    }

    fun saveProfile(
        displayName: String,
        businessName: String,
        specialty: String,
        description: String,
        address: String,
        city: String,
        duration: Int
    ) {
        viewModelScope.launch {
            val userId = prefs.userId.first() ?: return@launch
            _state.update { it.copy(isLoading = true, error = null) }

            val request = ProfessionalProfileRequestDto(
                userId = userId,
                displayName = displayName,
                businessName = businessName,
                specialty = specialty,
                description = description,
                address = address,
                city = city,
                defaultSessionDurationMinutes = duration
            )

            val currentProfile = _state.value.profile
            val result = if (currentProfile == null) {
                repository.createProfile(request)
            } else {
                repository.updateProfile(currentProfile.id, request)
            }

            result.onSuccess { updated ->
                _state.update { it.copy(profile = updated, isLoading = false, successMessage = "Perfil guardado con éxito", isEditing = false) }
                prefs.saveProfessionalProfileId(updated.id)
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = "Error al guardar perfil: ${e.message}") }
            }
        }
    }

    fun toggleEdit() {
        _state.update { it.copy(isEditing = !it.isEditing) }
    }
}
