package com.duoc.app.features.professionalprofile.service

import com.duoc.app.features.professionalprofile.dto.ProfessionalProfileRequest
import com.duoc.app.features.professionalprofile.dto.ProfessionalProfileResponse
import com.duoc.app.features.professionalprofile.model.ProfessionalProfile
import com.duoc.app.features.professionalprofile.repository.ProfessionalProfileRepository
import com.duoc.app.features.service.repository.CategoryRepository
import com.duoc.app.features.user.model.UserRole
import com.duoc.app.features.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Servicio de lógica de negocio encargado de la gestión integral de Perfiles Profesionales.
 *
 * El perfil profesional es la extensión del usuario con rol `SPECIALIST` que contiene
 * su información comercial, ubicación geográfica, especialidad técnica y configuración
 * de agenda (horarios de trabajo y duración de sesiones por defecto).
 *
 * @property professionalProfileRepository Repositorio para la persistencia de perfiles.
 * @property userRepository Repositorio para validar la existencia y rol de los usuarios.
 * @property categoryRepository Repositorio para la asociación de perfiles con categorías de servicio.
 */
@Service
class ProfessionalProfileService(
    private val professionalProfileRepository: ProfessionalProfileRepository,
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository
) {

    /**
     * Crea un nuevo perfil profesional vinculado a un usuario.
     *
     * Valida rigurosamente que el usuario tenga el rol `SPECIALIST` y que no posea un perfil
     * previo, garantizando la relación 1:1 entre Usuario y Perfil Profesional.
     *
     * @param request DTO con los datos del perfil (nombre comercial, especialidad, ubicación, etc.).
     * @return [ProfessionalProfileResponse] con la información del perfil persistido.
     * @throws IllegalArgumentException Si el usuario no es especialista, no existe o ya posee un perfil.
     */
    @Transactional
    fun create(request: ProfessionalProfileRequest): ProfessionalProfileResponse {
        val user = userRepository.findById(request.userId).orElseThrow {
            IllegalArgumentException("Usuario no encontrado")
        }

        if (user.role != UserRole.SPECIALIST) {
            throw IllegalArgumentException("Solo un especialista puede tener perfil profesional")
        }

        if (professionalProfileRepository.existsByUser_Id(request.userId)) {
            throw IllegalArgumentException("El especialista ya tiene un perfil profesional")
        }

        val category = request.categoryId?.let { 
            categoryRepository.findById(it).orElse(null)
        }

        val profile = ProfessionalProfile(
            user = user,
            category = category,
            displayName = request.displayName,
            businessName = request.businessName,
            specialty = request.specialty,
            description = request.description,
            address = request.address,
            city = request.city,
            country = request.country,
            latitude = request.latitude,
            longitude = request.longitude,
            defaultSessionDurationMinutes = request.defaultSessionDurationMinutes,
            workingHoursJson = request.workingHoursJson,
            active = true
        )

        return professionalProfileRepository.save(profile).toResponse()
    }

    /**
     * Recupera una lista de perfiles profesionales activos, permitiendo filtrado por categoría.
     *
     * Esta función es clave para el buscador de la aplicación móvil, permitiendo a los clientes
     * encontrar especialistas según su rubro.
     *
     * @param categoryId ID opcional de la categoría para filtrar resultados.
     * @return Lista de [ProfessionalProfileResponse] que cumplen con el criterio.
     */
    fun getActive(categoryId: String? = null): List<ProfessionalProfileResponse> {
        return if (categoryId != null) {
            professionalProfileRepository.findByCategoryIdAndActiveTrue(categoryId).map { it.toResponse() }
        } else {
            professionalProfileRepository.findByActiveTrue().map { it.toResponse() }
        }
    }

    /**
     * Obtiene el perfil profesional asociado a un identificador de usuario único.
     *
     * @param userId ID del usuario (especialista) cuyo perfil se requiere.
     * @return [ProfessionalProfileResponse] correspondiente al usuario.
     * @throws NoSuchElementException Si el usuario no tiene un perfil configurado.
     */
    fun getByUserId(userId: String): ProfessionalProfileResponse {
        val profile = professionalProfileRepository.findByUser_Id(userId)
            ?: throw NoSuchElementException("Perfil profesional no encontrado para el usuario $userId")
        return profile.toResponse()
    }

    /**
     * Actualiza la información detallada de un perfil profesional existente.
     *
     * Permite la modificación de campos dinámicos como `workingHoursJson` (Agenda) y
     * `defaultSessionDurationMinutes`, que impactan directamente en el motor de reservas.
     *
     * @param id ID único del perfil profesional (GUID).
     * @param request DTO con la información actualizada.
     * @return [ProfessionalProfileResponse] con los cambios aplicados y fecha de actualización.
     * @throws NoSuchElementException Si el perfil especificado no existe.
     */
    @Transactional
    fun update(id: String, request: ProfessionalProfileRequest): ProfessionalProfileResponse {
        val profile = professionalProfileRepository.findById(id).orElseThrow {
            NoSuchElementException("Perfil profesional no encontrado con id $id")
        }

        val category = request.categoryId?.let { 
            categoryRepository.findById(it).orElse(null)
        }

        profile.apply {
            this.category = category
            displayName = request.displayName
            businessName = request.businessName
            specialty = request.specialty
            description = request.description
            address = request.address
            city = request.city
            country = request.country
            latitude = request.latitude
            longitude = request.longitude
            defaultSessionDurationMinutes = request.defaultSessionDurationMinutes
            workingHoursJson = request.workingHoursJson
            updatedAt = LocalDateTime.now()
        }

        return professionalProfileRepository.save(profile).toResponse()
    }

    /**
     * Desactiva lógicamente un perfil profesional del sistema.
     *
     * Realiza un "Soft-Delete" para preservar la integridad referencial en reservas
     * históricas y registros financieros, pero oculta al profesional de nuevas búsquedas.
     *
     * @param id ID del perfil a desactivar.
     * @return Perfil actualizado con estado inactivo.
     * @throws NoSuchElementException Si el perfil no existe.
     */
    @Transactional
    fun deactivate(id: String): ProfessionalProfileResponse {
        val profile = professionalProfileRepository.findById(id).orElseThrow {
            NoSuchElementException("Perfil profesional no encontrado con id $id")
        }

        profile.apply {
            active = false
            updatedAt = LocalDateTime.now()
        }

        return professionalProfileRepository.save(profile).toResponse()
    }

    private fun ProfessionalProfile.toResponse(): ProfessionalProfileResponse {
        return ProfessionalProfileResponse(
            id = this.id!!,
            userId = this.user.id!!,
            categoryId = this.category?.id,
            displayName = this.displayName,
            businessName = this.businessName,
            specialty = this.specialty,
            description = this.description,
            address = this.address,
            city = this.city,
            country = this.country,
            defaultSessionDurationMinutes = this.defaultSessionDurationMinutes,
            latitude = this.latitude,
            longitude = this.longitude,
            workingHoursJson = this.workingHoursJson,
            active = this.active,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
}
