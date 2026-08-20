package id.flexi.kasir.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import id.flexi.kasir.data.local.entity.LocalTransactionItemEntity
import id.flexi.kasir.data.local.entity.LocalTransactionEntity
import id.flexi.kasir.data.local.relation.TransactionWithLocalItems
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingSource

/**
 * Data Access Object (DAO) untuk mengelola operasi database riwayat Transaction.
 */
@Dao
interface LocalTransactionDao {

    /**
     * Menyimpan data utama Transaction.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpanTransaction(
        Transaction: LocalTransactionEntity,
    )

    /**
     * Menyimpan daftar item Transaction secara massal.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpanDaftarItemTransaction(
        daftarItem: List<LocalTransactionItemEntity>,
    )

    /**
     * Menghapus seluruh item Transaction berdasarkan ID Transaction induk.
     */
    @Query("DELETE FROM item_Transaction_lokal WHERE TransactionId = :TransactionId")
    suspend fun hapusItemTransactionBerdasarkanIdTransaction(
        TransactionId: String,
    )

    /**
     * Operasi atomik untuk menyimpan Transaction beserta seluruh itemnya.
     */
    @Transaction
    suspend fun simpanTransactionDenganItem(
        Transaction: LocalTransactionEntity,
        daftarItem: List<LocalTransactionItemEntity>,
    ) {
        simpanTransaction(Transaction)
        hapusItemTransactionBerdasarkanIdTransaction(Transaction.id)
        simpanDaftarItemTransaction(daftarItem)
    }

    /**
     * Mengamati seluruh riwayat Transaction yang diurutkan dari yang terbaru.
     * Mengembalikan [Flow] untuk reaktivitas UI.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM Transaction_lokal
        ORDER BY waktuTransactionEpochMili DESC
        """
    )
    fun amatiSemuaTransaction(): Flow<List<TransactionWithLocalItems>>

    /**
     * Mengamati detail satu Transaction berdasarkan ID.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM Transaction_lokal
        WHERE id = :TransactionId
        LIMIT 1
        """
    )
    fun amatiTransactionBerdasarkanId(
        TransactionId: String,
    ): Flow<TransactionWithLocalItems?>

    /**
     * Mengambil data Transaction satu kali (bukan aliran data).
     */
    @Transaction
    @Query(
        """
        SELECT * FROM Transaction_lokal
        WHERE id = :TransactionId
        LIMIT 1
        """
    )
    suspend fun ambilTransactionBerdasarkanId(
        TransactionId: String,
    ): TransactionWithLocalItems?

    @Transaction
    @Query(
        """
        SELECT * FROM Transaction_lokal
        WHERE status = 'Pending'
        ORDER BY waktuTransactionEpochMili ASC
        """
    )
    fun amatiTransactionPending(): Flow<List<TransactionWithLocalItems>>

    @Transaction
    @Query(
        """
        SELECT * FROM Transaction_lokal
        WHERE status = 'Processing'
        ORDER BY waktuTransactionEpochMili ASC
        """
    )
    fun amatiTransactionDiproses(): Flow<List<TransactionWithLocalItems>>

    @Transaction
    @Query(
        """
        SELECT * FROM Transaction_lokal
        WHERE status != 'Pending'
        ORDER BY waktuTransactionEpochMili DESC
        """
    )
    fun amatiTransactionLunas(): Flow<List<TransactionWithLocalItems>>

    @Query("DELETE FROM Transaction_lokal WHERE id = :TransactionId")
    suspend fun hapusTransactionBerdasarkanId(TransactionId: String)

    @Query(
        """
        UPDATE Transaction_lokal
        SET status = :status, uangDibayar = :uangDibayar, PaymentMethod = :paymentMethod,
            waktuDibayarEpochMili = :waktuDibayar
        WHERE id = :id
        """
    )
    suspend fun perbaruiStatusDanUangDibayarTransaction(
        id: String,
        status: String,
        uangDibayar: Long,
        paymentMethod: String = "Cash",
        waktuDibayar: Long,
    )

    @Query("UPDATE Transaction_lokal SET status = :status WHERE id = :id")
    suspend fun perbaruiStatusTransaction(
        id: String,
        status: String,
    )

    /**
     * Menjaga versi entity tetap monotonik setelah update parsial yang mengubah
     * field bersama (status/pembayaran). Hanya menaikkan (tidak menurunkan).
     */
    @Query("UPDATE Transaction_lokal SET versi = :versi WHERE id = :id AND versi < :versi")
    suspend fun perbaruiVersiTransaction(
        id: String,
        versi: Long,
    )

    @Query("UPDATE item_Transaction_lokal SET apakahSelesai = 1 WHERE TransactionId = :transactionId")
    suspend fun tandaiItemSelesai(transactionId: String)

    @Query("UPDATE Transaction_lokal SET waktuDiprosesEpochMili = :waktu WHERE id = :id")
    suspend fun perbaruiWaktuDiproses(id: String, waktu: Long)

    @Query("UPDATE Transaction_lokal SET waktuSelesaiEpochMili = :waktu WHERE id = :id")
    suspend fun perbaruiWaktuSelesai(id: String, waktu: Long)

    @Query("UPDATE Transaction_lokal SET dibatalkan = 1, alasanPembatalan = :alasan WHERE id = :id")
    suspend fun tandaiDibatalkan(id: String, alasan: String? = null)

    @Query("UPDATE Transaction_lokal SET waktuDibayarEpochMili = :waktu WHERE id = :id")
    suspend fun perbaruiWaktuDibayar(id: String, waktu: Long)

    @Query(
        """
        UPDATE Transaction_lokal
        SET PaymentMethod = :paymentMethod, uangDibayar = :uangDibayar, catatan = :catatan
        WHERE id = :id
        """
    )
    suspend fun perbaruiPaymentMethodTransaction(
        id: String,
        paymentMethod: String,
        uangDibayar: Long,
        catatan: String?,
    )

    @Query(
        """
        SELECT COALESCE(MAX(nomorAntrian), 0) FROM Transaction_lokal
        WHERE waktuTransactionEpochMili >= :awalHariEpochMili
        AND waktuTransactionEpochMili < :akhirHariEpochMili
        """
    )
    suspend fun ambilNomorAntrianMaksHariIni(
        awalHariEpochMili: Long,
        akhirHariEpochMili: Long,
    ): Int

    /**
     * Mengambil daftar ID produk yang paling sering dipesan, diurutkan dari yang terbanyak.
     *
     * @param batasJumlah Jumlah produk teratas yang diambil (default 10).
     * @return Daftar ID produk yang diurutkan berdasarkan total kuantitas pemesanan.
     */
    @Query(
        """
        SELECT produkId, SUM(jumlah) as totalPesanan
        FROM item_Transaction_lokal
        GROUP BY produkId
        ORDER BY totalPesanan DESC
        LIMIT :batasJumlah
        """
    )
    suspend fun ambilIdProdukTerpopuler(
        batasJumlah: Int = 10,
    ): List<ProdukPopularitas>

    // ═══════════════════════════════════════
    // AGGREGATE QUERIES — Optimasi performa
    // ═══════════════════════════════════════

    /**
     * Total penjualan QRIS (lunas, Qris, tidak dibatalkan) untuk semua waktu.
     */
    @Query(
        """
        SELECT COALESCE(SUM(
            COALESCE(i.item_subtotal, 0) - t.potongan + t.biayaLayanan + t.pajak
        ), 0)
        FROM Transaction_lokal t
        LEFT JOIN (
            SELECT TransactionId, SUM(hargaProduk * jumlah) AS item_subtotal
            FROM item_Transaction_lokal
            GROUP BY TransactionId
        ) i ON t.id = i.TransactionId
        WHERE t.status != 'Pending' AND t.PaymentMethod = 'Qris' AND t.dibatalkan = 0
        """
    )
    fun hitungTotalQRISSemua(): Flow<Long>

    /**
     * Total penjualan tunai sejak waktu tertentu.
     */
    @Query(
        """
        SELECT COALESCE(SUM(
            COALESCE(i.item_subtotal, 0) - t.potongan + t.biayaLayanan + t.pajak
        ), 0)
        FROM Transaction_lokal t
        LEFT JOIN (
            SELECT TransactionId, SUM(hargaProduk * jumlah) AS item_subtotal
            FROM item_Transaction_lokal
            GROUP BY TransactionId
        ) i ON t.id = i.TransactionId
        WHERE t.status != 'Pending' AND t.PaymentMethod = 'Cash' AND t.dibatalkan = 0
        AND COALESCE(t.waktuDibayarEpochMili, t.waktuTransactionEpochMili) >= :sejak
        """
    )
    fun hitungTotalTunaiSejak(sejak: Long): Flow<Long>

    /**
     * Total penjualan QRIS sejak waktu tertentu.
     */
    @Query(
        """
        SELECT COALESCE(SUM(
            COALESCE(i.item_subtotal, 0) - t.potongan + t.biayaLayanan + t.pajak
        ), 0)
        FROM Transaction_lokal t
        LEFT JOIN (
            SELECT TransactionId, SUM(hargaProduk * jumlah) AS item_subtotal
            FROM item_Transaction_lokal
            GROUP BY TransactionId
        ) i ON t.id = i.TransactionId
        WHERE t.status != 'Pending' AND t.PaymentMethod = 'Qris' AND t.dibatalkan = 0
        AND COALESCE(t.waktuDibayarEpochMili, t.waktuTransactionEpochMili) >= :sejak
        """
    )
    fun hitungTotalQRISSejak(sejak: Long): Flow<Long>

    /**
     * Total penjualan tunai dalam rentang waktu tertentu.
     */
    @Query(
        """
        SELECT COALESCE(SUM(
            COALESCE(i.item_subtotal, 0) - t.potongan + t.biayaLayanan + t.pajak
        ), 0)
        FROM Transaction_lokal t
        LEFT JOIN (
            SELECT TransactionId, SUM(hargaProduk * jumlah) AS item_subtotal
            FROM item_Transaction_lokal
            GROUP BY TransactionId
        ) i ON t.id = i.TransactionId
        WHERE t.status != 'Pending' AND t.PaymentMethod = 'Cash' AND t.dibatalkan = 0
        AND COALESCE(t.waktuDibayarEpochMili, t.waktuTransactionEpochMili) >= :sejak
        AND COALESCE(t.waktuDibayarEpochMili, t.waktuTransactionEpochMili) < :sampai
        """
    )
    suspend fun hitungTotalTunaiRentang(sejak: Long, sampai: Long): Long

    /**
     * Total penjualan QRIS dalam rentang waktu tertentu.
     */
    @Query(
        """
        SELECT COALESCE(SUM(
            COALESCE(i.item_subtotal, 0) - t.potongan + t.biayaLayanan + t.pajak
        ), 0)
        FROM Transaction_lokal t
        LEFT JOIN (
            SELECT TransactionId, SUM(hargaProduk * jumlah) AS item_subtotal
            FROM item_Transaction_lokal
            GROUP BY TransactionId
        ) i ON t.id = i.TransactionId
        WHERE t.status != 'Pending' AND t.PaymentMethod = 'Qris' AND t.dibatalkan = 0
        AND COALESCE(t.waktuDibayarEpochMili, t.waktuTransactionEpochMili) >= :sejak
        AND COALESCE(t.waktuDibayarEpochMili, t.waktuTransactionEpochMili) < :sampai
        """
    )
    suspend fun hitungTotalQRISRentang(sejak: Long, sampai: Long): Long

    /**
     * Mengambil daftar transaksi dalam rentang waktu (one-shot).
     */
    @Transaction
    @Query(
        """
        SELECT * FROM Transaction_lokal
        WHERE status != 'Pending'
        AND waktuTransactionEpochMili >= :sejak
        AND waktuTransactionEpochMili < :sampai
        ORDER BY waktuTransactionEpochMili DESC
        """
    )
    suspend fun ambilTransactionRentang(sejak: Long, sampai: Long): List<TransactionWithLocalItems>

    /**
     * Mengamati transaksi lunas sejak waktu tertentu.
     * Untuk dashboard: chart, produk terlaris, waktu tunggu.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM Transaction_lokal
        WHERE status != 'Pending' AND waktuTransactionEpochMili >= :sejak
        ORDER BY waktuTransactionEpochMili DESC
        """
    )
    fun amatiTransactionSejak(sejak: Long): Flow<List<TransactionWithLocalItems>>

    // ═══════════════════════════════════════
    // PAGING 3 — Loading data bertahap
    // ═══════════════════════════════════════

    @Transaction
    @Query(
        """
        SELECT * FROM Transaction_lokal
        WHERE status != 'Pending'
        AND (:sejak IS NULL OR waktuTransactionEpochMili >= :sejak)
        AND (:sampai IS NULL OR waktuTransactionEpochMili < :sampai)
        ORDER BY waktuTransactionEpochMili DESC
        """
    )
    fun amatiTransactionPaged(
        sejak: Long?,
        sampai: Long?,
    ): PagingSource<Int, TransactionWithLocalItems>
}
