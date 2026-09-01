package com.duoc.app.features.service.model

import com.duoc.app.features.professionalprofile.model.ProfessionalProfile
import jakarta.persistence.*
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Objects

@Entity
@Table(
    name = "services",
    indexes = [
        Index(name = "idx_services_professional_profile", columnList = "professional_profile_id"),
        Index(name = "idx_services_active", columnList = "active")
    ]
)
@SQLDelete(sql = "UPDATE services SET active = false WHERE id = ?")
@SQLRestriction("active = true")
class ServiceOffering(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    val id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_profile_id", nullable = false)
    val professionalProfile: ProfessionalProfile,

    @Column(nullable = false)
    var name: String,

    @Column(length = 500)
    var description: String? = null,

    @Column(precision = 10, scale = 2)
    var price: BigDecimal? = null,

    @Column(name = "duration_minutes")
    var durationMinutes: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "price_unit", nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'SESSION'")
    var priceUnit: PriceUnit = PriceUnit.SESSION,

    @Column(name = "is_at_home", nullable = false, columnDefinition = "BIT(1) DEFAULT 0")
    var isAtHome: Boolean = false,

    @Column(nullable = false, columnDefinition = "BIT(1) DEFAULT 1")
    var active: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ServiceOffering) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = Objects.hash(id)

    override fun toString(): String = "ServiceOffering(id=$id, name=$name)"
}
