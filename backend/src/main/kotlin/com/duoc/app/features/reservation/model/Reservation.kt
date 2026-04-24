package com.duoc.app.features.reservation.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "reservations",
    indexes = [
        Index(name = "idx_reservations_client", columnList = "clientId"),
        Index(name = "idx_reservations_specialist", columnList = "specialistId"),
        Index(name = "idx_reservations_start", columnList = "reservationStart"),
        Index(name = "idx_reservations_status", columnList = "status"),
        Index(name = "idx_reservations_specialist_date", columnList = "specialistId,reservationStart"),
        Index(name = "idx_reservations_client_date", columnList = "clientId,reservationStart")
    ]
)
data class Reservation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val clientId: Long,

    @Column(nullable = false)
    val specialistId: Long,

    val serviceId: Long? = null,

    @Column(nullable = false)
    val reservationStart: LocalDateTime,

    val reservationEnd: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: ReservationStatus = ReservationStatus.PENDING,

    @Column(length = 1000)
    val notes: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)
