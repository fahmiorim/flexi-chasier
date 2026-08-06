package id.flexi.kasir.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import id.flexi.kasir.data.local.entity.LocalMutasiRekeningEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object untuk mutasi rekening (saldo awal, pemasukan, penarikan).
 */
@Dao
interface MutasiRekeningDao {

    /** Mengamati seluruh mutasi rekening (terbaru dulu). */
    @Query("SELECT * FROM mutasi_rekening ORDER BY waktu DESC")
    fun amatiSemua(): Flow<List<LocalMutasiRekeningEntity>>

    /** Mengambil seluruh mutasi rekening (sekali jalan). */
    @Query("SELECT * FROM mutasi_rekening ORDER BY waktu DESC")
    suspend fun ambilSemua(): List<LocalMutasiRekeningEntity>

    /** Menghitung total nominal untuk satu tipe mutasi. */
    @Query("SELECT COALESCE(SUM(nominal), 0) FROM mutasi_rekening WHERE tipe = :tipe")
    suspend fun hitungTotal(tipe: String): Long

    /** Menyimpan satu baris mutasi rekening (insert/timpa). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpan(mutasi: LocalMutasiRekeningEntity)

    /** Menghapus satu baris mutasi berdasarkan ID. */
    @Query("DELETE FROM mutasi_rekening WHERE id = :id")
    suspend fun hapus(id: String)
}
