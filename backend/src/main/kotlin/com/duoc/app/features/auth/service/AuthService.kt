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
 * Servicio encargado de la gestión de identidad, autenticación y registro de usuarios.
 *
 * Implementa la lógica de "Registro Dual", permitiendo la creación de cuentas tanto para
 * Clientes como para Especialistas. En el caso de los especialistas, automatiza la
 * creación de su perfil profesional base y la asociación con una categoría de servicio.
 *
 * @property userRepository Repositorio para la persistencia de usuarios.
 * @property professionalProfileRepository Repositorio para la gestión de perfiles de especialistas.
 * @property categoryRepository Repositorio para validar y asignar categorías de servicio.
 * @property passwordEncoder Componente de seguridad para el cifrado y validación de contraseñas.
 */
@Service
class AuthService(
    private val userRepository: UserRepository,
    private val professionalProfileRepository: com.duoc.app.features.professionalprofile.repository.ProfessionalProfileRepository,
    private val categoryRepository: CategoryRepository,
    private val passwordEncoder: PasswordEncoder,
    private val auditLogger: com.duoc.app.core.audit.AuditLogger
) {

    /**
     * Registra un nuevo usuario en la plataforma PointCheck.
     *
     * Realiza validaciones críticas de unicidad (email y RUT) y formatea el RUT chileno
     * antes de la persistencia. Si el rol solicitado es `SPECIALIST`, se crea de forma
     * transaccional un `ProfessionalProfile` asociado, asegurando la integridad de los datos.
     *
     * @param request DTO con los datos de registro (nombre, email, password, RUT, teléfono, rol, etc.).
     * @return [UserResponse] con los datos del usuario creado, incluyendo el ID de categoría si aplica.
     * @throws IllegalArgumentException Si el email/RUT ya existen, el RUT es inválido o la categoría no existe.
     */
    @org.springframework.transaction.annotation.Transactional
    fun register(request: RegisterRequest): UserResponse {
        // Validación de unicidad de identidad
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("El email ya está registrado.")
        }

        // Validación y formateo de RUT chileno mediante utilidad centralizada
        val formattedRut = RutUtils.formatRut(request.rut)
        if (!RutUtils.validateRut(formattedRut)) {
            throw IllegalArgumentException("El RUT ingresado no es válido.")
        }

        if (userRepository.existsByRut(formattedRut)) {
            throw IllegalArgumentException("El RUT ya está registrado.")
        }

        // Creación del objeto de dominio User con contraseña cifrada (BCrypt)
        val user = User(
            name = request.name,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            rut = formattedRut,
            phone = request.phone,
            role = request.role
        )

        val savedUser = userRepository.save(user)

        auditLogger.log(
            action = "CREAR",
            targetType = "Usuario",
            targetId = savedUser.id!!,
            targetName = savedUser.name,
            details = "Registro inicial de usuario con rol ${savedUser.role}",
            performedByEmail = savedUser.email,
            performedByName = savedUser.name
        )

        var assignedCategoryId: String? = null

        // LÓGICA DE ESPECIALISTA: Automatiza la creación del perfil profesional base
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
     * Autentica a un usuario verificando sus credenciales contra la base de datos.
     *
     * Incluye lógica de limpieza de entrada (trim) y validación de estado de cuenta (active).
     * Utiliza `findByEmailWithProfile` para cargar el perfil profesional en una sola consulta,
     * optimizando la respuesta para la navegación inicial de la App.
     *
     * @param request DTO con email y contraseña.
     * @return [UserResponse] con la información del usuario y su contexto profesional (si es especialista).
     * @throws IllegalArgumentException Si el usuario no existe, la contraseña es incorrecta o la cuenta está desactivada.
     */
    fun login(request: LoginRequest): UserResponse {
        val cleanEmail = request.email.trim()
        val cleanPassword = request.password.trim()
        
        println("AUTH-DEBUG: Intento de login para email: [$cleanEmail]")
        
        // Se utiliza EntityGraph o Fetch Join internamente para evitar N+1
        val user = userRepository.findByEmailWithProfile(cleanEmail)
        if (user == null) {
            println("AUTH-DEBUG: USUARIO NO ENCONTRADO en la DB: [$cleanEmail]")
            throw IllegalArgumentException("Usuario no encontrado: $cleanEmail")
        }

        // Validación de seguridad mediante PasswordEncoder (BCrypt)
        if (!passwordEncoder.matches(cleanPassword, user.password)) {
            println("AUTH-DEBUG: PASSWORD INCORRECTO para [$cleanEmail]")
            throw IllegalArgumentException("Contraseña incorrecta para este usuario.")
        }

        if (!user.active) {
            println("AUTH-DEBUG: CUENTA DESACTIVADA para [${request.email}]")
            throw IllegalArgumentException("La cuenta se encuentra desactivada.")
        }

        println("AUTH-DEBUG: LOGIN EXITOSO para [${request.email}]")

        // Obtención eficiente del categoryId desde la relación ya cargada
        val categoryId = user.professionalProfile?.category?.id

        return user.toResponse(categoryId)
    }

    /**
     * Mapea una entidad [User] a su representación de respuesta [UserResponse].
     * 
     * @param categoryId ID opcional de la categoría si el usuario es un profesional.
     */
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
