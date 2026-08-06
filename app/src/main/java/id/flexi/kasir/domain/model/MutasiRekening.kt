package id.flexi.kasir.domain.model

/**
 * Tipe mutasi rekening.
 */
enum class MutasiRekeningTipe {
    /** Saldo awal rekening (modal awal) — ditetapkan sekali, menambah saldo. */
    SaldoAwal,

    /** Uang masuk ke rekening (selain QRIS otomatis). */
    Pemasukan,

    /** Uang keluar/penarikan dari rekening. */
    Penarikan,
}

/**
 * Mutasi rekening (saldo awal, pemasukan, penarikan).
 *
 * Satu akun per gerai; saldo akhir = Σ SaldoAwal + Σ Pemasukan − Σ Penarikan.
 * Setoran & transaksi QRIS otomatis menambah saldo (dicatat server sebagai
 * mutasi QRIS report-only — tidak disinkronkan sebagai baris mutasi manual).
 *
 * @property id Identitas unik mutasi.
 * @property tipe Jenis mutasi.
 * @property nominal Jumlah uang yang dimutasikan.
 * @property catatan Deskripsi mutasi.
 * @property waktu Epoch millis saat mutasi dicatat.
 */
data class MutasiRekening(
    val id: String,
    val tipe: MutasiRekeningTipe,
    val nominal: Uang,
    val catatan: String = "",
    val waktu: Long = System.currentTimeMillis(),
)
