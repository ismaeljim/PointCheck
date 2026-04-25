package com.duoc.app.features.auth.dto

import com.duoc.app.features.user.model.UserRole

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    val role: UserRole = UserRole.CLIENT
)
