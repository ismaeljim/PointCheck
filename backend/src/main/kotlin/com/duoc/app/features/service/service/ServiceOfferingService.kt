package com.duoc.app.features.service.service

import com.duoc.app.features.professionalprofile.repository.ProfessionalProfileRepository
import com.duoc.app.features.service.dto.ServiceOfferingRequest
import com.duoc.app.features.service.dto.ServiceOfferingResponse
import com.duoc.app.features.service.model.ServiceOffering
import com.duoc.app.features.service.repository.ServiceOfferingRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * AUDITORÍA TÉCNICA: Gestión de Oferta de Servicios
 * 
 * Este servicio centraliza el catálogo de prestaciones que los especialistas pueden ofrecer.
 * 
 * Hallazgos de Implementación:
 * 1. [OK] Desacoplamiento de Entidad/DTO: Se utiliza el patrón Mapper para no exponer el modelo de datos.
 * 2. [OK] Validaciones de Negocio: Se verifica la existencia y el estado activo del perfil profesional antes de crear el servicio.
 * 3. [MEJORA] Soft-Delete: Implementado mediante el campo 'active'. Se recomienda auditoría de cambios para el historial de precios.
 * 4. [BRECHA] Validación de Precios: Falta validación de rango de precios permitidos (ej. no negativos).
 */
@Service
class ServiceOfferingService(
    private val serviceOfferingRepository: ServiceOfferingRepository,
    private val professionalProfileRepository: ProfessionalProfileRepository
) {

    fun create(request: ServiceOfferingRequest): ServiceOfferingResponse {
        // AUDITORÍA: Verificación de integridad referencial manual antes de persistencia.
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

    /**
     * AUDITORÍA: El método utiliza 'copy' de Data Class para inmutabilidad parcial.
     * Se garantiza que el servicio deje de ser elegible para nuevas reservas sin borrar historial.
     */
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
