package com.pointcheck.core.network.model

import com.google.gson.annotations.SerializedName

data class ApiErrorDto(
    @SerializedName("status") val status: Int,
    @SerializedName("error") val error: String,
    @SerializedName("message") val message: String,
    @SerializedName("timestamp") val timestamp: String? = null
)
