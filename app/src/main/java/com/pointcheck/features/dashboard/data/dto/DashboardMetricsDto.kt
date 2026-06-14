package com.pointcheck.features.dashboard.data.dto

/**
 * Objeto de transferencia de datos (DTO) que consolida métricas clave para el panel principal.
 * Blindado contra nulos de red para evitar fallos de deserialización.
 */
data class DashboardMetricsDto(
    val upcomingReservationsCount: Int? = 0,
    val recentReservationsCount: Int? = 0,
    val lastReservationStatus: String? = "",
    val appointmentsToday: Int? = 0,
    val appointmentsMonth: Int? = 0,
    val totalAttentionsPerformed: Int? = 0,
    val averageDurationMinutes: Double? = 0.0,
    val pendingBillingAmount: Double? = 0.0,
    val paidBillingAmount: Double? = 0.0,
    val subscriptionStatus: String? = "",
    val subscriptionPlan: String? = "",
    val specialty: String? = "",
    val isProfileComplete: Boolean? = true,
    
    // Métricas para Administradores
    val totalUsers: Int? = 0,
    val totalRevenue: Double? = 0.0,
    val pendingRevenue: Double? = 0.0,
    val activeSpecialists: Int? = 0,
    val systemAlerts: Int? = 0,

    val revenueSeries: List<ChartDataDto>? = emptyList(),
    val activitySeries: List<ChartDataDto>? = emptyList()
)
