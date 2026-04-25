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
        return ResponseEntity.ok(serviceOfferingService.create(request))
    }

    @GetMapping
    fun getActive(): ResponseEntity<List<ServiceOfferingResponse>> {
        return ResponseEntity.ok(serviceOfferingService.getActive())
    }

    @GetMapping("/professional-profile/{professionalProfileId}")
    fun getByProfessionalProfile(@PathVariable professionalProfileId: Long): ResponseEntity<List<ServiceOfferingResponse>> {
        return ResponseEntity.ok(serviceOfferingService.getByProfessionalProfile(professionalProfileId))
    }

    @PutMapping("/{id}/deactivate")
    fun deactivate(@PathVariable id: Long): ResponseEntity<ServiceOfferingResponse> {
        return ResponseEntity.ok(serviceOfferingService.deactivate(id))
    }
}
