package com.duoc.app.features.auth.service

import com.duoc.app.features.auth.dto.LoginRequest
import com.duoc.app.features.auth.dto.RegisterRequest
import com.duoc.app.features.user.dto.UserResponse
import com.duoc.app.features.user.model.User
import com.duoc.app.features.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository
) {

    fun register(request: RegisterRequest): UserResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("El email ya está registrado.")
        }

        // TODO: Implementar BCrypt para la contraseña
        val user = User(
            name = request.name,
            email = request.email,
            password = request.password,
            phone = request.phone,
            role = request.role
        )

        val savedUser = userRepository.save(user)
        return savedUser.toResponse()
    }

    fun login(request: LoginRequest): UserResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("Credenciales inválidas o usuario no encontrado.")

        // TODO: Usar BCrypt para comparar contraseñas
        if (user.password != request.password) {
            throw IllegalArgumentException("Credenciales inválidas o usuario no encontrado.")
        }

        if (!user.active) {
            throw IllegalArgumentException("La cuenta se encuentra desactivada.")
        }

        return user.toResponse()
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
