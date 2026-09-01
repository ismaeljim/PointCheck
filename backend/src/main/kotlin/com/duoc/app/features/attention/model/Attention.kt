package com.duoc.app.features.attention.model

import com.duoc.app.features.professionalprofile.model.ProfessionalProfile
import com.duoc.app.features.reservation.model.Reservation
import com.duoc.app.features.user.model.User
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.Objects

@Entity
@Table(
    name = "attentions",
    indexes = [
        Index(name = "idx_attentions_specialist_profile_date", columnList = "specialist_profile_id,startedAt"),
        Index(name = "idx_attentions_client", columnList = "client_id"),
        Index(name = "idx_attentions_status", columnList = "status")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_attentions_reservation", columnNames = ["reservation_id"])
    ]
)
class Attention(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    val id: String? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    val reservation: Reservation,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    val client: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialist_profile_id", nullable = false)
    val specialist: ProfessionalProfile,

    @Column(nullable = false)
    var startedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "finished_at")
    var finishedAt: LocalDateTime? = null,

    @Column(name = "duration_minutes")
    var durationMinutes: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: AttentionStatus = AttentionStatus.IN_PROGRESS,

    @Column(length = 2000)
    var observations: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Attention) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = Objects.hash(id)

    override fun toString(): String = "Attention(id=$id, status=$status, startedAt=$startedAt)"
}
