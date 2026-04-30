package com.pointcheck.features.subscriptions.data.dto

data class SubscriptionRequestDto(
    val professionalProfileId: Long,
    val planName: String,
    val startDate: String,
    val endDate: String
)
