package com.duoc.app.features.auth.controller

import com.duoc.app.features.auth.dto.LoginRequest
import com.duoc.app.features.auth.dto.RegisterRequest
import com.duoc.app.features.auth.service.AuthService
import com.duoc.app.features.user.dto.UserResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<Any> {
        return try {
            ResponseEntity.ok(authService.register(request))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf(
                "status" to 400,
                "error" to "Bad Request",
                "message" to (e.message ?: "Error en el registro")
            ))
        }
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<Any> {
        return try {
            ResponseEntity.ok(authService.login(request))
        } catch (e: IllegalArgumentException) {
            // Formato exacto que espera ApiErrorDto en la App
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf(
                "status" to 401,
                "error" to "Unauthorized",
                "message" to (e.message ?: "Credenciales inválidas")
            ))
        }
    }

    @PostMapping("/logout")
    fun logout(@RequestParam email: String): ResponseEntity<Any> {
        println("AUTH-DEBUG: Cierre de sesión solicitado para [$email]")
        return ResponseEntity.ok(mapOf("message" to "Sesión cerrada correctamente"))
    }
}
