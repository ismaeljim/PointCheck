package com.duoc.app.features.subscription.dto

import com.duoc.app.features.subscription.model.SubscriptionStatus
import java.time.LocalDate
import java.time.LocalDateTime

data class SubscriptionResponse(
    val id: Long,
    val specialistId: Long,
    val planName: String,
    val status: SubscriptionStatus,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val createdAt: LocalDateTime
)
