package com.duoc.app.features.user.controller

import com.duoc.app.features.user.dto.UserResponse
import com.duoc.app.features.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST Controller for managing user-related operations.
 *
 * This controller provides endpoints for retrieving user information by email or ID,
 * and fetching a list of registered specialists.
 *
 * @property userService The service layer handling user business logic.
 */
@RestController("featureUserController")
@RequestMapping("/api/users")
@CrossOrigin("*")
class UserController(
    private val userService: UserService
) {

    /**
     * Retrieves a user by their email address.
     *
     * @param email The email of the user to find.
     * @return A [ResponseEntity] containing the [UserResponse] if found, or 404 Not Found.
     */
    @GetMapping("/email/{email}")
    fun getByEmail(@PathVariable email: String): ResponseEntity<UserResponse> {
        return try {
            ResponseEntity.ok(userService.getByEmail(email))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Retrieves all users registered with the "Specialist" or "Professional" role.
     *
     * @return A [ResponseEntity] containing a list of [UserResponse] objects.
     */
    @GetMapping("/specialists")
    fun getSpecialists(): ResponseEntity<List<UserResponse>> {
        return ResponseEntity.ok(userService.getSpecialists())
    }

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param id The ID of the user.
     * @return A [ResponseEntity] containing the [UserResponse] if found, or 404 Not Found.
     */
    @GetMapping("/{id}")
    fun getById(@PathVariable id: String): ResponseEntity<UserResponse> {
        return try {
            ResponseEntity.ok(userService.getById(id))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Actualiza el perfil del usuario autenticado.
     */
    @PutMapping("/{id}")
    fun updateProfile(
        @PathVariable id: String,
        @RequestBody request: com.duoc.app.features.user.dto.UserUpdateRequest
    ): ResponseEntity<Any> {
        return try {
            ResponseEntity.ok(userService.updateProfile(id, request))
        } catch (e: SecurityException) {
            ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    /**
     * Actualiza la dirección del usuario.
     */
    @PutMapping("/{id}/address")
    fun updateAddress(
        @PathVariable id: String,
        @RequestParam address: String
    ): ResponseEntity<Any> {
        return try {
            ResponseEntity.ok(userService.updateAddress(id, address))
        } catch (e: SecurityException) {
            ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    /**
     * Cambia la contraseña del usuario.
     */
    @PutMapping("/{id}/password")
    fun changePassword(
        @PathVariable id: String,
        @RequestBody request: com.duoc.app.features.user.dto.ChangePasswordRequest
    ): ResponseEntity<Any> {
        return try {
            userService.changePassword(id, request)
            ResponseEntity.ok().build()
        } catch (e: SecurityException) {
            ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}