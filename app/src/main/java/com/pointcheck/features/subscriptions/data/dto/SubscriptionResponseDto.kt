package com.pointcheck.features.subscriptions.data.dto

data class SubscriptionResponseDto(
    val id: String,
    val professionalProfileId: String,
    val planName: String,
    val status: String,
    val startDate: String,
    val endDate: String,
    val createdAt: String,
    val updatedAt: String?
)
