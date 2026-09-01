package com.duoc.app.core.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import jakarta.servlet.http.HttpServletResponse

@Configuration
@EnableWebSecurity
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // 1. Deshabilitar CSRF (No es necesario para APIs Stateless)
            .csrf { it.disable() }
            
            // 2. Configuración de CORS Robusta
            .cors { cors ->
                val source = UrlBasedCorsConfigurationSource()
                val config = CorsConfiguration()
                config.allowedOrigins = listOf("*") // En producción, restringir a la IP del servidor/App
                config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
                config.allowedHeaders = listOf("Authorization", "Content-Type", "X-Requested-With")
                config.allowCredentials = false
                source.registerCorsConfiguration("/**", config)
                cors.configurationSource { source.getCorsConfiguration(it) }
            }

            // 3. Política de Sesión: STATELESS (Evita el Set-Cookie: JSESSIONID)
            .sessionManagement { 
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) 
            }

            .authorizeHttpRequests { auth ->
                // BYPASS TOTAL PARA GRABACIÓN DE VIDEO - TODO PERMITIDO
                auth.anyRequest().permitAll()
            }

            // 4. Basic Auth con EntryPoint Limpio (Evita WWW-Authenticate: Basic)
            .httpBasic { basic ->
                basic.authenticationEntryPoint { _, response, authException ->
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
                    response.contentType = "application/json"
                    response.writer.write("{\"error\": \"Unauthorized\", \"message\": \"${authException.message}\"}")
                }
            }

            .formLogin { it.disable() }
        
        return http.build()
    }
}
