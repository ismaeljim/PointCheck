package com.duoc.app.features.dashboard.dto

data class DashboardMetricsResponse(
    // Métricas para Clientes
    val upcomingReservationsCount: Int = 0,
    val recentReservationsCount: Int = 0,
    val lastReservationStatus: String? = null,

    // Métricas para Profesionales
    val appointmentsToday: Int = 0,
    val appointmentsMonth: Int = 0,
    val totalAttentionsPerformed: Int = 0,
    val averageDurationMinutes: Double = 0.0,
    val pendingBillingAmount: Double = 0.0,
    val paidBillingAmount: Double = 0.0,
    val subscriptionStatus: String? = null,
    val subscriptionPlan: String? = null,
    val specialty: String? = null
)
