package com.pointcheck.features.auth.data

data class User(
    val email: String,
    val name: String,
    val password: String = ""
)

