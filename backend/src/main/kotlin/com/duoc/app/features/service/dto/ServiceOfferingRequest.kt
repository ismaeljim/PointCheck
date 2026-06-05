package com.duoc.app.features.service.dto

import com.duoc.app.features.service.model.PriceUnit
import java.math.BigDecimal

data class ServiceOfferingRequest(
    val professionalProfileId: String,
    val name: String,
    val description: String? = null,
    val price: BigDecimal? = null,
    val durationMinutes: Int? = null,
    val priceUnit: PriceUnit = PriceUnit.SESSION,
    val isAtHome: Boolean = false
)
