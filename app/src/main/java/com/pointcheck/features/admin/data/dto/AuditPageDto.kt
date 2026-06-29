package com.pointcheck.features.admin.data.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para la respuesta paginada de logs de auditoría.
 */
data class AuditPageDto(
    @SerializedName("content") val content: List<AuditLogDto> = emptyList(),
    @SerializedName("totalPages") val totalPages: Int = 0,
    @SerializedName("totalElements") val totalElements: Long = 0L,
    @SerializedName("size") val size: Int = 0,
    @SerializedName("number") val number: Int = 0,
    @SerializedName("last") val last: Boolean = true
)
