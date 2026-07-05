/**
 * Konfigurasi build tingkat proyek.
 *
 * Plugin di sini disediakan untuk modul, tetapi tidak langsung diterapkan
 * pada tingkat root.
 */
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
}
