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

// Solo incluimos la App aquí para que Android Studio no se pelee con el Backend
include(":app")
