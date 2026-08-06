package id.flexi.kasir.domain.model

/**
 * Mencatat pembelian bahan baku.
 *
 * @property id Identitas unik pembelian.
 * @property bahanId Identitas bahan yang dibeli (FK ke Bahan).
 * @property jumlah Jumlah yang dibeli (dalam [satuanBeli]).
 * @property satuanBeli Satuan saat membeli (e.g., "bungkus", "dus", "kg").
 * @property totalHarga Total harga pembelian dalam Rupiah.
 * @property tanggalBeli Waktu (epoch millis) pembelian dilakukan.
 * @property catatan Catatan opsional pembelian.
 */
data class PembelianBahan(
    val id: String,
    val bahanId: String,
    val jumlah: Double,
    val satuanBeli: String = "pcs",
    val totalHarga: Long,
    val tanggalBeli: Long = System.currentTimeMillis(),
    val catatan: String? = null,
    /** ID mutasi kas BelanjaBahan terkait (dibatalkan saat pembelian dihapus). */
    val mutasiKasId: String? = null,
)
