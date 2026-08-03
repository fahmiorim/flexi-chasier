package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.repository.ProductRepository
import id.flexi.kasir.domain.repository.TransactionRepository

/**
 * Kasus penggunaan untuk memperbarui status favorit produk secara otomatis
 * berdasarkan produk yang paling sering dipesan (top 10).
 *
 * Produk dalam daftar top 10 akan otomatis difavoritkan tanpa
 * menghapus status favorit produk lain yang sudah ada.
 */
class UpdatePopularFavorites(
    private val transactionRepository: TransactionRepository,
    private val productRepository: ProductRepository,
) {

    /**
     * Mengeksekusi perhitungan ulang favorit berdasarkan data pemesanan.
     *
     * @param batasJumlah Jumlah produk teratas yang difavoritkan (default 10).
     */
    suspend fun eksekusi(
        batasJumlah: Int = 10,
    ) {
        val idProdukTerpopuler = transactionRepository.ambilIdProdukTerpopuler(batasJumlah)
        if (idProdukTerpopuler.isEmpty()) return

        productRepository.tandaiProdukFavorit(idProdukTerpopuler)
    }
}
