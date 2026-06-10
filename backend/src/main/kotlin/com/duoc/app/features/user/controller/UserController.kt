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
}