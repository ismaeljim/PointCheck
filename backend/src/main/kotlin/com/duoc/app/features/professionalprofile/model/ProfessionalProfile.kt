package com.duoc.app.features.professionalprofile.model

import com.duoc.app.features.user.model.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "professional_profiles",
    indexes = [
        Index(name = "idx_professional_profiles_user", columnList = "user_id"),
        Index(name = "idx_professional_profiles_active", columnList = "active")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_professional_profiles_user", columnNames = ["user_id"])
    ]
)
data class ProfessionalProfile(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: User,

    @Column(nullable = false, length = 150)
    val displayName: String,

    @Column(length = 150)
    val businessName: String? = null,

    @Column(length = 100)
    val specialty: String? = null,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(length = 255)
    val address: String? = null,

    @Column(length = 100)
    val city: String? = null,

    @Column(length = 100)
    val country: String? = null,

    @Column(nullable = false)
    val defaultSessionDurationMinutes: Int = 60,

    @Column(nullable = false)
    val active: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)
