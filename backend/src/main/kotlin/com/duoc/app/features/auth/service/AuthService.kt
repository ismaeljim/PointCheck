package com.duoc.app.features.auth.service

import com.duoc.app.core.util.RutUtils
import com.duoc.app.features.auth.dto.LoginRequest
import com.duoc.app.features.auth.dto.RegisterRequest
import com.duoc.app.features.service.repository.CategoryRepository
import com.duoc.app.features.user.dto.UserResponse
import com.duoc.app.features.user.model.User
import com.duoc.app.features.user.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

/**
 * Servicio encargado de la gestión de identidad y acceso.
 * Implementa la lógica de registro dual (Cliente/Especialista) y validación de credenciales.
 */
@Service
class AuthService(
    private val userRepository: UserRepository,
    private val professionalProfileRepository: com.duoc.app.features.professionalprofile.repository.ProfessionalProfileRepository,
    private val categoryRepository: CategoryRepository,
    private val passwordEncoder: PasswordEncoder
) {

    /**
     * Registra un nuevo usuario en el sistema.
     * Si el rol es SPECIALIST, crea automáticamente un perfil profesional vinculado.
     * 
     * AUDITORÍA: 
     * - Falta implementar hashing de contraseñas (BCrypt).
     * - Se debe agregar validación de longitud de teléfono.
     */
    @org.springframework.transaction.annotation.Transactional
    fun register(request: RegisterRequest): UserResponse {
        // Validación de unicidad de identidad
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("El email ya está registrado.")
        }

        // Validación y formateo de RUT chileno
        val formattedRut = RutUtils.formatRut(request.rut)
        if (!RutUtils.validateRut(formattedRut)) {
            throw IllegalArgumentException("El RUT ingresado no es válido.")
        }

        if (userRepository.existsByRut(formattedRut)) {
            throw IllegalArgumentException("El RUT ya está registrado.")
        }

        // Creación del objeto de dominio User con contraseña cifrada
        val user = User(
            name = request.name,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            rut = formattedRut,
            phone = request.phone,
            role = request.role
        )

        val savedUser = userRepository.save(user)

        var assignedCategoryId: String? = null

        // LÓGICA DE ESPECIALISTA: Automatiza la creación del perfil base
        if (request.role == com.duoc.app.features.user.model.UserRole.SPECIALIST) {
            val category = request.categoryId?.let { cid: String ->
                categoryRepository.findById(cid).orElseThrow {
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

    /**
     * Valida credenciales y retorna la información básica del usuario.
     * Incluye el categoryId si el usuario es un especialista para navegación directa en App.
     */
    fun login(request: LoginRequest): UserResponse {
        val cleanEmail = request.email.trim()
        val cleanPassword = request.password.trim() // Limpieza de seguridad
        
        println("AUTH-DEBUG: Intento de login para email: [$cleanEmail]")
        println("AUTH-DEBUG: Longitud password recibida: ${cleanPassword.length}")
        
        val user = userRepository.findByEmailWithProfile(cleanEmail)
        if (user == null) {
            println("AUTH-DEBUG: USUARIO NO ENCONTRADO en la DB: [$cleanEmail]")
            throw IllegalArgumentException("Usuario no encontrado: $cleanEmail")
        }

        println("AUTH-DEBUG: Usuario encontrado. Comparando password...")
        
        // Validación de seguridad mediante BCrypt
        if (!passwordEncoder.matches(cleanPassword, user.password)) {
            println("AUTH-DEBUG: PASSWORD INCORRECTO para [$cleanEmail]")
            throw IllegalArgumentException("Contraseña incorrecta para este usuario.")
        }

        if (!user.active) {
            println("AUTH-DEBUG: CUENTA DESACTIVADA para [${request.email}]")
            throw IllegalArgumentException("La cuenta se encuentra desactivada.")
        }

        println("AUTH-DEBUG: LOGIN EXITOSO para [${request.email}]")

        // OPTIMIZACIÓN SENIOR: Usamos la relación ya cargada por JOIN FETCH
        val categoryId = user.professionalProfile?.category?.id

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
