package com.duoc.app.features.dashboard.controller

import com.duoc.app.features.dashboard.dto.DashboardMetricsResponse
import com.duoc.app.features.dashboard.service.DashboardService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/dashboard")
class DashboardController(
    private val dashboardService: DashboardService
) {

    /**
     * Obtiene métricas operativas.
     * Se permite el acceso a cualquier usuario autenticado para evitar bloqueos 403 fortuitos.
     * El servicio discrimina internamente qué métricas mostrar según el rol detectado.
     */
    @GetMapping("/metrics")
    // @PreAuthorize("isAuthenticated()")
    fun getMetrics(authentication: Authentication): ResponseEntity<DashboardMetricsResponse> {
        val email = authentication.name
        val authorities = authentication.authorities.map { it.authority.uppercase() }
        
        // Detección de rol robusta
        val role = when {
            authorities.any { it == "ADMIN" || it == "ROLE_ADMIN" } -> "ADMIN"
            authorities.any { it == "SPECIALIST" || it == "ROLE_SPECIALIST" } -> "SPECIALIST"
            authorities.any { it == "CLIENT" || it == "ROLE_CLIENT" } -> "CLIENT"
            else -> ""
        }
        
        if (role.isBlank()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        
        val user = dashboardService.getUserByEmail(email)
        val userId = user.id ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        
        return ResponseEntity.ok(dashboardService.getMetrics(userId, role))
    }

    /**
     * Dashboard específico para Clientes.
     */
    @GetMapping("/client", "/client/{userId}")
    // @PreAuthorize("isAuthenticated()")
    fun getClientDashboard(
        authentication: Authentication,
        @PathVariable(required = false) userId: String?
    ): ResponseEntity<com.duoc.app.features.dashboard.dto.ClientDashboardResponse> {
        return try {
            val email = authentication.name
            val user = dashboardService.getUserByEmail(email)
            ResponseEntity.ok(dashboardService.getClientDashboard(user.id!!))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}
