package id.flexi.kasir.data.network.mapping

import id.flexi.kasir.data.jsonVarian
import id.flexi.kasir.data.network.model.ProductNetworkResponse
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.model.Varian

/**
 * Mengubah model respons jaringan menjadi model domain.
 *
 * Mapper ini juga menormalisasi teks dari server agar data yang masuk ke
 * domain dan Room tidak membawa spasi pinggir atau kode pindai kosong.
 */
fun ProductNetworkResponse.keDomain(): Produk {
    return Produk(
        id = id.trim(),
        nama = nama.trim(),
        harga = harga,
        stokTersedia = stokTersedia,
        kodePindai = kodePindai
            ?.trim()
            ?.takeIf { kode -> kode.isNotBlank() },
        deskripsi = deskripsi
            ?.trim()
            .orEmpty(),
        aktif = aktif,
        kategori = kategori.trim(),
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
 * Mengubah daftar respons produk jaringan menjadi daftar produk domain.
 */
fun List<ProductNetworkResponse>.keDomain(): List<Produk> {
    return map { responsProduk ->
        responsProduk.keDomain()
    }
}
