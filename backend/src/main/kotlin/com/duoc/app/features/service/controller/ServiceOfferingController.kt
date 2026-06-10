package com.duoc.app.features.service.controller

import com.duoc.app.features.service.dto.ServiceOfferingRequest
import com.duoc.app.features.service.dto.ServiceOfferingResponse
import com.duoc.app.features.service.service.ServiceOfferingService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controlador REST para la gestión de ofertas de servicios por parte de los profesionales.
 * Permite a los especialistas publicar, listar y desactivar los servicios que ofrecen.
 */
@RestController
@RequestMapping("/api/services")
@CrossOrigin("*")
class ServiceOfferingController(
    private val serviceOfferingService: ServiceOfferingService
) {

    /**
     * Registra un nuevo servicio ofrecido por un profesional.
     *
     * @param request Datos del servicio (nombre, descripción, precio, categoría, etc.).
     * @return Respuesta con el servicio registrado.
     */
    @PostMapping
    fun create(@RequestBody request: ServiceOfferingRequest): ResponseEntity<ServiceOfferingResponse> {
        return ResponseEntity.ok(serviceOfferingService.create(request))
    }

    /**
     * Obtiene la lista de todos los servicios activos en la plataforma.
     *
     * @return Lista de servicios disponibles para reserva.
     */
    @GetMapping
    fun getActive(): ResponseEntity<List<ServiceOfferingResponse>> {
        return ResponseEntity.ok(serviceOfferingService.getActive())
    }

    /**
     * Obtiene todos los servicios (activos e inactivos) de un profesional específico.
     *
     * @param professionalProfileId ID del perfil profesional.
     * @return Lista de servicios asociados al profesional.
     */
    @GetMapping("/professional-profile/{professionalProfileId}")
    fun getByProfessionalProfile(@PathVariable professionalProfileId: String): ResponseEntity<List<ServiceOfferingResponse>> {
        return ResponseEntity.ok(serviceOfferingService.getByProfessionalProfile(professionalProfileId))
    }

    /**
     * Desactiva un servicio específico para que deje de estar disponible para nuevas reservas.
     *
     * @param id ID del servicio a desactivar.
     * @return Servicio con estado actualizado a desactivado.
     */
    @PutMapping("/{id}/deactivate")
    fun deactivate(@PathVariable id: String): ResponseEntity<ServiceOfferingResponse> {
        return ResponseEntity.ok(serviceOfferingService.deactivate(id))
    }
}
