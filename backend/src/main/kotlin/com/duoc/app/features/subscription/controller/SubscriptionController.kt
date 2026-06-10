package com.duoc.app.features.subscription.controller

import com.duoc.app.features.subscription.dto.SubscriptionRequest
import com.duoc.app.features.subscription.dto.SubscriptionResponse
import com.duoc.app.features.subscription.service.SubscriptionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controlador REST para la gestión de suscripciones de profesionales.
 * Maneja la creación, consulta de estado actual y cancelación de planes de suscripción.
 */
@RestController
@RequestMapping("/api/subscriptions")
@CrossOrigin("*")
class SubscriptionController(
    private val subscriptionService: SubscriptionService
) {

    /**
     * Crea una nueva suscripción para un perfil profesional.
     *
     * @param request Datos de la suscripción (ID de perfil, tipo de plan, etc.).
     * @return Respuesta con los detalles de la suscripción activada.
     */
    @PostMapping
    fun create(@RequestBody request: SubscriptionRequest): ResponseEntity<SubscriptionResponse> {
        return ResponseEntity.ok(subscriptionService.create(request))
    }

    /**
     * Obtiene la suscripción vigente de un perfil profesional específico.
     *
     * @param professionalProfileId ID del perfil profesional.
     * @return Suscripción actual o 404 si no existe una activa.
     */
    @GetMapping("/professional-profile/{professionalProfileId}/current")
    fun getCurrentByProfessionalProfile(@PathVariable professionalProfileId: String): ResponseEntity<SubscriptionResponse> {
        val subscription = subscriptionService.getCurrentByProfessionalProfile(professionalProfileId)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(subscription)
    }

    /**
     * Cancela una suscripción activa.
     *
     * @param id ID de la suscripción a cancelar.
     * @return Detalles de la suscripción tras procesar la cancelación.
     */
    @PutMapping("/{id}/cancel")
    fun cancel(@PathVariable id: String): ResponseEntity<SubscriptionResponse> {
        return ResponseEntity.ok(subscriptionService.cancel(id))
    }
}
