package com.pointcheck.features.dashboard.data.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para configuraciones globales blindado contra nulos de red.
 */
data class GlobalSettingDto(
    @SerializedName("id") val id: String? = "",
    @SerializedName("key") val key: String? = "",
    @SerializedName("value") var value: String? = "",
    @SerializedName("description") val description: String? = "",
    @SerializedName("updated_at") val updatedAt: String? = ""
)
