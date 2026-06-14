package com.pointcheck.features.auth.data.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para la respuesta de usuario blindado contra nulos.
 * Se utilizan tipos nulables para prevenir crashes en tiempo de ejecución
 * causados por la deserialización de GSON desde el backend.
 */
data class UserResponseDto(
    @SerializedName("id") val id: String? = "",
    @SerializedName("token") val token: String? = "",
    @SerializedName("name") val name: String? = "",
    @SerializedName("email") val email: String? = "",
    @SerializedName("rut") val rut: String? = "",
    @SerializedName("phone") val phone: String? = "",
    @SerializedName("role") val role: String? = "",
    @SerializedName("active") val active: Boolean? = true,
    @SerializedName("address") val address: String? = "",
    @SerializedName("category_id") val categoryId: String? = ""
)
