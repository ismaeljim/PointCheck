package com.duoc.app.features.subscription.dto

import java.time.LocalDate

data class SubscriptionRequest(
    val professionalProfileId: Long,
    val planName: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)
