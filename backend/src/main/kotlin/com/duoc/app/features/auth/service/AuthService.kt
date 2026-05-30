package com.duoc.app.features.auth.service

import com.duoc.app.core.util.RutUtils
import com.duoc.app.features.auth.dto.LoginRequest
import com.duoc.app.features.auth.dto.RegisterRequest
import com.duoc.app.features.service.repository.CategoryRepository
import com.duoc.app.features.user.dto.UserResponse
import com.duoc.app.features.user.model.User
import com.duoc.app.features.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val professionalProfileRepository: com.duoc.app.features.professionalprofile.repository.ProfessionalProfileRepository,
    private val categoryRepository: CategoryRepository
) {

    @org.springframework.transaction.annotation.Transactional
    fun register(request: RegisterRequest): UserResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("El email ya está registrado.")
        }

        val formattedRut = RutUtils.formatRut(request.rut)
        if (!RutUtils.validateRut(formattedRut)) {
            throw IllegalArgumentException("El RUT ingresado no es válido.")
        }

        if (userRepository.existsByRut(formattedRut)) {
            throw IllegalArgumentException("El RUT ya está registrado.")
        }

        // TODO: Implementar BCrypt para la contraseña
        val user = User(
            name = request.name,
            email = request.email,
            password = request.password,
            rut = formattedRut,
            phone = request.phone,
            role = request.role
        )

        val savedUser = userRepository.save(user)

        var assignedCategoryId: String? = null

        // Si es especialista, creamos su perfil automáticamente
        if (request.role == com.duoc.app.features.user.model.UserRole.SPECIALIST) {
            val category = request.categoryId?.let { 
                categoryRepository.findById(it).orElseThrow { 
                    IllegalArgumentException("La categoría especificada no existe.") 
                }
            }
            
            val profile = com.duoc.app.features.professionalprofile.model.ProfessionalProfile(
                user = savedUser,
                displayName = savedUser.name,
                category = category,
                city = request.city,
                address = request.address
            )
            val savedProfile = professionalProfileRepository.save(profile)
            assignedCategoryId = savedProfile.category?.id
        }

        return savedUser.toResponse(assignedCategoryId)
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

        val categoryId = if (user.role == com.duoc.app.features.user.model.UserRole.SPECIALIST) {
            professionalProfileRepository.findByUser_Id(user.id!!)?.category?.id
        } else null

        return user.toResponse(categoryId)
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
