package com.duoc.app.features.service.model

import jakarta.persistence.*
import java.time.LocalDateTime

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

    @Column(name = "icon_key", nullable = false, length = 100)
    val iconKey: String,

    @Column(name = "color_hex", nullable = false, length = 7)
    val colorHex: String,

    @Column(nullable = false)
    val active: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
