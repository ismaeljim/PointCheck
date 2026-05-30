package com.duoc.app.features.professionalprofile.model

import com.duoc.app.features.service.model.Category
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
    @GeneratedValue(strategy = GenerationType.UUID) // Cambio a UUID
    @Column(name = "id", updatable = false, nullable = false)
    val id: String? = null, //

    @OneToOne(fetch = FetchType.LAZY) // Es OneToOne con User
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: User, // Automáticamente usará el ID String de User

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    var category: Category? = null,

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
    var isVerified: Boolean = false,

    @Column(nullable = false)
    var rating: Float = 0.0f,

    @Column(name = "latitude")
    var latitude: Double? = null,

    @Column(name = "longitude")
    var longitude: Double? = null,

    @Column(name = "working_hours_json", columnDefinition = "TEXT")
    var workingHoursJson: String? = null,

    @Column(nullable = false)
    val defaultSessionDurationMinutes: Int = 60,

    @Column(nullable = false)
    val active: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)
