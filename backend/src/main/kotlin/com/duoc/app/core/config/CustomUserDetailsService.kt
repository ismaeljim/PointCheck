package com.duoc.app.core.config

import com.duoc.app.features.user.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmailWithProfile(email)
            ?: throw UsernameNotFoundException("Usuario no encontrado con email: $email")

        // Mapeamos el Enum UserRole a una Authority de Spring
        // Importante: No usamos ROLE_ de prefijo porque en SecurityConfig usamos hasAuthority("ADMIN")
        val authorities = listOf(SimpleGrantedAuthority(user.role.name))

        return User.builder()
            .username(user.email)
            .password(user.password) // Debe estar ya cifrada con BCrypt en la DB
            .authorities(authorities)
            .disabled(!user.active)
            .build()
    }
}
