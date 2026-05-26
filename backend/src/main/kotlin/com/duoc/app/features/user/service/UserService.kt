package com.duoc.app.features.user.service

import com.duoc.app.features.professionalprofile.repository.ProfessionalProfileRepository
import com.duoc.app.features.user.dto.UserResponse
import com.duoc.app.features.user.model.User
import com.duoc.app.features.user.model.UserRole
import com.duoc.app.features.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val professionalProfileRepository: ProfessionalProfileRepository
) {

    fun getById(id: Long): UserResponse {
        val user = userRepository.findById(id).orElseThrow {
            IllegalArgumentException("Usuario no encontrado con ID: $id")
        }
        val categoryId = professionalProfileRepository.findByUser_Id(user.id)?.category?.id
        return user.toResponse(categoryId)
    }

    fun getByEmail(email: String): UserResponse {
        val user = userRepository.findByEmail(email) ?: throw IllegalArgumentException("Usuario no encontrado con email: $email")
        val categoryId = professionalProfileRepository.findByUser_Id(user.id)?.category?.id
        return user.toResponse(categoryId)
    }

    fun getSpecialistsByCategory(categoryId: Long): List<UserResponse> {
        return professionalProfileRepository.findByCategoryIdAndActiveTrue(categoryId)
            .map { it.user.toResponse(categoryId) }
    }

    fun getSpecialists(): List<UserResponse> {
        return userRepository.findByRole(UserRole.SPECIALIST)
            .map { user ->
                val categoryId = professionalProfileRepository.findByUser_Id(user.id)?.category?.id
                user.toResponse(categoryId)
            }
    }

    private fun User.toResponse(categoryId: Long? = null): UserResponse = UserResponse(
        id = this.id,
        name = this.name,
        email = this.email,
        rut = this.rut,
        phone = this.phone,
        role = this.role,
        active = this.active,
        categoryId = categoryId
    )
}
