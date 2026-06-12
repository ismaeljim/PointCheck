package com.duoc.app.core.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                // Endpoints públicos (Registro e Inicio de Sesión)
                auth.requestMatchers("/api/auth/**").permitAll()
                
                // Endpoints de Administración (Solo ADMIN)
                auth.requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Endpoints de Auditoría (Solo ADMIN)
                auth.requestMatchers("/api/audit/**").hasRole("ADMIN")

                // El resto requiere autenticación
                auth.anyRequest().authenticated()
            }
            .httpBasic { } // Habilitamos Basic Auth para simplificar la integración con la App actual
            .formLogin { it.disable() }
        
        return http.build()
    }
}
