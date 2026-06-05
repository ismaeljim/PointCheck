package com.duoc.app.features.user.repository

import com.duoc.app.features.user.model.User
import com.duoc.app.features.user.model.UserRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repositorio para la gestión de persistencia de Usuarios.
 * Proporciona métodos de búsqueda por email, RUT y roles.
 */
@Repository
interface UserRepository : JpaRepository<User, String> {
    
    /**
     * Recupera un usuario por su email para procesos de login.
     */
    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u LEFT JOIN FETCH u.professionalProfile pp LEFT JOIN FETCH pp.category WHERE u.email = :email")
    fun findByEmailWithProfile(email: String): User?

    fun findByEmail(email: String): User?

    /**
     * Verifica si un email ya está en uso.
     */
    fun existsByEmail(email: String): Boolean

    /**
     * Verifica si un RUT ya está en uso.
     */
    fun existsByRut(rut: String): Boolean

    /**
     * Filtra usuarios por su rol (CLIENT o SPECIALIST).
     */
    fun findByRole(role: UserRole): List<User>

    /**
     * Cuenta la cantidad de usuarios registrados por rol para estadísticas.
     */
    fun countByRole(role: UserRole): Long
}
