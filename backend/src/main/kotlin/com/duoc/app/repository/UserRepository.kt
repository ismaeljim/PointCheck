package com.duoc.app.repository

import com.duoc.app.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, String> {
    // Spring entenderá automáticamente que esta función debe buscar un usuario por su email
    fun findByEmail(email: String): User?
}
    