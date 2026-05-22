package com.duoc.app.features.service.model

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "service_templates")
data class ServiceTemplate(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    val category: Category,

    @Column(nullable = false)
    val name: String,

    @Column(name = "default_price", precision = 10, scale = 2)
    val defaultPrice: BigDecimal? = null,

    @Column(name = "default_duration")
    val defaultDuration: Int? = null,

    @Column(nullable = false)
    val active: Boolean = true
)
