package com.duoc.app.features.billing.model

import com.duoc.app.features.attention.model.Attention
import com.duoc.app.features.reservation.model.Reservation
import com.duoc.app.features.user.model.User
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Objects

@Entity
@Table(
    name = "billing_records",
    indexes = [
        Index(name = "idx_billing_reservation", columnList = "reservation_id"),
        Index(name = "idx_billing_attention", columnList = "attention_id"),
        Index(name = "idx_billing_client", columnList = "client_id"),
        Index(name = "idx_billing_specialist", columnList = "specialist_id"),
        Index(name = "idx_billing_specialist_date", columnList = "specialist_id,created_at"),
        Index(name = "idx_billing_status", columnList = "status"),
        Index(name = "idx_billing_paid_at", columnList = "paid_at")
    ]
)
class BillingRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    val id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    val reservation: Reservation,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attention_id")
    val attention: Attention? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    val client: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialist_id", nullable = false)
    val specialist: User,

    @Column(nullable = false, precision = 12, scale = 2)
    var amount: BigDecimal,

    @Column(nullable = false, length = 10)
    var currency: String = "CLP",

    @Enumerated(EnumType.STRING)
    var paymentMethod: PaymentMethod? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus = PaymentStatus.PENDING,

    @Column(name = "paid_at")
    var paidAt: LocalDateTime? = null,

    @Column(name = "external_reference")
    var externalReference: String? = null,

    @Column(length = 1000)
    var notes: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BillingRecord) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = Objects.hash(id)

    override fun toString(): String = "BillingRecord(id=$id, amount=$amount, status=$status)"
}
