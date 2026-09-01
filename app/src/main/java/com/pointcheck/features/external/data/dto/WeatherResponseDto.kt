package com.pointcheck.features.external.data.dto

import com.google.gson.annotations.SerializedName

/**
 * Objeto de transferencia de datos (DTO) que representa la información climática actual.
 * Proviene generalmente de una integración con servicios externos como OpenWeather.
 * 
 * @property main Contenedor de las métricas principales (temperatura, humedad).
 * @property weather Lista de descripciones climáticas y sus iconos correspondientes.
 * @property name Nombre de la ubicación geográfica (ciudad).
 */
data class WeatherResponseDto(
    val main: MainDto = MainDto(),
    val weather: List<WeatherDescriptionDto> = emptyList(),
    val name: String = "Ubicación desconocida"
)

data class MainDto(
    val temp: Double = 0.0,
    val humidity: Int = 0
)

data class WeatherDescriptionDto(
    val description: String = "",
    val icon: String = ""
)
