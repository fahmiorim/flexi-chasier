package id.flexi.kasir.domain.repository

import id.flexi.kasir.domain.model.Bahan
import id.flexi.kasir.domain.model.BahanResep
import id.flexi.kasir.domain.model.PembelianBahan
import id.flexi.kasir.domain.model.Resep
import kotlinx.coroutines.flow.Flow

interface BahanRepository {
    fun amatiSemuaBahan(): Flow<List<Bahan>>
    fun amatiBahanById(id: String): Flow<Bahan?>
    suspend fun ambilBahanById(id: String): Bahan?
    suspend fun saveBahan(bahan: Bahan)
    suspend fun deleteBahan(id: String)

    // ── Pembelian ──

    fun amatiPembelianBahan(bahanId: String): Flow<List<PembelianBahan>>
    suspend fun savePembelian(pembelian: PembelianBahan)
    suspend fun deletePembelian(id: String)
    suspend fun ambilPembelianTerakhir(bahanId: String): PembelianBahan?
    suspend fun perbaruiStokBahan(id: String, jumlah: Double)
    suspend fun perbaruiHargaSatuanBahan(id: String, harga: Long)

    // ── Resep ──

    fun amatiResepByProdukId(produkId: String): Flow<Resep?>
    suspend fun ambilResepByProdukId(produkId: String): Resep?
    suspend fun saveResep(resep: Resep)
    suspend fun deleteResep(id: String)
    suspend fun saveBahanResep(daftar: List<BahanResep>)
    suspend fun deleteBahanResepByResepId(resepId: String)
    suspend fun ambilSemuaResepWithBahan(): List<Resep>
}
