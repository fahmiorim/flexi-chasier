/**
 * Konfigurasi Build Modul Aplikasi (:app).
 * Mengatur dependensi, versi SDK, dan aturan pengemasan aplikasi.
 */
import java.io.FileInputStream
import java.util.Properties

plugins {
    // Menerapkan plugin yang didefinisikan di tingkat root
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

// Kredensial signing release dibaca dari keystore.properties (TIDAK di-commit;
// di-ignore oleh git). Bila file tidak ada, build release tetap berjalan dan
// menghasilkan APK unsigned — aman untuk developer/kontributor. Signing aktif
// otomatis begitu keystore.properties tersedia.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    // Validasi isi: file rusak/tidak lengkap harus gagal dengan pesan jelas,
    // bukan error ClassCast/NPE yang membingungkan saat konfigurasi.
    val kunciWajib = listOf("storeFile", "storePassword", "keyAlias", "keyPassword")
    val kunciHilang = kunciWajib.filter { keystoreProperties.getProperty(it).isNullOrBlank() }
    if (kunciHilang.isNotEmpty()) {
        throw GradleException(
            "keystore.properties tidak lengkap — kunci hilang: ${kunciHilang.joinToString()}. " +
                "Perbaiki atau hapus file agar build berjalan unsigned.",
        )
    }
}

// Alamat API untuk build RILIS (produksi: https://api.flexinet.id/).
// Nilai diambil dari gradle.properties (ALAMAT_DASAR_API) dan bisa di-override
// per-bangun dengan: ./gradlew assembleRelease -PALAMAT_DASAR_API=https://api.lain.example
val alamatDasarApiRelease: String =
    (findProperty("ALAMAT_DASAR_API") as? String)?.trim()?.trimEnd('/')
        ?: "https://flexi-kasir-belum-dikonfigurasi.invalid"

android {
    // Namespace unik untuk identifikasi package di sistem Android
    namespace = "id.flexi.kasir"
    compileSdk = 36

    defaultConfig {
        applicationId = "id.flexi.kasir"
        minSdk = 26
        targetSdk = 36

        // ── Checklist rilis (WAJIB dipatuhi sebelum distribusi APK baru) ──
        // 1. versionCode HARUS lebih besar dari versi sebelumnya; jika sama,
        //    Android menolak install update. Sudah terpakai: 1 (v0.1.0), 2 (v0.1.1).
        // 2. Naikkan versionName mengikuti versi fitur (label saja).
        // 3. Build dengan keystore release SAMA (keystore.properties); tanda
        //    tangan berbeda = update ditolak / data dianggap aplikasi lain.
        // 4. Bump versionCode hanya sekali per rilis yang didistribusikan.
        versionCode = 6
        versionName = "0.3.0"


        // Optimasi ABI: Hanya menyertakan arsitektur yang relevan untuk efisiensi ukuran APK
        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }

        // Konfigurasi KSP untuk Room: Menentukan lokasi penyimpanan skema DB
        ksp {
            arg("room.schemaLocation", "$projectDir/skema-room")
        }
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                storeFile = rootProject.file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
            }
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
                "\"https://api.flexinet.id/\"",
            )
        }

        release {
            // Optimasi kode (ProGuard/R8) untuk rilis publik
            isMinifyEnabled = true // Diaktifkan untuk keamanan & efisiensi

            // Signing otomatis bila keystore.properties ada; sebaliknya APK
            // unsigned (identik dengan perilaku sebelum konfigurasi ini).
            signingConfig = if (keystorePropertiesFile.exists()) {
                signingConfigs.getByName("release")
            } else {
                null
            }

            buildConfigField(
                "String",
                "ALAMAT_DASAR_API",
                "\"$alamatDasarApiRelease/\"",
            )

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    // Fail-fast: menolak membangun APK RILIS yang masih memakai placeholder,
    // agar aplikasi produksi tidak diam-diam terhubung ke alamat tidak valid.
    // Task didaftarkan secara lazy (register) sehingga aman meski tidak ada
    // task preReleaseBuild bawaan AGP; dependensi ke assembleRelease ditambahkan
    // hanya jika task tersebut benar-benar dipanggil.
    // Fail-fast kompatibel configuration cache: properti dijadikan task input
    // (Gradle men-serialize nilainya, bukan referensi project).
    val alamatRilisProvider = providers.gradleProperty("ALAMAT_DASAR_API")
    val cekAlamatRilis by tasks.registering {
        inputs.property("alamatDasarApi", alamatRilisProvider)
        doLast {
            val alamat = alamatRilisProvider.getOrElse("").trim().trimEnd('/')
            if (alamat.isEmpty() || alamat.contains("belum-dikonfigurasi")) {
                throw GradleException(
                    "ALAMAT_DASAR_API belum diatur untuk build rilis. " +
                        "Gunakan: ./gradlew assembleRelease -PALAMAT_DASAR_API=https://api.domain.com " +
                        "atau set ALAMAT_DASAR_API di gradle.properties.",
                )
            }
        }
    }

    tasks.matching { it.name == "assembleRelease" }.configureEach {
        dependsOn(cekAlamatRilis)
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

    // WorkManager — Sinkronisasi latar belakang (outbox push + pull berkala)
    implementation(libs.androidx.work.runtime.ktx)

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
