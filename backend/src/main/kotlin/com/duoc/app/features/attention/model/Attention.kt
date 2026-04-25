package com.duoc.app.features.attention.model

import com.duoc.app.features.reservation.model.Reservation
import com.duoc.app.features.user.model.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "attentions",
    indexes = [
        Index(name = "idx_attentions_specialist_date", columnList = "specialist_id,startedAt"),
        Index(name = "idx_attentions_client", columnList = "client_id"),
        Index(name = "idx_attentions_status", columnList = "status")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_attentions_reservation", columnNames = ["reservation_id"])
    ]
)
data class Attention(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    val reservation: Reservation,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    val client: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialist_id", nullable = false)
    val specialist: User,

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
