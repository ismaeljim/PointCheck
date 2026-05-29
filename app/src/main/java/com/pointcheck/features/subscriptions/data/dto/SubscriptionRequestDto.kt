package com.pointcheck.features.subscriptions.data.dto

data class SubscriptionRequestDto(
    val professionalProfileId: String,
    val planName: String,
    val startDate: String,
    val endDate: String
)
