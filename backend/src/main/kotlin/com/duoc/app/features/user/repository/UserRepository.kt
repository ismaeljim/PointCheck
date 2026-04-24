package com.duoc.app.features.user.repository

import com.duoc.app.features.user.model.User
import com.duoc.app.features.user.model.UserRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
    fun findByRole(role: UserRole): List<User>
}
