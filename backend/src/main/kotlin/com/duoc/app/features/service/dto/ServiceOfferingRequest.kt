package com.duoc.app.features.service.dto

import java.math.BigDecimal

data class ServiceOfferingRequest(
    val professionalProfileId: String,
    val name: String,
    val description: String? = null,
    val price: BigDecimal? = null,
    val durationMinutes: Int? = null
)
