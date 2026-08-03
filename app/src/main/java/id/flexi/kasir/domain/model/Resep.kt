package id.flexi.kasir.domain.model

/**
 * Resep yang menghubungkan produk dengan komposisi bahan bakunya.
 *
 * @property id Identitas unik resep.
 * @property produkId Identitas produk yang menggunakan resep ini (FK ke Produk).
 * @property varianNama Nama varian produk (null jika produk tanpa varian).
 * @property daftarBahan Daftar bahan dan jumlahnya.
 * @property createdAt Waktu (epoch millis) resep dibuat.
 */
data class Resep(
    val id: String,
    val produkId: String,
    val varianNama: String? = null,
    val daftarBahan: List<BahanResep> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
)

/**
 * Satu baris bahan dalam resep.
 *
 * @property id Identitas unik baris bahan.
 * @property resepId Identitas resep induk.
 * @property bahanId Identitas bahan yang digunakan.
 * @property jumlah Jumlah bahan yang diperlukan.
 * @property satuan Satuan jumlah (e.g., "gram", "ml", "pcs").
 */
data class BahanResep(
    val id: String = "",
    val resepId: String = "",
    val bahanId: String,
    val jumlah: Double,
    val satuan: String = "gram",
)
