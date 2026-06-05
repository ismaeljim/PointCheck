package com.duoc.app.features.service.dto

import java.math.BigDecimal

data class ServiceOfferingResponse(
    val id: String?,
    val professionalProfileId: String?,
    val name: String,
    val description: String?,
    val price: BigDecimal?,
    val durationMinutes: Int?,
    val priceUnit: String?,
    val isAtHome: Boolean,
    val active: Boolean
)
