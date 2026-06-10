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

/**
 * Interfaz de Retrofit para acceder a los servicios de categorías y plantillas.
 */
interface CategoryApi {
    /** Obtiene el catálogo completo de categorías de servicio. */
    @GET("api/categories")
    suspend fun getCategories(): List<CategoryDto>

    /**
     * Obtiene las plantillas de servicios configuradas para una categoría específica.
     * @param categoryId ID de la categoría (UUID).
     */
    @GET("api/categories/{id}/templates")
    suspend fun getTemplates(@Path("id") categoryId: String): List<ServiceTemplateDto>
}

/**
 * Estado de la UI para el flujo de selección de categorías y servicios iniciales.
 *
 * @property categories Lista de categorías disponibles para el profesional.
 * @property templates Lista de plantillas de servicio según la categoría elegida.
 * @property isLoading Indica si los datos se están cargando desde el servidor.
 * @property error Mensaje de error en caso de fallo en la red.
 */
data class OnboardingState(
    val categories: List<CategoryDto> = emptyList(),
    val templates: List<ServiceTemplateDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel que gestiona la selección de categorías durante el registro o configuración inicial.
 * Facilita el acceso a plantillas de servicio para estandarizar la oferta de los profesionales.
 */
class CategoryViewModel : ViewModel() {
    private val api = ApiClient.retrofitInstance.create(CategoryApi::class.java)
    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state

    init {
        loadCategories()
    }

    /**
     * Carga de forma asíncrona la lista de categorías desde el API.
     */
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

    /**
     * Carga las plantillas de servicio sugeridas para una categoría.
     * @param categoryId Identificador de la categoría seleccionada.
     */
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
