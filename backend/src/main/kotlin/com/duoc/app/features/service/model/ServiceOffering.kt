package com.duoc.app.features.service.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(
    name = "services",
    indexes = [
        Index(name = "idx_services_professional_profile", columnList = "professionalProfileId"),
        Index(name = "idx_services_active", columnList = "active")
    ]
)
data class ServiceOffering(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val professionalProfileId: Long,

    @Column(nullable = false)
    val name: String,

    @Column(length = 500)
    val description: String? = null,

    @Column(precision = 10, scale = 2)
    val price: BigDecimal? = null,

    val durationMinutes: Int? = null,

    val active: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)
