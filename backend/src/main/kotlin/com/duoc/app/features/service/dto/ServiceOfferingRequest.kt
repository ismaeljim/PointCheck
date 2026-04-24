package com.duoc.app.features.service.dto

import java.math.BigDecimal

data class ServiceOfferingRequest(
    val specialistId: Long,
    val name: String,
    val description: String? = null,
    val price: BigDecimal? = null,
    val durationMinutes: Int? = null
)
