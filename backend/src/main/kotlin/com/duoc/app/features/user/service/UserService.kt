package com.duoc.app.features.user.service

import com.duoc.app.features.professionalprofile.repository.ProfessionalProfileRepository
import com.duoc.app.features.user.dto.ChangePasswordRequest
import com.duoc.app.features.user.dto.UserResponse
import com.duoc.app.features.user.model.User
import com.duoc.app.features.user.model.UserRole
import com.duoc.app.features.user.repository.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

/**
 * Servicio para la gestión de datos de usuario.
 * Proporciona métodos de consulta y filtrado de usuarios y especialistas.
 */
@Service
class UserService(
    private val userRepository: UserRepository,
    private val professionalProfileRepository: ProfessionalProfileRepository,
    private val passwordEncoder: PasswordEncoder,
    private val auditLogger: com.duoc.app.core.audit.AuditLogger
) {

    /**
     * Valida que el usuario autenticado sea el dueño del ID que se intenta modificar
     * o que tenga privilegios de administrador.
     */
    private fun validateIdentity(targetId: String) {
        val auth = SecurityContextHolder.getContext().authentication
        
        if (auth == null || !auth.isAuthenticated || auth.name == "anonymousUser") {
            throw IllegalStateException("Acceso denegado: No hay una sesión activa")
        }

        val currentUser = userRepository.findByEmail(auth.name)
            ?: throw IllegalArgumentException("Usuario autenticado no encontrado")

        // El usuario solo puede modificarse a sí mismo, a menos que sea ADMIN
        if (currentUser.role != UserRole.ADMIN && currentUser.id != targetId) {
            auditLogger.log(
                action = "ACCESO_DENEGADO_IDOR",
                targetType = "USER",
                targetId = targetId,
                details = "El usuario '${currentUser.email}' intentó realizar una acción sobre el ID '$targetId' sin permisos."
            )
            throw SecurityException("No tienes permisos para realizar esta acción sobre este perfil.")
        }
    }

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

    /**
     * Actualiza los datos de perfil de un usuario.
     */
    fun updateProfile(id: String, request: com.duoc.app.features.user.dto.UserUpdateRequest): UserResponse {
        validateIdentity(id)
        val user = userRepository.findById(id).orElseThrow {
            IllegalArgumentException("Usuario no encontrado")
        }
        
        val oldName = user.name
        val oldPhone = user.phone
        val oldAddress = user.address

        user.name = request.name
        user.phone = request.phone
        user.address = request.address
        user.updatedAt = java.time.LocalDateTime.now()
        
        val savedUser = userRepository.save(user)
        
        // Registro de auditoría con detalles de cambios
        val details = mutableListOf<String>()
        if (oldName != request.name) details.add("Nombre: '$oldName' -> '${request.name}'")
        if (oldPhone != request.phone) details.add("Teléfono: '$oldPhone' -> '${request.phone}'")
        if (oldAddress != request.address) details.add("Dirección: '$oldAddress' -> '${request.address}'")
        
        auditLogger.log(
            action = "AUTO_EDICION",
            targetType = "USER",
            targetId = id,
            targetName = savedUser.name,
            details = if (details.isEmpty()) "Sin cambios detectados" else details.joinToString(", ")
        )
        
        val categoryId = professionalProfileRepository.findByUser_Id(savedUser.id!!)?.category?.id
        return savedUser.toResponse(categoryId)
    }

    /**
     * Actualiza la dirección de un usuario.
     */
    fun updateAddress(id: String, address: String): UserResponse {
        validateIdentity(id)
        val user = userRepository.findById(id).orElseThrow {
            IllegalArgumentException("Usuario no encontrado")
        }
        val oldAddress = user.address
        user.address = address
        user.updatedAt = java.time.LocalDateTime.now()
        val savedUser = userRepository.save(user)
        
        auditLogger.log(
            action = "ACTUALIZAR_DIRECCION",
            targetType = "USER",
            targetId = id,
            targetName = savedUser.name,
            details = "Dirección: '$oldAddress' -> '$address'"
        )

        val categoryId = professionalProfileRepository.findByUser_Id(savedUser.id!!)?.category?.id
        return savedUser.toResponse(categoryId)
    }

    /**
     * Cambia la contraseña del usuario validando la contraseña actual.
     */
    fun changePassword(id: String, request: ChangePasswordRequest) {
        validateIdentity(id)
        val user = userRepository.findById(id).orElseThrow {
            IllegalArgumentException("Usuario no encontrado")
        }

        if (!passwordEncoder.matches(request.currentPassword, user.password)) {
            throw IllegalArgumentException("La contraseña actual es incorrecta")
        }

        user.password = passwordEncoder.encode(request.newPassword)
        user.updatedAt = java.time.LocalDateTime.now()
        userRepository.save(user)

        auditLogger.log(
            action = "CAMBIO_PASSWORD",
            targetType = "USER",
            targetId = id,
            targetName = user.name,
            details = "El usuario cambió su contraseña exitosamente"
        )
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
