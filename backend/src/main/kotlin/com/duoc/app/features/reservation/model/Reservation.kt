package com.duoc.app.features.reservation.model

import com.duoc.app.features.billing.model.PaymentMethod
import com.duoc.app.features.service.model.ServiceOffering
import com.duoc.app.features.user.model.User
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.Objects

@Entity
@Table(
    name = "reservations",
    indexes = [
        Index(name = "idx_reservations_client", columnList = "client_id"),
        Index(name = "idx_reservations_specialist", columnList = "specialist_id"),
        Index(name = "idx_reservations_service", columnList = "service_id"),
        Index(name = "idx_reservations_start", columnList = "reservationStart"),
        Index(name = "idx_reservations_status", columnList = "status"),
        Index(name = "idx_reservations_specialist_date", columnList = "specialist_id,reservationStart"),
        Index(name = "idx_reservations_client_date", columnList = "client_id,reservationStart")
    ]
)
class Reservation(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    val id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    val client: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialist_id", nullable = false)
    val specialist: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    val service: ServiceOffering? = null,

    @Column(nullable = false)
    var reservationStart: LocalDateTime,

    @Column(name = "reservation_end")
    var reservationEnd: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'PENDING'")
    var status: ReservationStatus = ReservationStatus.PENDING,

    @Column(length = 1000)
    var notes: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    var paymentMethod: PaymentMethod? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Reservation) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = Objects.hash(id)

    override fun toString(): String = "Reservation(id=$id, start=$reservationStart, status=$status)"
}
