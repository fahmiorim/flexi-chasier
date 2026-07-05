/**
 * Konfigurasi Manajemen Plugin dan Resolusi Dependensi Global.
 * Berkas ini mengatur sumber repositori dan struktur proyek utama.
 */
@file:Suppress("UnstableApiUsage")

pluginManagement {
    // Definisi repositori untuk mengunduh plugin Gradle
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Memastikan semua modul menggunakan repositori yang didefinisikan di sini
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Identitas Proyek Utama
rootProject.name = "CassyKasir"

// Daftar modul yang disertakan dalam proses build
include(":app")
