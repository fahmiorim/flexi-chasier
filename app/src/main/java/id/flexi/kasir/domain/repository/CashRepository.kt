package id.flexi.kasir.domain.repository

import id.flexi.kasir.domain.model.CashKas
import id.flexi.kasir.domain.model.CashMutation
import id.flexi.kasir.domain.model.Setoran
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak repositori untuk mengelola data kas (shift, mutasi, setoran).
 */
interface CashRepository {

    // ── Kas ──

    suspend fun simpanKas(kas: CashKas)

    suspend fun tutupKas(id: String, saldoAkhir: Long, catatanTutup: String?)

    fun amatiKasAktif(): Flow<CashKas?>

    fun amatiSemuaKas(): Flow<List<CashKas>>

    suspend fun ambilKasBerdasarkanId(id: String): CashKas?

    // ── Mutasi ──

    suspend fun simpanMutasi(mutasi: CashMutation)

    fun amatiMutasiBerdasarkanKas(kasId: String): Flow<List<CashMutation>>

    fun amatiSemuaMutasi(): Flow<List<CashMutation>>

    suspend fun ambilMutasiBerdasarkanId(id: String): CashMutation?

    suspend fun hapusMutasi(id: String)

    // ── Setoran ──

    suspend fun simpanSetoran(setoran: Setoran)

    fun amatiSetoran(): Flow<List<Setoran>>

    suspend fun ambilSetoranBerdasarkanId(id: String): Setoran?

    suspend fun perbaruiSetoran(id: String, catatanBaru: String)

    suspend fun hapusSetoran(id: String)

    // ═══════════════════════════════════════
    // AGGREGATE — Optimasi performa
    // ═══════════════════════════════════════

    /** Total mutasi berdasarkan tipe untuk satu shift (reactive). */
    fun hitungTotalMutasiBerdasarkanTipe(kasId: String, tipe: String): Flow<Long>

    /** Total setoran aktif (dihapus = 0) untuk satu shift (reactive). */
    fun hitungTotalSetoranBerdasarkanKas(kasId: String): Flow<Long>

    /** Total setoran aktif (dihapus = 0) (reactive). */
    fun hitungTotalSetoranAktif(): Flow<Long>

    /** Total mutasi berdasarkan tipe untuk satu shift (one-shot). */
    suspend fun ambilTotalMutasiBerdasarkanTipe(kasId: String, tipe: String): Long
}
