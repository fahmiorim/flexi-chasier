package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.repository.ProductRepository

/**
 * Kasus penggunaan untuk menyimpan atau memperbarui data produk.
 *
 * Melakukan validasi bisnis dasar sebelum data dikirim ke repositori.
 *
 * @property repositori Kontrak repositori produk.
 */
class SaveProduct(
    private val repositori: ProductRepository,
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

        // Harga boleh 0 jika ada varian dengan harga
        if (produk.harga == 0L && produk.varian.isEmpty()) {
            throw IllegalArgumentException("Harga produk harus diisi jika tidak ada varian.")
        }

        // Pastikan varian unik (tidak ada duplikat nama)
        val namaVarian = produk.varian.map { it.nama.trim().uppercase() }
        if (namaVarian.size != namaVarian.distinct().size) {
            throw IllegalArgumentException("Nama varian tidak boleh duplikat.")
        }

        val produkValid = produk.copy(
            nama = namaBersih,
            deskripsi = produk.deskripsi.trim(),
        )

        repositori.SaveProduct(produkValid)
    }
}
