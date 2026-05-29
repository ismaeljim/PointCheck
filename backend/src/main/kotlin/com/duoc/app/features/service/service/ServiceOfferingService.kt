package com.duoc.app.features.service.service

import com.duoc.app.features.professionalprofile.repository.ProfessionalProfileRepository
import com.duoc.app.features.service.dto.ServiceOfferingRequest
import com.duoc.app.features.service.dto.ServiceOfferingResponse
import com.duoc.app.features.service.model.ServiceOffering
import com.duoc.app.features.service.repository.ServiceOfferingRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ServiceOfferingService(
    private val serviceOfferingRepository: ServiceOfferingRepository,
    private val professionalProfileRepository: ProfessionalProfileRepository
) {

    fun create(request: ServiceOfferingRequest): ServiceOfferingResponse {
        val profile = professionalProfileRepository.findById(request.professionalProfileId).orElseThrow {
            IllegalArgumentException("El perfil profesional con ID ${request.professionalProfileId} no existe.")
        }

        if (!profile.active) {
            throw IllegalArgumentException("El perfil profesional con ID ${request.professionalProfileId} no está activo.")
        }

        val serviceOffering = ServiceOffering(
            professionalProfile = profile,
            name = request.name,
            description = request.description,
            price = request.price,
            durationMinutes = request.durationMinutes
        )

        return serviceOfferingRepository.save(serviceOffering).toResponse()
    }

    fun getByProfessionalProfile(professionalProfileId: String): List<ServiceOfferingResponse> {
        return serviceOfferingRepository.findByProfessionalProfile_Id(professionalProfileId).map { it.toResponse() }
    }

    fun getActive(): List<ServiceOfferingResponse> {
        return serviceOfferingRepository.findByActiveTrue().map { it.toResponse() }
    }

    fun deactivate(id: String): ServiceOfferingResponse {
        val serviceOffering = serviceOfferingRepository.findById(id).orElseThrow {
            IllegalArgumentException("Servicio no encontrado con ID: $id")
        }
        
        val updatedService = serviceOffering.copy(
            active = false,
            updatedAt = LocalDateTime.now()
        )
        
        return serviceOfferingRepository.save(updatedService).toResponse()
    }

    private fun ServiceOffering.toResponse(): ServiceOfferingResponse = ServiceOfferingResponse(
        id = this.id,
        professionalProfileId = this.professionalProfile.id,
        name = this.name,
        description = this.description,
        price = this.price,
        durationMinutes = this.durationMinutes,
        active = this.active
    )
}
