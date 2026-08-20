package id.flexi.kasir.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import id.flexi.kasir.data.local.entity.LocalCashKasEntity
import id.flexi.kasir.data.local.entity.LocalCashMutationEntity
import id.flexi.kasir.data.local.entity.LocalSetoranEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object untuk entitas kas, mutasi, dan setoran.
 */
@Dao
interface LocalCashDao {

    // ── Kas ──

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpanKas(kas: LocalCashKasEntity)

    @Query("UPDATE shift_kas SET saldoAkhir = :saldoAkhir, waktuTutup = :waktuTutup, status = 'Tutup', catatanTutup = :catatanTutup WHERE id = :id")
    suspend fun tutupKas(id: String, saldoAkhir: Long, waktuTutup: Long, catatanTutup: String?)

    @Query("SELECT * FROM shift_kas WHERE status = 'Buka' LIMIT 1")
    fun amatiKasAktif(): Flow<LocalCashKasEntity?>

    @Query("SELECT * FROM shift_kas ORDER BY waktuBuka DESC")
    fun amatiSemuaKas(): Flow<List<LocalCashKasEntity>>

    @Query("SELECT * FROM shift_kas WHERE id = :id")
    suspend fun ambilKasBerdasarkanId(id: String): LocalCashKasEntity?

    @Query("DELETE FROM shift_kas WHERE id = :id")
    suspend fun hapusKas(id: String)

    // ── Mutasi ──

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpanMutasi(mutasi: LocalCashMutationEntity)

    @Query("SELECT * FROM mutasi_kas WHERE shiftId = :kasId ORDER BY waktu DESC")
    fun amatiMutasiBerdasarkanKas(kasId: String): Flow<List<LocalCashMutationEntity>>

    @Query("SELECT * FROM mutasi_kas ORDER BY waktu DESC")
    fun amatiSemuaMutasi(): Flow<List<LocalCashMutationEntity>>

    @Query("SELECT * FROM mutasi_kas WHERE id = :id")
    suspend fun ambilMutasiBerdasarkanId(id: String): LocalCashMutationEntity?

    @Query("DELETE FROM mutasi_kas WHERE id = :id")
    suspend fun hapusMutasi(id: String)

    @Query("DELETE FROM mutasi_kas WHERE shiftId = :shiftId")
    suspend fun hapusMutasiBerdasarkanShift(shiftId: String)

    // ── Setoran ──

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun simpanSetoran(setoran: LocalSetoranEntity)

    @Query("SELECT * FROM setoran_kas ORDER BY waktu DESC")
    fun amatiSetoran(): Flow<List<LocalSetoranEntity>>

    @Query("SELECT * FROM setoran_kas WHERE id = :id")
    suspend fun ambilSetoranBerdasarkanId(id: String): LocalSetoranEntity?

    @Query("UPDATE setoran_kas SET catatan = :catatanBaru WHERE id = :id")
    suspend fun perbaruiSetoran(id: String, catatanBaru: String)

    @Query("UPDATE setoran_kas SET dihapus = 1 WHERE id = :id")
    suspend fun hapusSetoran(id: String)

    @Query("UPDATE setoran_kas SET dihapus = 1 WHERE shiftId = :shiftId")
    suspend fun hapusSetoranBerdasarkanShift(shiftId: String)

    // ═══════════════════════════════════════
    // AGGREGATE QUERIES — Optimasi performa
    // ═══════════════════════════════════════

    @Query("SELECT COALESCE(SUM(nominal), 0) FROM mutasi_kas WHERE shiftId = :kasId AND tipe = :tipe")
    fun hitungTotalMutasiBerdasarkanTipe(kasId: String, tipe: String): Flow<Long>

    @Query("SELECT COALESCE(SUM(nominal), 0) FROM setoran_kas WHERE dihapus = 0")
    fun hitungTotalSetoranAktif(): Flow<Long>

    @Query("SELECT COALESCE(SUM(nominal), 0) FROM setoran_kas WHERE shiftId = :kasId AND dihapus = 0")
    fun hitungTotalSetoranBerdasarkanKas(kasId: String): Flow<Long>

    @Query("SELECT COALESCE(SUM(nominal), 0) FROM mutasi_kas WHERE shiftId = :kasId AND tipe = :tipe")
    suspend fun ambilTotalMutasiBerdasarkanTipe(kasId: String, tipe: String): Long

}
