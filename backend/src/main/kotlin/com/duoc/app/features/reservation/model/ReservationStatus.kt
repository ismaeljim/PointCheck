package com.duoc.app.features.reservation.model

/**
 * Representa los estados legales de una reserva en el ecosistema PointCheck.
 */
enum class ReservationStatus {
    PENDING,    // Cita solicitada, pendiente de confirmación o atención.
    CONFIRMED,  // Cita confirmada por el especialista o en proceso de atención.
    CANCELLED,  // Cita anulada por cualquiera de las partes.
    COMPLETED   // Cita finalizada exitosamente con registro de atención.
}
