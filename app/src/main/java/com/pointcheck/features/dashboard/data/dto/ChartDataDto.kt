package com.pointcheck.features.dashboard.data.dto

/**
 * DTO para datos de gráficos blindado contra nulos.
 */
data class ChartDataDto(
    val label: String? = "",
    val value: Double? = 0.0
)
