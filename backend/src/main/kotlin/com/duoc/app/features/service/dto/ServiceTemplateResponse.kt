package com.duoc.app.features.service.dto

import java.math.BigDecimal

data class ServiceTemplateResponse(
    val id: Long,
    val categoryId: Long,
    val name: String,
    val defaultPrice: BigDecimal?,
    val defaultDuration: Int?
)
