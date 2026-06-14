package com.pointcheck.features.admin.data.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para los registros de auditoría del sistema blindado contra nulos.
 * Se utilizan tipos nulables con valores por defecto para prevenir crashes
 * cuando Gson deserializa JSONs incompletos del backend.
 */
data class AuditLogDto(
    @SerializedName("id") val id: String? = "",
    @SerializedName("action") val action: String? = "",
    @SerializedName("performed_by_email") val performedByEmail: String? = "",
    @SerializedName("performed_by_name") val performedByName: String? = "",
    @SerializedName("target_type") val targetType: String? = "",
    @SerializedName("target_id") val targetId: String? = "",
    @SerializedName("target_name") val targetName: String? = "",
    @SerializedName("details") val details: String? = "",
    @SerializedName("ip_address") val ipAddress: String? = "",
    @SerializedName("timestamp") val timestamp: String? = ""
)
