package com.duoc.app.features.service.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.util.Objects

@Entity
@Table(name = "service_templates")
class ServiceTemplate(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    val id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    val category: Category,

    @Column(nullable = false)
    var name: String,

    @Column(name = "default_price", precision = 10, scale = 2)
    var defaultPrice: BigDecimal? = null,

    @Column(name = "default_duration")
    var defaultDuration: Int? = null,

    @Column(nullable = false)
    var active: Boolean = true
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ServiceTemplate) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = Objects.hash(id)

    override fun toString(): String = "ServiceTemplate(id=$id, name=$name)"
}
