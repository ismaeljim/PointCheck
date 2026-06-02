package com.pointcheck.features.admin.data.dto

import com.google.gson.annotations.SerializedName

data class AuditLogDto(
    @SerializedName("id") val id: String,
    @SerializedName("action") val action: String,
    @SerializedName("performedBy") val performedBy: String,
    @SerializedName("targetType") val targetType: String,
    @SerializedName("targetId") val targetId: String,
    @SerializedName("details") val details: String?,
    @SerializedName("timestamp") val timestamp: String
)
