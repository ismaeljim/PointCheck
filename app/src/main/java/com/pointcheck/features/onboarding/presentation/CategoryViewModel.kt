package com.pointcheck.features.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.features.onboarding.presentation.dto.CategoryDto
import com.pointcheck.features.onboarding.presentation.dto.ServiceTemplateDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CategoryApi {
    @GET("api/categories")
    suspend fun getCategories(): List<CategoryDto>

    @GET("api/categories/{id}/templates")
    suspend fun getTemplates(@Path("id") categoryId: String): List<ServiceTemplateDto>
}

data class OnboardingState(
    val categories: List<CategoryDto> = emptyList(),
    val templates: List<ServiceTemplateDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class CategoryViewModel : ViewModel() {
    private val api = ApiClient.retrofitInstance.create(CategoryApi::class.java)
    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val cats = api.getCategories()
                _state.value = _state.value.copy(categories = cats, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun loadTemplates(categoryId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val temps = api.getTemplates(categoryId)
                _state.value = _state.value.copy(templates = temps, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message, isLoading = false)
            }
        }
    }
}
