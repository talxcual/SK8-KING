// build.gradle.kts (Module: app)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Se elimina: alias(libs.plugins.kotlin.compose) - Ya no es necesario si no usas Compose UI
    id("com.google.gms.google-services")
    // Si estás usando KSP (Kotlin Symbol Processing) para procesadores de anotaciones como los de Glide,
    // asegúrate de que el plugin esté aquí. Por ahora, asumiremos 'annotationProcessor' es suficiente.
    // id("com.google.devtools.ksp") version "1.9.23-1.0.20" // Descomenta y ajusta si usas KSP
}

android {
    namespace = "com.Ktoledo.pissdrunxking"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.Ktoledo.pissdrunxking"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    // ***************************************************************
    // CAMBIO IMPORTANTE AQUÍ: Actualizado a Java 11 para eliminar advertencias
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11" // Coincide con Java 11
    }
    // ***************************************************************
    buildFeatures {
        // Se elimina: compose = true - Ya no es necesario si no usas Compose UI
        viewBinding = true // Si usas View Binding, asegúrate de que esté habilitado
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Dependencias de Compose UI (todas eliminadas/comentadas si no se usan)
    // implementation(libs.androidx.activity.compose)
    // implementation(platform(libs.androidx.compose.bom))
    // implementation(libs.androidx.ui)
    // implementation(libs.androidx.ui.graphics)
    // implementation(libs.androidx.ui.tooling.preview)
    // implementation(libs.androidx.material3) // Material3 es para Compose; 'material' es para Views

    // Dependencias para el sistema de Views tradicional (XML)
    implementation(libs.androidx.appcompat) // Para compatibilidad de la barra de acción, etc.
    implementation(libs.material) // Para componentes de Material Design en Views (como TabLayout)
    implementation(libs.androidx.activity) // Para Activity extensibles
    implementation(libs.androidx.constraintlayout) // Para ConstraintLayout

    // Versión actual a 2025-06-02 (o la más reciente según Firebase BoM)
    implementation(platform(libs.firebase.bom))
    // Asegúrate de usar las dependencias específicas de Firebase que necesites, ejemplo:
    // implementation("com.google.firebase:firebase-firestore-ktx")
    // implementation("com.google.firebase:firebase-storage-ktx")


    // ¡¡¡AÑADE ESTA LÍNEA PARA GSON!!!
    implementation(libs.gson)

    implementation(libs.androidx.recyclerview) // Para RecyclerView
    implementation(libs.androidx.cardview) // Para CardView
    implementation(libs.firebase.auth.ktx) // Para autenticación de Firebase

    // AÑADIDO: Dependencia para Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.2.0") // O la última versión estable

    // GLIDE - AÑADIDO PARA LA CARGA DE IMÁGENES
    implementation ("com.github.bumptech.glide:glide:4.16.0") // Última versión estable
    // Para procesador de anotaciones, si no usas KSP, asegúrate de que KAPT esté configurado si es necesario.
    // Si estás usando KSP, reemplazarías 'annotationProcessor' con 'ksp'
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    // Si usas KSP, descomenta esta línea y comenta la de arriba:
    // ksp ("com.github.bumptech.glide:compiler:4.16.0")


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Firebase Firestore
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Dependencias de testing para Compose (eliminadas/comentadas si no se usan)
    // androidTestImplementation(platform(libs.androidx.compose.bom))
    // androidTestImplementation(libs.androidx.ui.test.junit4)
    // debugImplementation(libs.androidx.ui.tooling)
    // debugImplementation(libs.androidx.ui.test.manifest)
}