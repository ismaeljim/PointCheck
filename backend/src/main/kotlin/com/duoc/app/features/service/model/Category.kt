package com.duoc.app.features.service.model

import jakarta.persistence.*
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime
import java.util.Objects

/**
 * Entidad que agrupa los servicios y especialistas en categorías de negocio.
 * (Ej: Salud, Belleza, Consultoría).
 */
@Entity
@Table(
    name = "categories",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_categories_name", columnNames = ["name"])
    ]
)
@SQLDelete(sql = "UPDATE categories SET active = false WHERE id = ?")
@SQLRestriction("active = true")
class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    val id: String? = null,

    @Column(nullable = false, length = 100)
    var name: String,

    /**
     * Referencia a un ícono en el frontend (ej: "ic_health", "ic_beauty").
     */
    @Column(name = "icon_key", nullable = false, length = 100)
    var iconKey: String,

    /**
     * Color representativo de la categoría para la UI en formato HEX.
     */
    @Column(name = "color_hex", nullable = false, length = 7)
    var colorHex: String,

    @Column(nullable = false, columnDefinition = "BIT(1) DEFAULT 1")
    var active: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Category) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = Objects.hash(id)

    override fun toString(): String = "Category(id=$id, name=$name)"
}
