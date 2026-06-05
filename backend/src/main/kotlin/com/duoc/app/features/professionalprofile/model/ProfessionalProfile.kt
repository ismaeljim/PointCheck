package com.duoc.app.features.professionalprofile.model

import com.duoc.app.features.service.model.Category
import com.duoc.app.features.user.model.User
import jakarta.persistence.*
import java.time.LocalDateTime

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
data class ProfessionalProfile(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    val id: String? = null,

    /**
     * Relación uno a uno con la entidad User.
     * AUDITORÍA: La carga es LAZY para optimizar el rendimiento cuando no se requiere la info del usuario.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: User,

    /**
     * Categoría principal del especialista (Barbería, Salud, etc.).
     */
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

    @Column(name = "is_verified", nullable = false, columnDefinition = "BIT(1) DEFAULT 0")
    var isVerified: Boolean = false,

    @Column(nullable = false, columnDefinition = "FLOAT DEFAULT 0.0")
    var rating: Float = 0.0f,

    /**
     * Coordenadas geográficas para integración con mapas.
     */
    @Column(name = "latitude")
    var latitude: Double? = null,

    @Column(name = "longitude")
    var longitude: Double? = null,

    /**
     * Almacena la disponibilidad semanal en formato JSON.
     * AUDITORÍA: Se recomienda validar el formato JSON en el servicio antes de persistir.
     */
    @Column(name = "working_hours_json", columnDefinition = "TEXT")
    var workingHoursJson: String? = null,

    @Column(nullable = false, columnDefinition = "INT DEFAULT 60")
    val defaultSessionDurationMinutes: Int = 60,

    @Column(nullable = false, columnDefinition = "BIT(1) DEFAULT 1")
    val active: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)
