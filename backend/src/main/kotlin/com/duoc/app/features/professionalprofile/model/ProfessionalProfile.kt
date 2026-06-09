package com.duoc.app.features.professionalprofile.model

import com.duoc.app.features.service.model.Category
import com.duoc.app.features.user.model.User
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.Objects

/**
 * Entidad que representa el perfil detallado de un especialista.
 * Extiende la información del usuario base para incluir detalles comerciales,
 * ubicación geográfica, especialidad y horarios de trabajo.
 */
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
class ProfessionalProfile(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    val id: String? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    var category: Category? = null,

    @Column(nullable = false, length = 150)
    var displayName: String,

    @Column(length = 150)
    var businessName: String? = null,

    @Column(length = 100)
    var specialty: String? = null,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(length = 255)
    var address: String? = null,

    @Column(length = 100)
    var city: String? = null,

    @Column(length = 100)
    var country: String? = null,

    @Column(name = "is_verified", nullable = false, columnDefinition = "BIT(1) DEFAULT 0")
    var isVerified: Boolean = false,

    @Column(nullable = false, columnDefinition = "FLOAT DEFAULT 0.0")
    var rating: Float = 0.0f,

    @Column(name = "latitude")
    var latitude: Double? = null,

    @Column(name = "longitude")
    var longitude: Double? = null,

    @Column(name = "working_hours_json", columnDefinition = "TEXT")
    var workingHoursJson: String? = null,

    @Column(nullable = false, columnDefinition = "INT DEFAULT 60")
    var defaultSessionDurationMinutes: Int = 60,

    @Column(nullable = false, columnDefinition = "BIT(1) DEFAULT 1")
    var active: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProfessionalProfile) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = Objects.hash(id)

    override fun toString(): String = "ProfessionalProfile(id=$id, displayName=$displayName)"
}
