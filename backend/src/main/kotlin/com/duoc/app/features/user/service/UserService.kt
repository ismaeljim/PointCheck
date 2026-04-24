package com.duoc.app.features.user.service

import com.duoc.app.features.user.dto.UserResponse
import com.duoc.app.features.user.model.User
import com.duoc.app.features.user.model.UserRole
import com.duoc.app.features.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun getById(id: Long): UserResponse {
        val user = userRepository.findById(id).orElseThrow {
            IllegalArgumentException("Usuario no encontrado con ID: $id")
        }
        return user.toResponse()
    }

    fun getByEmail(email: String): UserResponse {
        val user = userRepository.findByEmail(email) ?: throw IllegalArgumentException("Usuario no encontrado con email: $email")
        return user.toResponse()
    }

    fun getSpecialists(): List<UserResponse> {
        return userRepository.findByRole(UserRole.SPECIALIST).map { it.toResponse() }
    }

    private fun User.toResponse(): UserResponse = UserResponse(
        id = this.id,
        name = this.name,
        email = this.email,
        phone = this.phone,
        role = this.role,
        active = this.active
    )
}
