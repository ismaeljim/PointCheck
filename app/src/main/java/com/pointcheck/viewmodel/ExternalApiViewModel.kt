package com.pointcheck.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.data.network.NetworkRepository
import kotlinx.coroutines.launch

class ExternalApiViewModel : ViewModel() {
    private val networkRepository = NetworkRepository()
    val weatherState = mutableStateOf<String?>(null)

    fun fetchWeather(city: String) {
        viewModelScope.launch {
            try {
                val response = networkRepository.getWeather(city)
                if (response.isSuccessful) {
                    // Por ahora, guardamos la respuesta JSON completa como un String
                    weatherState.value = response.body()?.toString()
                } else {
                    weatherState.value = "Error al obtener el clima"
                }
            } catch (e: Exception) {
                weatherState.value = "Error de red"
            }
        }
    }
}
