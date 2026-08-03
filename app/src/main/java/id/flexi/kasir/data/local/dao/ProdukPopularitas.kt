package id.flexi.kasir.data.local.dao

/**
 * Hasil query agregat untuk menghitung popularitas produk.
 *
 * @property produkId ID unik produk yang dipesan.
 * @property totalPesanan Total kuantitas pemesanan produk ini.
 */
data class ProdukPopularitas(
    val produkId: String,
    val totalPesanan: Int,
)
