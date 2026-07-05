package id.cassy.kasir.data.jaringan.pemetaan

import id.cassy.kasir.data.jaringan.model.ResponsProdukJaringan
import id.cassy.kasir.ranah.model.Produk

/**
 * Mengubah model respons jaringan menjadi model domain.
 *
 * Mapper ini juga menormalisasi teks dari server agar data yang masuk ke
 * domain dan Room tidak membawa spasi pinggir atau kode pindai kosong.
 */
fun ResponsProdukJaringan.keDomain(): Produk {
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
    )
}

/**
 * Mengubah daftar respons produk jaringan menjadi daftar produk domain.
 */
fun List<ResponsProdukJaringan>.keDomain(): List<Produk> {
    return map { responsProduk ->
        responsProduk.keDomain()
    }
}
