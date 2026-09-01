package com.duoc.app.features.user.dto

import com.duoc.app.features.user.model.UserRole

data class UserSummaryDto(
    val id: String,
    val name: String,
    val rut: String,
    val role: UserRole,
    val email: String = "",
    val phone: String = ""
)
