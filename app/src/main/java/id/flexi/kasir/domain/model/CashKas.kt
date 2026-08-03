package id.flexi.kasir.domain.model

/**
 * Status kas (buka/tutup).
 */
enum class CashKasStatus {
    /** Kas sedang berjalan (sudah dibuka) */
    Buka,
    /** Kas sudah ditutup */
    Tutup,
}

/**
 * Model domain untuk sesi kas.
 *
 * Mencatat periode buka-tutup laci kas, saldo awal, dan saldo akhir
 * untuk rekonsiliasi kas harian.
 *
 * @property id Identitas unik kas.
 * @property saldoAwal Uang modal awal di laci saat buka kas.
 * @property saldoAkhir Saldo fisik akhir saat tutup kas (null jika belum tutup).
 * @property waktuBuka Epoch millis saat kas dibuka.
 * @property waktuTutup Epoch millis saat kas ditutup (null jika belum tutup).
 * @property status Status kas (Buka/Tutup).
 * @property catatanBuka Catatan saat membuka kas.
 * @property catatanTutup Catatan saat menutup kas.
 */
data class CashKas(
    val id: String,
    val saldoAwal: Uang,
    val saldoAkhir: Uang? = null,
    val waktuBuka: Long,
    val waktuTutup: Long? = null,
    val status: CashKasStatus = CashKasStatus.Buka,
    val catatanBuka: String? = null,
    val catatanTutup: String? = null,
)
