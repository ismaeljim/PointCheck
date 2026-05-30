package com.duoc.app.features.user.dto

import com.duoc.app.features.user.model.UserRole

data class UserResponse(
    val id: String?,
    val name: String,
    val email: String,
    val rut: String,
    val phone: String,
    val role: UserRole,
    val active: Boolean,
    val categoryId: String? = null
)
