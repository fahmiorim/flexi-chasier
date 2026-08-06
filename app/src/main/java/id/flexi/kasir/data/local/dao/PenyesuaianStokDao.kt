package id.flexi.kasir.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import id.flexi.kasir.data.local.entity.LocalPenyesuaianStokEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object untuk riwayat penyesuaian/reset stok.
 */
@Dao
interface PenyesuaianStokDao {

    /** Mengamati seluruh riwayat penyesuaian (terbaru dulu). */
    @Query("SELECT * FROM penyesuaian_stok ORDER BY waktu DESC")
    fun amatiSemua(): Flow<List<LocalPenyesuaianStokEntity>>

    /** Mengamati riwayat penyesuaian untuk satu entitas (produk/bahan). */
    @Query(
        "SELECT * FROM penyesuaian_stok WHERE jenis = :jenis AND entitasId = :entitasId ORDER BY waktu DESC",
    )
    fun amatiBerdasarkanEntitas(
        jenis: String,
        entitasId: String,
    ): Flow<List<LocalPenyesuaianStokEntity>>

    /** Menyimpan satu baris penyesuaian (insert/timpa). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpan(penyesuaian: LocalPenyesuaianStokEntity)

    /** Menghapus satu baris penyesuaian berdasarkan ID. */
    @Query("DELETE FROM penyesuaian_stok WHERE id = :id")
    suspend fun hapus(id: String)
}
