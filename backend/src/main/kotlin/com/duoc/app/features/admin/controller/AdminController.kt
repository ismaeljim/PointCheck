package com.duoc.app.features.admin.controller

import com.duoc.app.features.admin.model.AuditLog
import com.duoc.app.features.admin.model.GlobalSettings
import com.duoc.app.features.admin.service.AdminService
import com.duoc.app.features.user.model.User
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/admin")
@org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ADMIN')")
class AdminController(private val adminService: AdminService) {

    @GetMapping("/users")
    fun getAllUsers(): ResponseEntity<List<com.duoc.app.features.admin.dto.AdminUserResponse>> = ResponseEntity.ok(adminService.getAllUsers())

    @PatchMapping("/users/{id}/toggle-status")
    fun toggleUserStatus(@PathVariable id: String): ResponseEntity<com.duoc.app.features.admin.dto.AdminUserResponse> =
        ResponseEntity.ok(adminService.toggleUserStatus(id))

    @PutMapping("/users/{id}")
    fun updateUser(
        @PathVariable id: String,
        @RequestBody request: com.duoc.app.features.admin.dto.AdminUserUpdateRequest
    ): ResponseEntity<com.duoc.app.features.admin.dto.AdminUserResponse> =
        ResponseEntity.ok(adminService.updateUser(id, request))

    @GetMapping("/reports/financial")
    fun getFinancialReport(): ResponseEntity<Map<String, Any>> = 
        ResponseEntity.ok(adminService.getFinancialReport())

    @GetMapping("/settings")
    fun getSettings(): ResponseEntity<List<GlobalSettings>> = 
        ResponseEntity.ok(adminService.getSettings())

    @PostMapping("/settings")
    fun updateSetting(
        @RequestParam key: String,
        @RequestParam value: String
    ): ResponseEntity<GlobalSettings> = 
        ResponseEntity.ok(adminService.updateSetting(key, value))

    @GetMapping("/audit-logs")
    fun getAuditLogs(): ResponseEntity<List<AuditLog>> = 
        ResponseEntity.ok(adminService.getAuditLogs())
}
