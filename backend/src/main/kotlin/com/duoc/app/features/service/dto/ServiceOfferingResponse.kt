package com.duoc.app.features.service.dto

import java.math.BigDecimal

data class ServiceOfferingResponse(
    val id: Long,
    val professionalProfileId: Long,
    val name: String,
    val description: String?,
    val price: BigDecimal?,
    val durationMinutes: Int?,
    val active: Boolean
)
