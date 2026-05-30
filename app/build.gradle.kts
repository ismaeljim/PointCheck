plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.pointcheck"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.pointcheck"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "1.2"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Jetpack Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))

    // Componentes base
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.9.0")

    // Jetpack Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Navegación y ViewModel
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Corrutinas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Coil
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Herramientas de depuración
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Google Maps & Location
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
}
