package id.flexi.kasir.data.local.repository

import id.flexi.kasir.data.local.dao.MutasiRekeningDao
import id.flexi.kasir.data.local.dao.PenyesuaianStokDao
import id.flexi.kasir.data.local.mapping.keDomain
import id.flexi.kasir.data.local.mapping.keLokal
import id.flexi.kasir.data.sync.OutboxPencatat
import id.flexi.kasir.domain.model.MutasiRekening
import id.flexi.kasir.domain.model.PenyesuaianStok
import id.flexi.kasir.domain.model.StokJenis
import id.flexi.kasir.domain.repository.StokRekeningRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementasi lokal [StokRekeningRepository] berbasis Room + outbox sinkron.
 *
 * Setiap penulisan menyimpan entitas lalu mencatat outbox (best-effort).
 */
class StokRekeningRepositoryLokal(
    private val penyesuaianStokDao: PenyesuaianStokDao,
    private val mutasiRekeningDao: MutasiRekeningDao,
    private val pencatatOutbox: OutboxPencatat? = null,
) : StokRekeningRepository {

    override fun amatiSemuaPenyesuaian(): Flow<List<PenyesuaianStok>> {
        return penyesuaianStokDao.amatiSemua().map { daftar -> daftar.map { it.keDomain() } }
    }

    override fun amatiPenyesuaianBerdasarkanEntitas(
        jenis: StokJenis,
        entitasId: String,
    ): Flow<List<PenyesuaianStok>> {
        return penyesuaianStokDao.amatiBerdasarkanEntitas(jenis.name, entitasId)
            .map { daftar -> daftar.map { it.keDomain() } }
    }

    override suspend fun simpanPenyesuaian(penyesuaian: PenyesuaianStok) {
        penyesuaianStokDao.simpan(penyesuaian.keLokal())
        runCatching { pencatatOutbox?.catatPenyesuaianStok(penyesuaian) }
    }

    override fun amatiMutasiRekening(): Flow<List<MutasiRekening>> {
        return mutasiRekeningDao.amatiSemua().map { daftar -> daftar.map { it.keDomain() } }
    }

    override suspend fun ambilSemuaMutasiRekening(): List<MutasiRekening> {
        return mutasiRekeningDao.ambilSemua().map { it.keDomain() }
    }

    override suspend fun simpanMutasiRekening(mutasi: MutasiRekening) {
        mutasiRekeningDao.simpan(mutasi.keLokal())
        runCatching { pencatatOutbox?.catatMutasiRekening(mutasi) }
    }
}
