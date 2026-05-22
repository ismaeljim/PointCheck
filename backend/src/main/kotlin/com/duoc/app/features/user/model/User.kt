package com.duoc.app.features.user.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_users_role", columnList = "role"),
        Index(name = "idx_users_active", columnList = "active")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uk_users_email", columnNames = ["email"])
    ]
)
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false)
    val name: String,
    
    @Column(nullable = false)
    val email: String,
    
    @Column(nullable = false)
    val password: String,
    
    @Column(nullable = false, unique = true)
    val rut: String,
    
    val phone: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole = UserRole.CLIENT,
    
    @Column(nullable = false)
    val active: Boolean = true,
    
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)
