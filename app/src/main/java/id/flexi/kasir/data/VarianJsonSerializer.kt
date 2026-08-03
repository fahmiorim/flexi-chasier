package id.flexi.kasir.data

import kotlinx.serialization.json.Json

/**
 * Instance Json bersama untuk serialisasi/deserialisasi daftar varian produk.
 *
 * Dipakai oleh mapper lokal dan jaringan agar konfigurasi serializer varian
 * konsisten di seluruh lapisan data.
 */
val jsonVarian = Json {
    ignoreUnknownKeys = true
}
