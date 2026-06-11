pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    // Permitimos que cada proyecto maneje sus repositorios si quiere
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PointCheck"

// --- LÓGICA DE AISLAMIENTO POR IDE (MURALLA CHINA) ---
// Detectamos qué IDE está abriendo el proyecto para evitar conflictos de recursos.
val isAndroidStudio = System.getProperty("idea.platform.prefix")?.contains("AndroidStudio", ignoreCase = true) == true
val isIntelliJ = System.getProperty("idea.platform.prefix")?.contains("Idea", ignoreCase = true) == true

if (isAndroidStudio) {
    // En Android Studio: Cargamos solo la App para evitar que intente gestionar Spring Boot.
    println("📱 [Entorno] MODO ANDROID: Cargando exclusivamente módulo :app")
    include(":app")
} else if (isIntelliJ) {
    // En IntelliJ: Cargamos solo el Backend para que no intente controlar el emulador de Android.
    println("🚀 [Entorno] MODO BACKEND: Cargando exclusivamente módulo :backend")
    include(":backend")
} else {
    // Para terminal, CI/CD o si se requiere cargar todo el entorno.
    include(":app")
    include(":backend")
}
