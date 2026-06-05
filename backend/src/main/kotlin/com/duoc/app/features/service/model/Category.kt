package com.duoc.app.features.service.model

import jakarta.persistence.*
import java.time.LocalDateTime

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
data class Category(
    @Id
    val id: String = java.util.UUID.randomUUID().toString(),

    @Column(nullable = false, length = 100)
    val name: String,

    /**
     * Referencia a un ícono en el frontend (ej: "ic_health", "ic_beauty").
     */
    @Column(name = "icon_key", nullable = false, length = 100)
    val iconKey: String,

    /**
     * Color representativo de la categoría para la UI en formato HEX.
     */
    @Column(name = "color_hex", nullable = false, length = 7)
    val colorHex: String,

    @Column(nullable = false, columnDefinition = "BIT(1) DEFAULT 1")
    val active: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
