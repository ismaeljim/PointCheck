package com.duoc.app.features.billing.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(
    name = "billing_records",
    indexes = [
        Index(name = "idx_billing_reservation", columnList = "reservationId"),
        Index(name = "idx_billing_attention", columnList = "attentionId"),
        Index(name = "idx_billing_specialist_date", columnList = "specialistId,created_at"),
        Index(name = "idx_billing_status", columnList = "status"),
        Index(name = "idx_billing_paid_at", columnList = "paid_at")
    ]
)
data class BillingRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val reservationId: Long,

    val attentionId: Long? = null,

    @Column(nullable = false)
    val clientId: Long,

    @Column(nullable = false)
    val specialistId: Long,

    @Column(nullable = false, precision = 12, scale = 2)
    val amount: BigDecimal,

    @Column(nullable = false)
    val currency: String = "CLP",

    @Enumerated(EnumType.STRING)
    val paymentMethod: PaymentMethod? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: PaymentStatus = PaymentStatus.PENDING,

    @Column(name = "paid_at")
    val paidAt: LocalDateTime? = null,

    val externalReference: String? = null,

    @Column(length = 1000)
    val notes: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)
