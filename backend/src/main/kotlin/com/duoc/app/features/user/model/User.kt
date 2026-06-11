package com.duoc.app.features.user.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.Objects

/**
 * Entidad que representa a un usuario en el sistema.
 * Soporta autenticación y diferenciación de roles (CLIENT/SPECIALIST).
 */
@Entity
@Table(
    name = "users",
    // AUDITORÍA: Los índices ayudan en búsquedas frecuentes por rol y estado de actividad.
    indexes = [
        Index(name = "idx_users_role", columnList = "role"),
        Index(name = "idx_users_active", columnList = "active")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_users_email", columnNames = ["email"]),
        UniqueConstraint(name = "uk_users_rut", columnNames = ["rut"])
    ]
)
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    val id: String? = null,

    @Column(nullable = false)
    var name: String,
    
    @Column(nullable = false)
    var email: String,
    
    @Column(nullable = false)
    var password: String,
    
    @Column(nullable = false, unique = true)
    var rut: String,
    
    @Column(nullable = false)
    var phone: String,
    
    @Column(nullable = true)
    var address: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'CLIENT'")
    var role: UserRole = UserRole.CLIENT,
    
    @Column(nullable = false, columnDefinition = "BIT(1) DEFAULT 1")
    var active: Boolean = true,
    
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
) {
    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var professionalProfile: com.duoc.app.features.professionalprofile.model.ProfessionalProfile? = null

    fun toSummaryDto() = com.duoc.app.features.user.dto.UserSummaryDto(
        id = this.id!!,
        name = this.name,
        rut = this.rut,
        role = this.role
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = Objects.hash(id)

    override fun toString(): String = "User(id=$id, email=$email, role=$role)"
}
