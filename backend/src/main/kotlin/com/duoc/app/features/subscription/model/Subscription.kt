package com.duoc.app.features.subscription.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "subscriptions",
    indexes = [
        Index(name = "idx_subscriptions_professional_profile", columnList = "professionalProfileId"),
        Index(name = "idx_subscriptions_status", columnList = "status"),
        Index(name = "idx_subscriptions_end_date", columnList = "endDate")
    ]
)
data class Subscription(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val professionalProfileId: Long,

    @Column(nullable = false)
    val planName: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,

    @Column(nullable = false)
    val startDate: LocalDate,

    @Column(nullable = false)
    val endDate: LocalDate,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)
