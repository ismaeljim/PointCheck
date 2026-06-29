package com.duoc.app.features.admin.controller

import com.duoc.app.features.admin.model.AuditLog
import com.duoc.app.features.admin.service.AdminService
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Controlador dedicado para la Auditoría de Gobernanza.
 * Responde a /api/audit cumpliendo con el contrato estricto de seguridad.
 */
@RestController
@RequestMapping("/api/audit")
class AuditController(private val adminService: AdminService) {

    @GetMapping("", "/")
    @PreAuthorize("hasAuthority('ADMIN')")
    fun getAuditLogs(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<AuditLog>> {
        return ResponseEntity.ok(adminService.getAuditLogs(page, size))
    }
}
