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

@Service
class ProfessionalProfileService(
    private val professionalProfileRepository: ProfessionalProfileRepository,
    private val userRepository: UserRepository,
    private val categoryRepository: CategoryRepository
) {

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
            defaultSessionDurationMinutes = request.defaultSessionDurationMinutes,
            active = true
        )

        return professionalProfileRepository.save(profile).toResponse()
    }

    fun getActive(categoryId: String? = null): List<ProfessionalProfileResponse> {
        return if (categoryId != null) {
            professionalProfileRepository.findByCategoryIdAndActiveTrue(categoryId).map { it.toResponse() }
        } else {
            professionalProfileRepository.findByActiveTrue().map { it.toResponse() }
        }
    }

    fun getByUserId(userId: String): ProfessionalProfileResponse {
        val profile = professionalProfileRepository.findByUser_Id(userId)
            ?: throw NoSuchElementException("Perfil profesional no encontrado para el usuario $userId")
        return profile.toResponse()
    }

    @Transactional
    fun update(id: String, request: ProfessionalProfileRequest): ProfessionalProfileResponse {
        val profile = professionalProfileRepository.findById(id).orElseThrow {
            NoSuchElementException("Perfil profesional no encontrado con id $id")
        }

        val category = request.categoryId?.let { 
            categoryRepository.findById(it).orElse(null)
        }

        val updatedProfile = profile.copy(
            category = category,
            displayName = request.displayName,
            businessName = request.businessName,
            specialty = request.specialty,
            description = request.description,
            address = request.address,
            city = request.city,
            country = request.country,
            defaultSessionDurationMinutes = request.defaultSessionDurationMinutes,
            updatedAt = LocalDateTime.now()
        )

        return professionalProfileRepository.save(updatedProfile).toResponse()
    }

    @Transactional
    fun deactivate(id: String): ProfessionalProfileResponse {
        val profile = professionalProfileRepository.findById(id).orElseThrow {
            NoSuchElementException("Perfil profesional no encontrado con id $id")
        }

        val deactivatedProfile = profile.copy(
            active = false,
            updatedAt = LocalDateTime.now()
        )

        return professionalProfileRepository.save(deactivatedProfile).toResponse()
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
            active = this.active,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
}
