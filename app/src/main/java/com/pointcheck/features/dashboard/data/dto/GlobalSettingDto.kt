package com.pointcheck.features.dashboard.data.dto

data class GlobalSettingDto(
    val id: Long? = null,
    val key: String,
    var value: String,
    val description: String? = null,
    val updatedAt: String? = null
)
