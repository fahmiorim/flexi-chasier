package id.cassy.kasir.data.lokal.pemetaan

import id.cassy.kasir.data.lokal.entitas.EntitasProdukLokal
import id.cassy.kasir.ranah.model.Produk

/**
 * Mengubah entitas database lokal menjadi objek domain Produk.
 */
fun EntitasProdukLokal.keDomain(): Produk {
    return Produk(
        id = id,
        nama = nama,
        harga = harga,
        stokTersedia = stokTersedia,
        kodePindai = kodePindai,
        deskripsi = deskripsi,
        aktif = apakahAktif,
        kategori = kategori,
    )
}

/**
 * Mengubah objek domain Produk menjadi entitas database lokal.
 */
fun Produk.keLokal(): EntitasProdukLokal {
    return EntitasProdukLokal(
        id = id,
        nama = nama,
        harga = harga,
        stokTersedia = stokTersedia,
        kodePindai = kodePindai,
        deskripsi = deskripsi,
        apakahAktif = aktif,
        kategori = kategori,
    )
}
