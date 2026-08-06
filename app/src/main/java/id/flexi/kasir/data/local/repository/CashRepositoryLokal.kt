package id.flexi.kasir.data.local.repository

import id.flexi.kasir.data.local.dao.LocalCashDao
import id.flexi.kasir.data.local.mapping.toDomain
import id.flexi.kasir.data.local.mapping.toEntity
import id.flexi.kasir.data.sync.OutboxPencatat
import id.flexi.kasir.domain.model.CashKas
import id.flexi.kasir.domain.model.CashMutation
import id.flexi.kasir.domain.model.Setoran
import id.flexi.kasir.domain.repository.CashRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementasi repositori kas yang beroperasi di atas Room (lokal).
 *
 * Menerjemahkan panggilan repositori ranah menjadi operasi DAO,
 * termasuk pemetaan (mapping) antara entitas basis data dan objek ranah.
 */
class CashRepositoryLokal(
    private val dao: LocalCashDao,
    private val pencatatOutbox: OutboxPencatat? = null,
) : CashRepository {

    // ── Kas ──

    override suspend fun simpanKas(kas: CashKas) {
        dao.simpanKas(kas.toEntity())
        runCatching { pencatatOutbox?.catatShiftKas(kas) }
    }

    override suspend fun tutupKas(id: String, saldoAkhir: Long, catatanTutup: String?) {
        dao.tutupKas(id, saldoAkhir, System.currentTimeMillis(), catatanTutup)
        val kas = dao.ambilKasBerdasarkanId(id)?.toDomain()
        if (kas != null) {
            runCatching { pencatatOutbox?.catatShiftKas(kas) }
        }
    }

    override fun amatiKasAktif(): Flow<CashKas?> =
        dao.amatiKasAktif().map { it?.toDomain() }

    override fun amatiSemuaKas(): Flow<List<CashKas>> =
        dao.amatiSemuaKas().map { list -> list.map { it.toDomain() } }

    override suspend fun ambilKasBerdasarkanId(id: String): CashKas? =
        dao.ambilKasBerdasarkanId(id)?.toDomain()

    // ── Mutasi ──

    override suspend fun simpanMutasi(mutasi: CashMutation) {
        dao.simpanMutasi(mutasi.toEntity())
        runCatching { pencatatOutbox?.catatMutasiKas(mutasi) }
    }

    override fun amatiMutasiBerdasarkanKas(kasId: String): Flow<List<CashMutation>> =
        dao.amatiMutasiBerdasarkanKas(kasId).map { list -> list.map { it.toDomain() } }

    override fun amatiSemuaMutasi(): Flow<List<CashMutation>> =
        dao.amatiSemuaMutasi().map { list -> list.map { it.toDomain() } }

    override suspend fun ambilMutasiBerdasarkanId(id: String): CashMutation? =
        dao.ambilMutasiBerdasarkanId(id)?.toDomain()

    override suspend fun hapusMutasi(id: String) {
        val mutasi = dao.ambilMutasiBerdasarkanId(id)?.toDomain()
        dao.hapusMutasi(id)
        if (mutasi != null) {
            runCatching { pencatatOutbox?.catatMutasiKas(mutasi, dihapus = true) }
        }
    }

    // ── Setoran ──

    override suspend fun simpanSetoran(setoran: Setoran) {
        dao.simpanSetoran(setoran.toEntity())
        runCatching { pencatatOutbox?.catatSetoran(setoran) }
    }

    override fun amatiSetoran(): Flow<List<Setoran>> =
        dao.amatiSetoran().map { list -> list.map { it.toDomain() } }

    override suspend fun ambilSetoranBerdasarkanId(id: String): Setoran? =
        dao.ambilSetoranBerdasarkanId(id)?.toDomain()

    override suspend fun perbaruiSetoran(id: String, catatanBaru: String) {
        dao.perbaruiSetoran(id, catatanBaru)
        val setoran = dao.ambilSetoranBerdasarkanId(id)?.toDomain()?.copy(catatan = catatanBaru)
        if (setoran != null) {
            runCatching { pencatatOutbox?.catatSetoran(setoran) }
        }
    }

    override suspend fun hapusSetoran(id: String) {
        val setoran = dao.ambilSetoranBerdasarkanId(id)?.toDomain()
        dao.hapusSetoran(id)
        if (setoran != null) {
            runCatching { pencatatOutbox?.catatSetoran(setoran.copy(dihapus = true)) }
        }
    }

    // ═══════════════════════════════════════
    // AGGREGATE — SQL SUM langsung
    // ═══════════════════════════════════════

    override fun hitungTotalMutasiBerdasarkanTipe(kasId: String, tipe: String): Flow<Long> =
        dao.hitungTotalMutasiBerdasarkanTipe(kasId, tipe)

    override fun hitungTotalMutasiSemuaBerdasarkanTipe(tipe: String): Flow<Long> =
        dao.hitungTotalMutasiSemuaBerdasarkanTipe(tipe)

    override fun hitungTotalSetoranBerdasarkanKas(kasId: String): Flow<Long> =
        dao.hitungTotalSetoranBerdasarkanKas(kasId)

    override fun hitungTotalSetoranAktif(): Flow<Long> =
        dao.hitungTotalSetoranAktif()

    override suspend fun ambilTotalMutasiBerdasarkanTipe(kasId: String, tipe: String): Long =
        dao.ambilTotalMutasiBerdasarkanTipe(kasId, tipe)

    override fun hitungTotalSaldoAwalSemua(): Flow<Long> =
        dao.hitungTotalSaldoAwalSemua()
}
