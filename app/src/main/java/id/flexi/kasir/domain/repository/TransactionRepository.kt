package id.flexi.kasir.domain.repository

import id.flexi.kasir.domain.model.PaymentMethod
import id.flexi.kasir.domain.model.TransactionStatus
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.model.Uang
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingData

/**
 * Kontrak repository Transaction untuk layer ranah.
 *
 * Use case hanya bergantung pada kontrak ini agar aturan bisnis Transaction
 * tidak menempel langsung ke implementasi Room.
 */
interface TransactionRepository {

    /**
     * Menyimpan Transaction baru beserta itemnya.
     *
     * Implementasi data wajib memastikan penyimpanan bersifat atomik agar
     * Transaction tidak tersimpan setengah.
     *
     * @param Transaction Transaction final yang akan dicatat.
     */
    suspend fun simpanTransaction(
        Transaction: Transaction,
    )

    /**
     * Menyimpan Transaction dan mengurangi stok produk terkait secara atomik.
     *
     * Implementasi wajib memastikan Transaction tidak tersimpan jika stok produk
     * tidak cukup, produk tidak aktif, atau produk tidak ditemukan.
     *
     * @param Transaction Transaction final yang akan dicatat.
     */
    suspend fun simpanTransactionDanKurangiStok(
        Transaction: Transaction,
    )

    /**
     * Menyimpan Transaction dengan penyesuaian stok berdasarkan delta.
     *
     * Membandingkan item lama dengan item baru:
     * - Item baru/ditambah jumlahnya → kurangi stok
     * - Item dihapus/dikurangi jumlahnya → tambah (restore) stok
     * - Item tidak berubah → tidak ada perubahan stok
     *
     * @param Transaction Transaction baru yang akan disimpan.
     * @param oldTransaction Transaction lama (untuk perbandingan), null jika baru.
     */
    suspend fun simpanTransactionDenganDeltaStok(
        Transaction: Transaction,
        oldTransaction: Transaction?,
    )

    /**
     * Mengamati semua Transaction yang sudah tersimpan.
     *
     * @return Aliran daftar Transaction dari sumber data aktif.
     */
    fun amatiSemuaTransaction(): Flow<List<Transaction>>

    /**
     * Mengamati satu Transaction berdasarkan identitas.
     *
     * @param identitasTransaction Identitas unik Transaction.
     * @return Aliran Transaction, atau null bila tidak ditemukan.
     */
    fun ObserveTransactionById(
        identitasTransaction: String,
    ): Flow<Transaction?>

    /**
     * Mengambil satu Transaction berdasarkan identitas secara sekali jalan.
     *
     * @param identitasTransaction Identitas unik Transaction.
     * @return Transaction bila ditemukan, atau null bila tidak ada.
     */
    suspend fun ambilTransactionBerdasarkanIdentitas(
        identitasTransaction: String,
    ): Transaction?

    fun amatiTransactionPending(): Flow<List<Transaction>>

    fun amatiTransactionDiproses(): Flow<List<Transaction>>

    fun amatiTransactionLunas(): Flow<List<Transaction>>

    suspend fun perbaruiStatusTransaction(
        identitasTransaction: String,
        status: TransactionStatus,
    )

    suspend fun hapusTransactionDanKembalikanStok(
        identitasTransaction: String,
    )

    suspend fun batalkanTransaction(
        identitasTransaction: String,
        alasan: String? = null,
    )

    suspend fun perbaruiStatusDanPaymentTransaction(
        identitasTransaction: String,
        status: TransactionStatus,
        uangDibayar: Uang,
        paymentMethod: PaymentMethod = PaymentMethod.Cash,
        waktuDibayarEpochMili: Long? = null,
    )

    suspend fun perbaruiStatusDanWaktuTransaction(
        identitasTransaction: String,
        status: TransactionStatus,
        waktuDiprosesEpochMili: Long? = null,
        waktuSelesaiEpochMili: Long? = null,
        waktuDibayarEpochMili: Long? = null,
    )

    suspend fun perbaruiWaktuSelesai(
        identitasTransaction: String,
        waktuSelesaiEpochMili: Long,
    )

    suspend fun perbaruiWaktuDibayar(
        identitasTransaction: String,
        waktuDibayarEpochMili: Long,
    )

    /**
     * Mengedit detail pembayaran transaksi (metode bayar, uang dibayar, catatan).
     * Digunakan untuk koreksi jika user salah input saat transaksi.
     */
    suspend fun perbaruiPaymentMethodTransaction(
        identitasTransaction: String,
        paymentMethod: PaymentMethod,
        uangDibayar: Long,
        catatan: String?,
    )

    suspend fun tandaiItemSelesai(identitasTransaction: String)

    suspend fun ambilNomorAntrianBerikutnya(): Int

    suspend fun pastikanDataAwalTersedia()

    /**
     * Mengambil daftar ID produk terpopuler berdasarkan total kuantitas pemesanan.
     *
     * @param batasJumlah Jumlah produk teratas yang diambil.
     * @return Daftar ID produk yang paling laris, diurutkan dari yang terbanyak.
     */
    suspend fun ambilIdProdukTerpopuler(
        batasJumlah: Int = 10,
    ): List<String>

    // ═══════════════════════════════════════
    // AGGREGATE — Optimasi performa
    // ═══════════════════════════════════════

    /** Mengambil daftar transaksi dalam rentang waktu (one-shot). */
    suspend fun ambilTransactionRentang(sejak: Long, sampai: Long): List<Transaction>

    /** Total penjualan QRIS (semua waktu). */
    fun hitungTotalQRISSemua(): Flow<Long>

    /** Total penjualan tunai sejak waktu tertentu. */
    fun hitungTotalTunaiSejak(sejak: Long): Flow<Long>

    /** Total penjualan QRIS sejak waktu tertentu. */
    fun hitungTotalQRISSejak(sejak: Long): Flow<Long>

    /** Total penjualan tunai dalam rentang waktu (suspend, one-shot). */
    suspend fun hitungTotalTunaiRentang(sejak: Long, sampai: Long): Long

    /** Total penjualan QRIS dalam rentang waktu (suspend, one-shot). */
    suspend fun hitungTotalQRISRentang(sejak: Long, sampai: Long): Long

    /** Riwayat transaksi lunas sejak waktu tertentu (untuk dashboard/chart). */
    fun amatiTransactionSejak(sejak: Long): Flow<List<Transaction>>

    // ═══════════════════════════════════════
    // PAGING 3 — Data bertahap
    // ═══════════════════════════════════════

    /** Riwayat transaksi dengan Paging 3 (filter tanggal via SQL). */
    fun amatiTransactionPaged(
        sejak: Long? = null,
        sampai: Long? = null,
    ): Flow<PagingData<Transaction>>
}
