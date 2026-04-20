package com.duoc.app.controller

import com.duoc.app.model.LoginRequest
import com.duoc.app.model.User
import com.duoc.app.model.UserResponse
import com.duoc.app.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userRepository: UserRepository
) {

    @PostMapping("/register")
    fun registerUser(@RequestBody user: User): ResponseEntity<UserResponse> {
        if (userRepository.existsById(user.email)) {
            return ResponseEntity.status(409).build()
        }

        // Evita que un cliente se registre como ADMIN desde el frontend
        val toSave = user.copy(role = "USER")
        val saved = userRepository.save(toSave)

        return ResponseEntity.ok(saved.toResponse())
    }

    @GetMapping("/{email}")
    fun getUserByEmail(@PathVariable email: String): ResponseEntity<UserResponse> {
        val user = userRepository.findByEmail(email)
        return if (user != null) {
            ResponseEntity.ok(user.toResponse())
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<UserResponse> {
        val user = userRepository.findByEmail(loginRequest.email)
        return if (user != null && user.password == loginRequest.password) {
            ResponseEntity.ok(user.toResponse())
        } else {
            ResponseEntity.status(401).build()
        }
    }

    private fun User.toResponse(): UserResponse =
        UserResponse(email = email, name = name, role = role)
}
