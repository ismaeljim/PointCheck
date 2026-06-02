package com.duoc.app.features.auth.controller

import com.duoc.app.features.auth.dto.LoginRequest
import com.duoc.app.features.auth.dto.RegisterRequest
import com.duoc.app.features.auth.service.AuthService
import com.duoc.app.features.user.dto.UserResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controlador REST para la gestión de autenticación.
 * Expone endpoints para el registro de usuarios y el inicio de sesión.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*") // AUDITORÍA: En producción, restringir orígenes permitidos.
class AuthController(
    private val authService: AuthService
) {

    /**
     * Endpoint de Registro.
     * Recibe los datos del usuario (Cliente o Especialista) y delega la creación al servicio.
     * 
     * AUDITORÍA:
     * - Se recomienda devolver un mensaje de error claro en el cuerpo de la respuesta en lugar de solo .build().
     */
    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<UserResponse> {
        return try {
            ResponseEntity.ok(authService.register(request))
        } catch (e: IllegalArgumentException) {
            // TODO: Retornar DTO de error con el mensaje e.message
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    /**
     * Endpoint de Login.
     * Valida credenciales y retorna el perfil del usuario autenticado.
     */
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<UserResponse> {
        return try {
            ResponseEntity.ok(authService.login(request))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
    }
}
