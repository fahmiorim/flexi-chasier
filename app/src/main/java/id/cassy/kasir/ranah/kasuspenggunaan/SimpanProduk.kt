package id.cassy.kasir.ranah.kasuspenggunaan

import id.cassy.kasir.ranah.model.Produk
import id.cassy.kasir.ranah.repositori.RepositoriProduk

/**
 * Kasus penggunaan untuk menyimpan atau memperbarui data produk.
 *
 * Melakukan validasi bisnis dasar sebelum data dikirim ke repositori.
 *
 * @property repositori Kontrak repositori produk.
 */
class SimpanProduk(
    private val repositori: RepositoriProduk,
) {

    /**
     * Mengeksekusi penyimpanan produk.
     *
     * @param produk Data produk yang akan disimpan.
     * @throws IllegalArgumentException Jika data produk tidak valid.
     */
    suspend fun eksekusi(produk: Produk) {
        val namaBersih = produk.nama.trim()

        if (namaBersih.isBlank()) {
            throw IllegalArgumentException("Nama produk tidak boleh kosong.")
        }

        if (produk.harga < 0) {
            throw IllegalArgumentException("Harga produk tidak boleh negatif.")
        }

        if (produk.stokTersedia < 0) {
            throw IllegalArgumentException("Stok produk tidak boleh negatif.")
        }

        val produkValid = produk.copy(
            nama = namaBersih,
            deskripsi = produk.deskripsi.trim(),
        )

        repositori.simpanProduk(produkValid)
    }
}
