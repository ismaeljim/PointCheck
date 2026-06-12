package com.pointcheck.features.admin.data.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para los registros de auditoría del sistema.
 * 
 * Basado en la nueva estructura de trazabilidad que separa el nombre y email del actor,
 * y proporciona detalles legibles sobre el objetivo de la acción.
 */
data class AuditLogDto(
    @SerializedName("id") val id: String,
    @SerializedName("action") val action: String, // ej: ACCESO, CREAR, EDITAR, ELIMINAR
    @SerializedName("performedByEmail") val performedByEmail: String,
    @SerializedName("performedByName") val performedByName: String,
    @SerializedName("targetType") val targetType: String, // ej: Usuario, Servicio, Cliente
    @SerializedName("targetId") val targetId: String,
    @SerializedName("targetName") val targetName: String?, // ej: Nombre del cliente o servicio
    @SerializedName("details") val details: String?, // ej: "Nombre: OLD -> NEW"
    @SerializedName("ipAddress") val ipAddress: String?,
    @SerializedName("timestamp") val timestamp: String
)
