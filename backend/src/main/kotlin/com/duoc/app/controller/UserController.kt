package com.duoc.app.controller

import com.duoc.app.features.auth.dto.LoginRequest
import com.duoc.app.features.auth.dto.RegisterRequest
import com.duoc.app.features.auth.service.AuthService
import com.duoc.app.features.user.dto.UserResponse
import com.duoc.app.features.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController("legacyUserController")
@RequestMapping("/api/users")
@Deprecated("Migrar a com.duoc.app.features.user.controller.UserController y AuthController")
class UserController(
    private val authService: AuthService,
    private val userService: UserService
) {

    @PostMapping("/register")
    fun registerUser(@RequestBody request: RegisterRequest): ResponseEntity<UserResponse> {
        // TODO: Android debe migrar a POST /api/auth/register
        return ResponseEntity.ok(authService.register(request))
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<UserResponse> {
        // TODO: Android debe migrar a POST /api/auth/login
        return ResponseEntity.ok(authService.login(request))
    }

    @GetMapping("/{email}")
    fun getUserByEmail(@PathVariable email: String): ResponseEntity<UserResponse> {
        // TODO: Android debe migrar a GET /api/users/{id}
        return ResponseEntity.ok(userService.getByEmail(email))
    }
}
