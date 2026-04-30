package com.pointcheck.features.subscriptions.data.dto

data class SubscriptionResponseDto(
    val id: Long,
    val professionalProfileId: Long,
    val planName: String,
    val status: String,
    val startDate: String,
    val endDate: String
)
