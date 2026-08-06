package id.flexi.kasir.domain.model

/**
 * Bahan baku yang digunakan dalam resep produk.
 *
 * @property id Identitas unik bahan.
 * @property nama Nama bahan baku (e.g., "SKM", "Cremer", "Gula Aren").
 * @property satuan Satuan ukuran (e.g., "gram", "ml", "pcs", "botol").
 * @property stokTersedia Jumlah stok yang tersedia dalam satuan terkecil.
 * @property hargaPerSatuan Harga per satuan dalam Rupiah, dihitung otomatis dari pembelian terakhir.
 * @property stokMinimum Ambang stok minimum (0 = tidak diberlakukan), diisi lewat web.
 * @property aktif Status aktif/nonaktif bahan, diatur lewat web.
 * @property createdAt Waktu (epoch millis) bahan pertama kali dibuat.
 */
data class Bahan(
    val id: String,
    val nama: String,
    val satuan: String = "pcs",
    val stokTersedia: Double = 0.0,
    val hargaPerSatuan: Long = 0L,
    val stokMinimum: Int = 0,
    val aktif: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
)
