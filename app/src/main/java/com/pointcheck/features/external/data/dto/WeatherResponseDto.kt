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
    val main: MainDto,
    val weather: List<WeatherDescriptionDto>,
    val name: String
)

/**
 * Datos numéricos principales del clima.
 * 
 * @property temp Temperatura actual en grados Celsius.
 * @property humidity Porcentaje de humedad relativa.
 */
data class MainDto(
    val temp: Double,
    val humidity: Int
)

/**
 * Descripción visual y textual de la condición climática.
 * 
 * @property description Breve descripción (ej: "cielo despejado").
 * @property icon Código del icono representativo para mostrar en la interfaz.
 */
data class WeatherDescriptionDto(
    val description: String,
    val icon: String
)
