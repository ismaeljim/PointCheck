package com.pointcheck.features.admin.data.dto

import com.google.gson.annotations.SerializedName

/**
 * Objeto de transferencia de datos (DTO) para los registros de auditoría del sistema.
 * Permite al administrador rastrear acciones críticas realizadas por los usuarios.
 * 
 * @property id Identificador único del registro de auditoría.
 * @property action Descripción de la acción realizada (ej: "USER_LOGIN", "RESERVATION_CANCELLED").
 * @property performedBy Nombre o identificación del usuario que realizó la acción.
 * @property targetType Tipo de entidad afectada (ej: "USER", "RESERVATION").
 * @property targetId ID de la entidad específica afectada.
 * @property details Información adicional o cambios realizados en formato texto.
 * @property timestamp Fecha y hora exacta de la acción.
 */
data class AuditLogDto(
    @SerializedName("id") val id: String,
    @SerializedName("action") val action: String,
    @SerializedName("performedBy") val performedBy: String,
    @SerializedName("targetType") val targetType: String,
    @SerializedName("targetId") val targetId: String,
    @SerializedName("details") val details: String?,
    @SerializedName("timestamp") val timestamp: String
)
