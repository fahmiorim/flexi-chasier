package id.flexi.kasir.domain.model

/**
 * Tipe mutasi kas.
 */
enum class CashMutationType {
    /** Uang masuk ke kas (setoran, penjualan tunai) */
    Pemasukan,
    /** Uang keluar dari kas (belanja, transport, dll) */
    Pengeluaran,
}

/**
 * Kategori pengeluaran kas.
 */
enum class CashExpenseCategory(val label: String) {
    BelanjaBahan(label = "Belanja Bahan"),
    Transportasi(label = "Transportasi"),
    ListrikAir(label = "Listrik & Air"),
    Sewa(label = "Sewa"),
    GajiKaryawan(label = "Gaji Karyawan"),
    Lainnya(label = "Lainnya"),
    ;

    companion object {
        fun fromString(value: String): CashExpenseCategory =
            entries.firstOrNull { it.name == value } ?: Lainnya
    }
}

/**
 * Model domain untuk mutasi kas.
 *
 * Mencatat setiap uang yang keluar atau masuk dari laci kas selama
 * satu shift berlangsung. Digunakan untuk rekonsiliasi saat tutup kas.
 *
 * @property id Identitas unik mutasi.
 * @property shiftId ID shift induk tempat mutasi terjadi.
 * @property tipe Jenis mutasi (Pemasukan/Pengeluaran).
 * @property kategori Kategori pengeluaran (hanya relevan untuk Pengeluaran).
 * @property nominal Jumlah uang yang dimutasikan.
 * @property catatan Deskripsi mutasi.
 * @property waktu Epoch millis saat mutasi dicatat.
 */
data class CashMutation(
    val id: String,
    val shiftId: String,
    val tipe: CashMutationType,
    val kategori: CashExpenseCategory = CashExpenseCategory.Lainnya,
    val nominal: Uang,
    val catatan: String = "",
    val waktu: Long,
)
