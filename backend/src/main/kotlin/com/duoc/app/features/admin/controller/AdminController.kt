package com.duoc.app.features.admin.controller

import com.duoc.app.features.admin.model.AuditLog
import com.duoc.app.features.admin.model.GlobalSettings
import com.duoc.app.features.admin.service.AdminService
import com.duoc.app.features.user.model.User
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal

/**
 * Controlador REST para operaciones administrativas de alto nivel.
 * 
 * Protegido con seguridad basada en roles (requiere autoridad 'ADMIN'). Permite la
 * gestión de usuarios, visualización de reportes financieros, configuración global
 * del sistema y acceso a los logs de auditoría.
 */
@RestController
@RequestMapping("/api/admin")
// @org.springframework.security.access.prepost.PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
class AdminController(private val adminService: AdminService) {

    /**
     * Recupera la lista de todos los usuarios registrados en el sistema.
     * @return [ResponseEntity] con la lista de usuarios.
     */
    @GetMapping("/users")
    fun getAllUsers(): ResponseEntity<List<com.duoc.app.features.admin.dto.AdminUserResponse>> = ResponseEntity.ok(adminService.getAllUsers())

    /**
     * Cambia el estado de actividad (habilitado/deshabilitado) de un usuario.
     * @param id ID del usuario a modificar.
     * @return [ResponseEntity] con los datos del usuario actualizado.
     */
    @PatchMapping("/users/{id}/toggle-status")
    fun toggleUserStatus(@PathVariable id: String): ResponseEntity<com.duoc.app.features.admin.dto.AdminUserResponse> =
        ResponseEntity.ok(adminService.toggleUserStatus(id))

    /**
     * Actualiza los datos de un usuario desde el panel de administración.
     * @param id ID del usuario.
     * @param request DTO con los datos a actualizar.
     * @return [ResponseEntity] con el usuario actualizado.
     */
    @PutMapping("/users/{id}")
    fun updateUser(
        @PathVariable id: String,
        @RequestBody request: com.duoc.app.features.admin.dto.AdminUserUpdateRequest
    ): ResponseEntity<com.duoc.app.features.admin.dto.AdminUserResponse> =
        ResponseEntity.ok(adminService.updateUser(id, request))

    /**
     * Genera un reporte financiero consolidado de la plataforma.
     * @return [ResponseEntity] con métricas de ingresos y transacciones.
     */
    @GetMapping("/reports/financial")
    fun getFinancialReport(): ResponseEntity<Map<String, Any>> = 
        ResponseEntity.ok(adminService.getFinancialReport())

    /**
     * Obtiene el listado de configuraciones globales del sistema.
     * @return [ResponseEntity] con las configuraciones.
     */
    @GetMapping("/settings")
    fun getSettings(): ResponseEntity<List<GlobalSettings>> = 
        ResponseEntity.ok(adminService.getSettings())

    /**
     * Actualiza o crea una configuración global.
     * @param key Clave de la configuración.
     * @param value Valor a asignar.
     * @return [ResponseEntity] con la configuración guardada.
     */
    @PostMapping("/settings")
    fun updateSetting(
        @RequestParam key: String,
        @RequestParam value: String,
        principal: Principal
    ): ResponseEntity<GlobalSettings> = 
        ResponseEntity.ok(adminService.updateSetting(key, value, principal.name))

    /**
     * Obtiene métricas administrativas de gobernanza.
     */
    @GetMapping("/metrics")
    fun getAdminMetrics(): ResponseEntity<com.duoc.app.features.dashboard.dto.DashboardMetricsResponse> {
        return ResponseEntity.ok(adminService.getAdminMetrics())
    }
}
