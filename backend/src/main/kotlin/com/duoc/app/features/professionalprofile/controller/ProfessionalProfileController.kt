package com.duoc.app.features.professionalprofile.controller

import com.duoc.app.features.professionalprofile.dto.ProfessionalProfileRequest
import com.duoc.app.features.professionalprofile.dto.ProfessionalProfileResponse
import com.duoc.app.features.professionalprofile.service.ProfessionalProfileService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controlador REST para la gestión de Perfiles Profesionales.
 * Expone endpoints para la administración de la identidad comercial de los especialistas.
 */
@RestController
@RequestMapping("/api/professional-profiles")
@CrossOrigin("*")
class ProfessionalProfileController(
    private val professionalProfileService: ProfessionalProfileService
) {

    /**
     * Endpoint para la creación inicial del perfil. 
     * Requiere que el usuario tenga rol SPECIALIST.
     */
    @PostMapping
    fun create(@RequestBody request: ProfessionalProfileRequest): ResponseEntity<ProfessionalProfileResponse> {
        return ResponseEntity.ok(professionalProfileService.create(request))
    }

    /**
     * Lista perfiles activos. Soporta filtrado por categoría para búsquedas de clientes.
     */
    @GetMapping
    fun getActive(
        @RequestParam(required = false) categoryId: String?
    ): ResponseEntity<List<ProfessionalProfileResponse>> {
        return ResponseEntity.ok(professionalProfileService.getActive(categoryId))
    }

    /**
     * Recupera el perfil específico de un usuario especialista.
     */
    @GetMapping("/user/{userId}")
    fun getByUserId(@PathVariable userId: String): ResponseEntity<ProfessionalProfileResponse> {
        return ResponseEntity.ok(professionalProfileService.getByUserId(userId))
    }

    /**
     * Actualiza datos comerciales o de disponibilidad.
     */
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: String,
        @RequestBody request: ProfessionalProfileRequest
    ): ResponseEntity<ProfessionalProfileResponse> {
        return ResponseEntity.ok(professionalProfileService.update(id, request))
    }

    /**
     * Desactivación lógica para retirar al especialista de las búsquedas públicas.
     */
    @PutMapping("/{id}/deactivate")
    fun deactivate(@PathVariable id: String): ResponseEntity<ProfessionalProfileResponse> {
        return ResponseEntity.ok(professionalProfileService.deactivate(id))
    }
}
