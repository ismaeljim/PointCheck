package com.duoc.app.core.config

import com.duoc.app.features.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
import javax.sql.DataSource

/**
 * SENIOR ARCHITECTURE SOLUTION: Inicialización Controlada e Idempotente.
 * 
 * ¿POR QUÉ ESTO?: 
 * Anteriormente, Spring Boot ejecutaba data.sql en cada inicio (spring.sql.init.mode=always), 
 * lo que generaba ráfagas de errores "Duplicate entry" en los logs al intentar re-insertar 
 * registros existentes. Esto ocultaba errores reales y degradaba la experiencia de depuración.
 * 
 * SOLUCIÓN:
 * 1. Desactivamos la inicialización automática por script en application.properties.
 * 2. Implementamos este Initializer programático que verifica el estado de la DB (userRepository.count()).
 * 3. Solo si la base de datos está virgen, se gatilla el ResourceDatabasePopulator.
 * 
 * BENEFICIO: Logs limpios, inicio más rápido y garantía de integridad de datos.
 */
@Configuration
class DatabaseInitializer(
    private val userRepository: UserRepository,
    private val dataSource: DataSource
) {
    private val logger = LoggerFactory.getLogger(DatabaseInitializer::class.java)

    @Bean
    fun initDatabase(): CommandLineRunner {
        return CommandLineRunner {
            if (userRepository.count() == 0L) {
                logger.info("Base de datos vacía. Iniciando carga de datos inicial (Seed)...")
                try {
                    val resource = ClassPathResource("data.sql")
                    if (resource.exists()) {
                        val populator = ResourceDatabasePopulator(resource)
                        populator.execute(dataSource)
                        logger.info("Seed de datos completado exitosamente.")
                    } else {
                        logger.warn("Archivo data.sql no encontrado en resources.")
                    }
                } catch (e: Exception) {
                    logger.error("Error al ejecutar el seed de datos: ${e.message}", e)
                }
            } else {
                logger.info("Base de datos ya contiene datos. Omitiendo inicialización SQL.")
            }
        }
    }
}
