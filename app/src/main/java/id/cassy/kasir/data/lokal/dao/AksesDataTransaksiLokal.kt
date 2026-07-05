package id.cassy.kasir.data.lokal.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import id.cassy.kasir.data.lokal.entitas.EntitasItemTransaksiLokal
import id.cassy.kasir.data.lokal.entitas.EntitasTransaksiLokal
import id.cassy.kasir.data.lokal.relasi.TransaksiDenganItemLokal
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) untuk mengelola operasi database riwayat transaksi.
 */
@Dao
interface AksesDataTransaksiLokal {

    /**
     * Menyimpan data utama transaksi.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpanTransaksi(
        transaksi: EntitasTransaksiLokal,
    )

    /**
     * Menyimpan daftar item transaksi secara massal.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpanDaftarItemTransaksi(
        daftarItem: List<EntitasItemTransaksiLokal>,
    )

    /**
     * Menghapus seluruh item transaksi berdasarkan ID transaksi induk.
     */
    @Query("DELETE FROM item_transaksi_lokal WHERE transaksiId = :transaksiId")
    suspend fun hapusItemTransaksiBerdasarkanIdTransaksi(
        transaksiId: String,
    )

    /**
     * Operasi atomik untuk menyimpan transaksi beserta seluruh itemnya.
     */
    @Transaction
    suspend fun simpanTransaksiDenganItem(
        transaksi: EntitasTransaksiLokal,
        daftarItem: List<EntitasItemTransaksiLokal>,
    ) {
        simpanTransaksi(transaksi)
        hapusItemTransaksiBerdasarkanIdTransaksi(transaksi.id)
        simpanDaftarItemTransaksi(daftarItem)
    }

    /**
     * Mengamati seluruh riwayat transaksi yang diurutkan dari yang terbaru.
     * Mengembalikan [Flow] untuk reaktivitas UI.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM transaksi_lokal
        ORDER BY waktuTransaksiEpochMili DESC
        """
    )
    fun amatiSemuaTransaksi(): Flow<List<TransaksiDenganItemLokal>>

    /**
     * Mengamati detail satu transaksi berdasarkan ID.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM transaksi_lokal
        WHERE id = :transaksiId
        LIMIT 1
        """
    )
    fun amatiTransaksiBerdasarkanId(
        transaksiId: String,
    ): Flow<TransaksiDenganItemLokal?>

    /**
     * Mengambil data transaksi satu kali (bukan aliran data).
     */
    @Transaction
    @Query(
        """
        SELECT * FROM transaksi_lokal
        WHERE id = :transaksiId
        LIMIT 1
        """
    )
    suspend fun ambilTransaksiBerdasarkanId(
        transaksiId: String,
    ): TransaksiDenganItemLokal?

    @Transaction
    @Query(
        """
        SELECT * FROM transaksi_lokal
        WHERE status = 'Pending'
        ORDER BY waktuTransaksiEpochMili DESC
        """
    )
    fun amatiTransaksiPending(): Flow<List<TransaksiDenganItemLokal>>

    @Query("DELETE FROM transaksi_lokal WHERE id = :transaksiId")
    suspend fun hapusTransaksiBerdasarkanId(transaksiId: String)

    @Query(
        """
        UPDATE transaksi_lokal
        SET status = :status, uangDibayar = :uangDibayar
        WHERE id = :id
        """
    )
    suspend fun perbaruiStatusDanUangDibayarTransaksi(
        id: String,
        status: String,
        uangDibayar: Long,
    )
}
