package com.duoc.app.features.professionalprofile.controller

import com.duoc.app.features.professionalprofile.dto.ProfessionalProfileRequest
import com.duoc.app.features.professionalprofile.dto.ProfessionalProfileResponse
import com.duoc.app.features.professionalprofile.service.ProfessionalProfileService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/professional-profiles")
@CrossOrigin("*")
class ProfessionalProfileController(
    private val professionalProfileService: ProfessionalProfileService
) {

    @PostMapping
    fun create(@RequestBody request: ProfessionalProfileRequest): ResponseEntity<ProfessionalProfileResponse> {
        return ResponseEntity.ok(professionalProfileService.create(request))
    }

    @GetMapping
    fun getActive(): ResponseEntity<List<ProfessionalProfileResponse>> {
        return ResponseEntity.ok(professionalProfileService.getActive())
    }

    @GetMapping("/user/{userId}")
    fun getByUserId(@PathVariable userId: Long): ResponseEntity<ProfessionalProfileResponse> {
        return ResponseEntity.ok(professionalProfileService.getByUserId(userId))
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: ProfessionalProfileRequest
    ): ResponseEntity<ProfessionalProfileResponse> {
        return ResponseEntity.ok(professionalProfileService.update(id, request))
    }

    @PutMapping("/{id}/deactivate")
    fun deactivate(@PathVariable id: Long): ResponseEntity<ProfessionalProfileResponse> {
        return ResponseEntity.ok(professionalProfileService.deactivate(id))
    }
}
