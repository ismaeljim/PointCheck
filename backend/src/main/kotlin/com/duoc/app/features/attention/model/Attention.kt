package com.duoc.app.features.attention.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "attentions",
    indexes = [
        Index(name = "idx_attentions_specialist_date", columnList = "specialistId,startedAt"),
        Index(name = "idx_attentions_client", columnList = "clientId"),
        Index(name = "idx_attentions_status", columnList = "status")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_attentions_reservation", columnNames = ["reservationId"])
    ]
)
data class Attention(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val reservationId: Long,

    @Column(nullable = false)
    val clientId: Long,

    @Column(nullable = false)
    val specialistId: Long,

    @Column(nullable = false)
    val startedAt: LocalDateTime = LocalDateTime.now(),

    val finishedAt: LocalDateTime? = null,

    val durationMinutes: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: AttentionStatus = AttentionStatus.IN_PROGRESS,

    @Column(length = 2000)
    val observations: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)
