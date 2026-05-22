package com.pointcheck.features.onboarding.presentation.dto

data class ServiceTemplateDto(
    val id: Long,
    val name: String,
    val description: String,
    val defaultPrice: Double,
    val unit: String
)
