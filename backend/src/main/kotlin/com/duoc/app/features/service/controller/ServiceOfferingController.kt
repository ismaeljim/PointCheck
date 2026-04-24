package com.duoc.app.features.service.controller

import com.duoc.app.features.service.dto.ServiceOfferingRequest
import com.duoc.app.features.service.dto.ServiceOfferingResponse
import com.duoc.app.features.service.service.ServiceOfferingService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/services")
@CrossOrigin("*")
class ServiceOfferingController(
    private val serviceOfferingService: ServiceOfferingService
) {

    @PostMapping
    fun create(@RequestBody request: ServiceOfferingRequest): ResponseEntity<ServiceOfferingResponse> {
        return try {
            ResponseEntity.ok(serviceOfferingService.create(request))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping
    fun getActive(): ResponseEntity<List<ServiceOfferingResponse>> {
        return ResponseEntity.ok(serviceOfferingService.getActive())
    }

    @GetMapping("/specialist/{specialistId}")
    fun getBySpecialist(@PathVariable specialistId: Long): ResponseEntity<List<ServiceOfferingResponse>> {
        return ResponseEntity.ok(serviceOfferingService.getBySpecialist(specialistId))
    }

    @PutMapping("/{id}/deactivate")
    fun deactivate(@PathVariable id: Long): ResponseEntity<ServiceOfferingResponse> {
        return try {
            ResponseEntity.ok(serviceOfferingService.deactivate(id))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
}
