package com.duoc.app.features.user.controller

import com.duoc.app.features.user.dto.UserResponse
import com.duoc.app.features.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController("featureUserController")
@RequestMapping("/api/users")
@CrossOrigin("*")
class UserController(
    private val userService: UserService
) {

    @GetMapping("/email/{email}")
    fun getByEmail(@PathVariable email: String): ResponseEntity<UserResponse> {
        return try {
            ResponseEntity.ok(userService.getByEmail(email))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/specialists")
    fun getSpecialists(): ResponseEntity<List<UserResponse>> {
        return ResponseEntity.ok(userService.getSpecialists())
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<UserResponse> {
        return try {
            ResponseEntity.ok(userService.getById(id))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
}