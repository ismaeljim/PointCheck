package com.duoc.app.features.user.service

import com.duoc.app.features.professionalprofile.repository.ProfessionalProfileRepository
import com.duoc.app.features.user.dto.UserResponse
import com.duoc.app.features.user.model.User
import com.duoc.app.features.user.model.UserRole
import com.duoc.app.features.user.repository.UserRepository
import org.springframework.stereotype.Service

/**
 * Servicio para la gestión de datos de usuario.
 * Proporciona métodos de consulta y filtrado de usuarios y especialistas.
 */
@Service
class UserService(
    private val userRepository: UserRepository,
    private val professionalProfileRepository: ProfessionalProfileRepository
) {

    /**
     * Obtiene un usuario por su ID único.
     */
    fun getById(id: String): UserResponse {
        val user = userRepository.findById(id).orElseThrow {
            IllegalArgumentException("Usuario no encontrado con ID: $id")
        }
        val categoryId = professionalProfileRepository.findByUser_Id(user.id!!)?.category?.id
        return user.toResponse(categoryId)
    }

    /**
     * Obtiene un usuario por su correo electrónico.
     */
    fun getByEmail(email: String): UserResponse {
        val user = userRepository.findByEmail(email) ?: throw IllegalArgumentException("Usuario no encontrado con email: $email")
        val categoryId = professionalProfileRepository.findByUser_Id(user.id!!)?.category?.id
        return user.toResponse(categoryId)
    }

    /**
     * Lista todos los especialistas asociados a una categoría específica.
     */
    fun getSpecialistsByCategory(categoryId: String): List<UserResponse> {
        return professionalProfileRepository.findByCategoryIdAndActiveTrue(categoryId)
            .map { it.user.toResponse(categoryId) }
    }

    /**
     * Lista todos los usuarios con rol SPECIALIST.
     */
    fun getSpecialists(): List<UserResponse> {
        return userRepository.findByRole(UserRole.SPECIALIST)
            .map { user ->
                val categoryId = professionalProfileRepository.findByUser_Id(user.id!!)?.category?.id
                user.toResponse(categoryId)
            }
    }

    private fun User.toResponse(categoryId: String? = null): UserResponse = UserResponse(
        id = this.id!!,
        name = this.name,
        email = this.email,
        rut = this.rut,
        phone = this.phone,
        role = this.role,
        active = this.active,
        categoryId = categoryId
    )
}
