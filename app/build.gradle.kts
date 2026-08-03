/**
 * Konfigurasi Build Modul Aplikasi (:app).
 * Mengatur dependensi, versi SDK, dan aturan pengemasan aplikasi.
 */
plugins {
    // Menerapkan plugin yang didefinisikan di tingkat root
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    // Namespace unik untuk identifikasi package di sistem Android
    namespace = "id.flexi.kasir"
    compileSdk = 36

    defaultConfig {
        applicationId = "id.flexi.kasir"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"


        // Optimasi ABI: Hanya menyertakan arsitektur yang relevan untuk efisiensi ukuran APK
        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }

        // Konfigurasi KSP untuk Room: Menentukan lokasi penyimpanan skema DB
        ksp {
            arg("room.schemaLocation", "$projectDir/skema-room")
        }
    }

    buildTypes {
        debug {
            // Memberikan pembeda visual/sistem untuk versi pengembangan
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"

            buildConfigField(
                "String",
                "ALAMAT_DASAR_API",
                "\"http://10.0.2.2:4000/\"",
            )
        }

        release {
            // Optimasi kode (ProGuard/R8) untuk rilis publik
            isMinifyEnabled = true // Diaktifkan untuk keamanan & efisiensi

            buildConfigField(
                "String",
                "ALAMAT_DASAR_API",
                "\"https://flexi-kasir-belum-dikonfigurasi.invalid/\"",
            )

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        // Mengaktifkan dukungan Jetpack Compose
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // Resolusi bentrok pustaka grafis eksternal
            pickFirsts += "**/libandroidx.graphics.path.so"
        }
        jniLibs {
            // Mempertahankan simbol debug untuk analisis crash di pustaka tertentu
            keepDebugSymbols += "**/libandroidx.graphics.path.so"
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    // Pustaka Dasar Android & Kotlin
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose BOM (Bill of Materials): Menjamin konsistensi versi antar komponen Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.google.android.material)

    // Arsitektur & Navigasi
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.compose.foundation)

    // Persistensi Data Lokal (Room & DataStore)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)

    // Paging 3 — Loading data bertahap untuk performa
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)

    // Pengujian lokal JVM untuk logika domain murni.
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.core)

    // Dependensi Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
}
// Trigger sinkronisasi IDE
