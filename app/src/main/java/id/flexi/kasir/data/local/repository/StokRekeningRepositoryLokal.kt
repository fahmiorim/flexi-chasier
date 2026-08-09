package id.flexi.kasir.data.local.repository

import androidx.room.withTransaction
import id.flexi.kasir.data.local.dao.MutasiRekeningDao
import id.flexi.kasir.data.local.dao.PenyesuaianStokDao
import id.flexi.kasir.data.local.database.FlexiKasirDatabase
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
 * Penulisan entitas + antrian outbox dibungkus SATU transaksi agar crash di
 * tengah tidak meninggalkan data yang tidak pernah ter-push.
 */
class StokRekeningRepositoryLokal(
    private val basisData: FlexiKasirDatabase,
    private val pencatatOutbox: OutboxPencatat? = null,
) : StokRekeningRepository {

    private val penyesuaianStokDao: PenyesuaianStokDao = basisData.PenyesuaianStokDao()
    private val mutasiRekeningDao: MutasiRekeningDao = basisData.MutasiRekeningDao()

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
        basisData.withTransaction {
            penyesuaianStokDao.simpan(penyesuaian.keLokal())
            runCatching { pencatatOutbox?.catatPenyesuaianStok(penyesuaian) }
        }
    }

    override fun amatiMutasiRekening(): Flow<List<MutasiRekening>> {
        return mutasiRekeningDao.amatiSemua().map { daftar -> daftar.map { it.keDomain() } }
    }

    override suspend fun ambilSemuaMutasiRekening(): List<MutasiRekening> {
        return mutasiRekeningDao.ambilSemua().map { it.keDomain() }
    }

    override suspend fun simpanMutasiRekening(mutasi: MutasiRekening) {
        basisData.withTransaction {
            mutasiRekeningDao.simpan(mutasi.keLokal())
            runCatching { pencatatOutbox?.catatMutasiRekening(mutasi) }
        }
    }
}
