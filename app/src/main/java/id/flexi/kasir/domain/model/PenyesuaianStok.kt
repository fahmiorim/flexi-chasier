package id.flexi.kasir.domain.model

/**
 * Jenis entitas yang disesuaikan stoknya.
 */
enum class StokJenis {
    /** Bahan baku (persediaan, bukan barang jual). */
    Bahan,

    /** Produk jadi (barang yang dijual). */
    Produk,
}

/**
 * Riwayat penyesuaian/reset stok (bahan maupun produk).
 *
 * Dicatat setiap kali stok diubah manual (Atur Stok/Reset Stok) sehingga
 * perubahan selalu bisa dilacak — termasuk sebelum/sesudah dan alasan.
 *
 * @property id Identitas unik penyesuaian.
 * @property jenis Apakah penyesuaian untuk Bahan atau Produk.
 * @property entitasId ID bahan/produk yang disesuaikan.
 * @property namaEntitas Nama entitas saat dicatat (snapshot untuk laporan).
 * @property stokSebelum Stok sebelum penyesuaian.
 * @property stokSesudah Stok setelah penyesuaian.
 * @property selisih stokSesudah − stokSebelum (negatif = pengurangan).
 * @property alasan Alasan penyesuaian (mis. "Stok fisik setelah opname").
 * @property waktu Epoch millis saat penyesuaian dicatat.
 */
data class PenyesuaianStok(
    val id: String,
    val jenis: StokJenis,
    val entitasId: String,
    val namaEntitas: String = "",
    val stokSebelum: Int,
    val stokSesudah: Int,
    val alasan: String = "",
    val waktu: Long = System.currentTimeMillis(),
) {
    /** selisih = stokSesudah − stokSebelum (positif = penambahan, negatif = pengurangan). */
    val selisih: Int get() = stokSesudah - stokSebelum
}
