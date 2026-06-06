plugins {
    id("base")
    // Restaurado a versiones modernas para soportar Compose 1.7+ y Kotlin 2.0
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.jvm") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.spring") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.jpa") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("org.springframework.boot") version "3.3.1" apply false
    id("io.spring.dependency-management") version "1.1.5" apply false
}

// Tarea para evitar el error 'unitTestClasses' not found
tasks.register("unitTestClasses") {
    description = "Tarea de conveniencia para compilar clases de prueba en todos los subproyectos"
    group = "verification"
    subprojects.forEach { subproject ->
        // Agregamos dependencia a las tareas de compilaciÃ³n de pruebas si existen
        dependsOn(subproject.tasks.matching { 
            it.name == "testClasses" || it.name.contains("UnitTestClasses") 
        })
    }
}
