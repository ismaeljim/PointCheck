package com.duoc.app.features.service.service

import com.duoc.app.features.professionalprofile.repository.ProfessionalProfileRepository
import com.duoc.app.features.service.dto.ServiceOfferingRequest
import com.duoc.app.features.service.dto.ServiceOfferingResponse
import com.duoc.app.features.service.model.ServiceOffering
import com.duoc.app.features.service.repository.ServiceOfferingRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * Servicio para la gestión de la oferta de servicios (catálogo).
 *
 * Permite a los especialistas definir las prestaciones que ofrecen, incluyendo
 * precios, duración y modalidad (a domicilio o presencial).
 */
@Service
class ServiceOfferingService(
    private val serviceOfferingRepository: ServiceOfferingRepository,
    private val professionalProfileRepository: ProfessionalProfileRepository
) {

    /**
     * Crea un nuevo servicio en el catálogo del especialista.
     *
     * @param request Detalle del servicio a ofrecer.
     * @return [ServiceOfferingResponse] con el servicio persistido.
     * @throws IllegalArgumentException si el perfil profesional no existe o no está activo.
     */
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
            durationMinutes = request.durationMinutes,
            priceUnit = request.priceUnit,
            isAtHome = request.isAtHome
        )

        return serviceOfferingRepository.save(serviceOffering).toResponse()
    }

    /**
     * Recupera todos los servicios configurados por un especialista específico.
     *
     * @param professionalProfileId ID del perfil profesional.
     * @return Lista de servicios asociados.
     */
    fun getByProfessionalProfile(professionalProfileId: String): List<ServiceOfferingResponse> {
        return serviceOfferingRepository.findByProfessionalProfile_Id(professionalProfileId).map { it.toResponse() }
    }

    /**
     * Obtiene el listado global de todos los servicios marcados como activos en la plataforma.
     *
     * @return Lista de servicios disponibles para reserva.
     */
    fun getActive(): List<ServiceOfferingResponse> {
        return serviceOfferingRepository.findByActiveTrue().map { it.toResponse() }
    }

    /**
     * Desactiva un servicio del catálogo (Soft-Delete).
     *
     * El servicio dejará de estar disponible para nuevas reservas, pero se mantiene
     * en la base de datos para preservar la integridad referencial del historial.
     *
     * @param id ID del servicio a desactivar.
     * @return Servicio actualizado con estado inactivo.
     */
    fun deactivate(id: String): ServiceOfferingResponse {
        val serviceOffering = serviceOfferingRepository.findById(id).orElseThrow {
            IllegalArgumentException("Servicio no encontrado con ID: $id")
        }
        
        serviceOffering.apply {
            active = false
            updatedAt = LocalDateTime.now()
        }
        
        return serviceOfferingRepository.save(serviceOffering).toResponse()
    }

    private fun ServiceOffering.toResponse(): ServiceOfferingResponse = ServiceOfferingResponse(
        id = this.id,
        professionalProfileId = this.professionalProfile.id,
        name = this.name,
        description = this.description,
        price = this.price,
        durationMinutes = this.durationMinutes,
        priceUnit = this.priceUnit.name,
        isAtHome = this.isAtHome,
        active = this.active
    )
}
