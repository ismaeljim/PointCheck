package com.duoc.app.features.subscription.model

import com.duoc.app.features.professionalprofile.model.ProfessionalProfile
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Objects

@Entity
@Table(
    name = "subscriptions",
    indexes = [
        Index(name = "idx_subscriptions_professional_profile", columnList = "professional_profile_id"),
        Index(name = "idx_subscriptions_status", columnList = "status"),
        Index(name = "idx_subscriptions_end_date", columnList = "endDate")
    ]
)
class Subscription(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    val id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_profile_id", nullable = false)
    val professionalProfile: ProfessionalProfile,

    @Column(nullable = false)
    var planName: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: SubscriptionStatus = SubscriptionStatus.ACTIVE,

    @Column(nullable = false)
    var startDate: LocalDate,

    @Column(nullable = false)
    var endDate: LocalDate,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Subscription) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = Objects.hash(id)

    override fun toString(): String = "Subscription(id=$id, planName=$planName, status=$status)"
}
