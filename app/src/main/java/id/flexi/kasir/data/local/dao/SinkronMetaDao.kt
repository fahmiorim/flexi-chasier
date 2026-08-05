package id.flexi.kasir.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Akses data metadata sinkronisasi (key-value).
 */
@Dao
interface SinkronMetaDao {

    @Query("SELECT nilai FROM meta_sinkron WHERE kunci = :kunci LIMIT 1")
    suspend fun ambil(kunci: String): String?

    @Query(
        """
        INSERT INTO meta_sinkron (kunci, nilai) VALUES (:kunci, :nilai)
        ON CONFLICT(kunci) DO UPDATE SET nilai = :nilai
        """,
    )
    suspend fun simpan(
        kunci: String,
        nilai: String,
    )

    /**
     * Mengamati satu nilai metadata secara reaktif (untuk UI status sinkronisasi).
     */
    @Query("SELECT nilai FROM meta_sinkron WHERE kunci = :kunci LIMIT 1")
    fun observasi(kunci: String): Flow<String?>
}
