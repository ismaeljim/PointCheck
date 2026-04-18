package com.pointcheck.features.external.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import kotlinx.coroutines.launch

class ExternalApiViewModel : ViewModel() {
    private val api = ApiClient.instance
    val weatherState = mutableStateOf<String?>(null)

    fun fetchWeather(city: String) {
        viewModelScope.launch {
            try {
                val response = api.getWeather(city)
                if (response.isSuccessful) {
                    weatherState.value = response.body()?.toString()
                } else {
                    weatherState.value = "Error al obtener el clima"
                }
            } catch (e: Exception) {
                weatherState.value = "Error de red: ${e.message}"
            }
        }
    }
}
