package com.pointcheck.core.network.model

import com.google.gson.annotations.SerializedName

/**
 * Objeto de transferencia de datos (DTO) para representar errores devueltos por la API de Spring Boot.
 * Estructurado para capturar el formato estándar de error de Spring Security y controladores personalizados.
 * 
 * @property status Código de estado HTTP del error.
 * @property error Nombre corto del error o categoría.
 * @property message Descripción detallada del error para mostrar al usuario o para debugging.
 * @property timestamp Marca de tiempo en que ocurrió el error en el servidor.
 */
data class ApiErrorDto(
    @SerializedName("status") val status: Int,
    @SerializedName("error") val error: String,
    @SerializedName("message") val message: String,
    @SerializedName("timestamp") val timestamp: String? = null
)
