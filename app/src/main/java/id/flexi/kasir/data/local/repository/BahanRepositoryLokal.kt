package id.flexi.kasir.data.local.repository

import id.flexi.kasir.data.local.dao.BahanDao
import id.flexi.kasir.data.local.mapping.keDomain
import id.flexi.kasir.data.local.mapping.keLokal
import id.flexi.kasir.data.sync.OutboxPencatat
import id.flexi.kasir.domain.model.Bahan
import id.flexi.kasir.domain.model.BahanResep
import id.flexi.kasir.domain.model.PembelianBahan
import id.flexi.kasir.domain.model.Resep
import id.flexi.kasir.domain.repository.BahanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class BahanRepositoryLokal(
    private val BahanDao: BahanDao,
    private val pencatatOutbox: OutboxPencatat? = null,
) : BahanRepository {

    override fun amatiSemuaBahan(): Flow<List<Bahan>> {
        return BahanDao.amatiSemuaBahan().map { entities ->
            entities.map { it.keDomain() }
        }
    }

    override fun amatiBahanById(id: String): Flow<Bahan?> {
        return BahanDao.amatiBahanBerdasarkanId(id).map { it?.keDomain() }
    }

    override suspend fun ambilBahanById(id: String): Bahan? {
        return BahanDao.ambilBahanBerdasarkanId(id)?.keDomain()
    }

    override suspend fun saveBahan(bahan: Bahan) {
        BahanDao.simpanBahan(bahan.keLokal())
        runCatching { pencatatOutbox?.catatBahan(bahan) }
    }

    override suspend fun deleteBahan(id: String) {
        val bahan = BahanDao.ambilBahanBerdasarkanId(id)?.keDomain()
        BahanDao.hapusBahan(id)
        if (bahan != null) {
            runCatching { pencatatOutbox?.catatBahan(bahan, dihapus = true) }
        }
    }

    // ── Pembelian ──

    override fun amatiPembelianBahan(bahanId: String): Flow<List<PembelianBahan>> {
        return BahanDao.amatiPembelianBerdasarkanBahan(bahanId).map { entities ->
            entities.map { it.keDomain() }
        }
    }

    override suspend fun savePembelian(pembelian: PembelianBahan) {
        BahanDao.simpanPembelian(pembelian.keLokal())
        runCatching { pencatatOutbox?.catatPembelianBahan(pembelian) }
    }

    override suspend fun deletePembelian(id: String) {
        val pembelian = BahanDao.ambilPembelianBerdasarkanId(id)?.keDomain()
        BahanDao.hapusPembelian(id)
        if (pembelian != null) {
            runCatching { pencatatOutbox?.catatPembelianBahan(pembelian, dihapus = true) }
        }
    }

    override suspend fun ambilPembelianTerakhir(bahanId: String): PembelianBahan? {
        return BahanDao.ambilPembelianTerakhir(bahanId)?.keDomain()
    }

    override suspend fun perbaruiStokBahan(id: String, jumlah: Double) {
        BahanDao.perbaruiStokBahan(id, jumlah)
    }

    override suspend fun perbaruiHargaSatuanBahan(id: String, harga: Long) {
        BahanDao.perbaruiHargaSatuanBahan(id, harga)
    }

    // ── Resep ──

    override fun amatiResepByProdukId(produkId: String): Flow<Resep?> {
        return BahanDao.amatiResepBerdasarkanProduk(produkId).map { entities ->
            val resepEntity = entities.firstOrNull()
            if (resepEntity != null) {
                val bahanResepEntities = BahanDao.ambilBahanResepBerdasarkanResep(resepEntity.id)
                resepEntity.keDomain(daftarBahan = bahanResepEntities.map { it.keDomain() })
            } else {
                null
            }
        }
    }

    override suspend fun ambilResepByProdukId(produkId: String): Resep? {
        val resepEntity = BahanDao.ambilResepBerdasarkanProduk(produkId) ?: return null
        val bahanResepEntities = BahanDao.ambilBahanResepBerdasarkanResep(resepEntity.id)
        return resepEntity.keDomain(daftarBahan = bahanResepEntities.map { it.keDomain() })
    }

    override suspend fun saveResep(resep: Resep) {
        BahanDao.simpanResep(resep.keLokal())
        runCatching { pencatatOutbox?.catatResep(resep) }
    }

    override suspend fun deleteResep(id: String) {
        val resep = BahanDao.ambilResepBerdasarkanId(id)
        BahanDao.hapusResep(id)
        if (resep != null) {
            val resepDomain = resep.keDomain(
                daftarBahan = BahanDao.ambilBahanResepBerdasarkanResep(id).map { it.keDomain() },
            )
            runCatching { pencatatOutbox?.catatResep(resepDomain, dihapus = true) }
        }
    }

    override suspend fun saveBahanResep(daftar: List<BahanResep>) {
        if (daftar.isEmpty()) return
        val entities = daftar.map { it.keLokal() }
        BahanDao.simpanBanyakBahanResep(entities)
    }

    override suspend fun deleteBahanResepByResepId(resepId: String) {
        BahanDao.hapusBahanResepBerdasarkanResep(resepId)
    }

    override suspend fun ambilSemuaResepWithBahan(): List<Resep> {
        return BahanDao.amatiSemuaResep().first().map { entity ->
            val bahanResepEntities = BahanDao.ambilBahanResepBerdasarkanResep(entity.id)
            entity.keDomain(daftarBahan = bahanResepEntities.map { it.keDomain() })
        }
    }
}
