package com.pointcheck.features.dashboard.data.dto

/**
 * Objeto de transferencia de datos (DTO) que consolida métricas clave para el panel principal.
 * Contiene información relevante tanto para Clientes como para Especialistas, permitiendo una vista rápida del estado de sus actividades.
 * 
 * @property upcomingReservationsCount Número de reservas programadas a futuro.
 * @property recentReservationsCount Número de reservas realizadas recientemente.
 * @property lastReservationStatus Estado de la última reserva registrada.
 * @property appointmentsToday Citas programadas para el día de hoy (Especialista).
 * @property appointmentsMonth Total de citas gestionadas en el mes actual (Especialista).
 * @property totalAttentionsPerformed Acumulado histórico de atenciones completadas.
 * @property averageDurationMinutes Tiempo promedio de atención en minutos.
 * @property pendingBillingAmount Monto total pendiente por cobrar (Especialista).
 * @property paidBillingAmount Monto total ya recaudado (Especialista).
 * @property subscriptionStatus Estado de la suscripción profesional (ej: "ACTIVE", "EXPIRED").
 * @property subscriptionPlan Nombre del plan de suscripción contratado.
 * @property specialty Especialidad o categoría del profesional.
 */
data class DashboardMetricsDto(
    val upcomingReservationsCount: Int = 0,
    val recentReservationsCount: Int = 0,
    val lastReservationStatus: String? = null,
    val appointmentsToday: Int = 0,
    val appointmentsMonth: Int = 0,
    val totalAttentionsPerformed: Int = 0,
    val averageDurationMinutes: Double = 0.0,
    val pendingBillingAmount: Double = 0.0,
    val paidBillingAmount: Double = 0.0,
    val subscriptionStatus: String? = null,
    val subscriptionPlan: String? = null,
    val specialty: String? = null,
    val isProfileComplete: Boolean = true
)
