package com.duoc.app.features.service.service

import com.duoc.app.features.service.dto.ServiceOfferingRequest
import com.duoc.app.features.service.dto.ServiceOfferingResponse
import com.duoc.app.features.service.model.ServiceOffering
import com.duoc.app.features.service.repository.ServiceOfferingRepository
import com.duoc.app.features.user.model.UserRole
import com.duoc.app.features.user.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ServiceOfferingService(
    private val serviceOfferingRepository: ServiceOfferingRepository,
    private val userRepository: UserRepository
) {

    fun create(request: ServiceOfferingRequest): ServiceOfferingResponse {
        val specialist = userRepository.findById(request.specialistId).orElseThrow {
            IllegalArgumentException("El especialista con ID ${request.specialistId} no existe.")
        }

        if (specialist.role != UserRole.SPECIALIST) {
            throw IllegalArgumentException("El usuario con ID ${request.specialistId} no es un especialista.")
        }

        val serviceOffering = ServiceOffering(
            specialistId = request.specialistId,
            name = request.name,
            description = request.description,
            price = request.price,
            durationMinutes = request.durationMinutes
        )

        return serviceOfferingRepository.save(serviceOffering).toResponse()
    }

    fun getBySpecialist(specialistId: Long): List<ServiceOfferingResponse> {
        return serviceOfferingRepository.findBySpecialistId(specialistId).map { it.toResponse() }
    }

    fun getActive(): List<ServiceOfferingResponse> {
        return serviceOfferingRepository.findByActiveTrue().map { it.toResponse() }
    }

    fun deactivate(id: Long): ServiceOfferingResponse {
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
        specialistId = this.specialistId,
        name = this.name,
        description = this.description,
        price = this.price,
        durationMinutes = this.durationMinutes,
        active = this.active
    )
}
