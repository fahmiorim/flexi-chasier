package id.flexi.kasir.data.local.mapping

import id.flexi.kasir.data.jsonVarian
import id.flexi.kasir.data.local.entity.LocalProductEntity
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.model.Varian
import kotlinx.serialization.encodeToString

/**
 * Mengubah entitas database lokal menjadi objek domain Produk.
 */
fun LocalProductEntity.keDomain(): Produk {
    return Produk(
        id = id,
        nama = nama,
        harga = harga,
        stokTersedia = stokTersedia,
        kodePindai = kodePindai,
        deskripsi = deskripsi,
        aktif = apakahAktif,
        kategori = kategori,
        fotoUri = fotoUri,
        favorit = favorit,
        hargaModal = hargaModal,
        varian = varianJson?.let { json ->
            try {
                jsonVarian.decodeFromString<List<Varian>>(json)
            } catch (_: Exception) {
                emptyList()
            }
        } ?: emptyList(),
        apakahStokDiaktifkan = apakahStokDiaktifkan,
    )
}

/**
 * Mengubah objek domain Produk menjadi entitas database lokal.
 */
fun Produk.keLokal(): LocalProductEntity {
    return LocalProductEntity(
        id = id,
        nama = nama,
        harga = harga,
        stokTersedia = stokTersedia,
        kodePindai = kodePindai,
        deskripsi = deskripsi,
        apakahAktif = aktif,
        kategori = kategori,
        fotoUri = fotoUri,
        favorit = favorit,
        hargaModal = hargaModal,
        varianJson = if (varian.isNotEmpty()) {
            try {
                jsonVarian.encodeToString(varian)
            } catch (_: Exception) {
                null
            }
        } else {
            null
        },
        apakahStokDiaktifkan = apakahStokDiaktifkan,
    )
}
