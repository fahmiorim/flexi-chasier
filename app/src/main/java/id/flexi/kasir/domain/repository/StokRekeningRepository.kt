package id.flexi.kasir.domain.repository

import id.flexi.kasir.domain.model.MutasiRekening
import id.flexi.kasir.domain.model.PenyesuaianStok
import id.flexi.kasir.domain.model.StokJenis
import kotlinx.coroutines.flow.Flow

/**
 * Repositori untuk riwayat penyesuaian stok & mutasi rekening.
 *
 * Kedua entitas mandiri (tanpa induk) dan disinkronkan lintas perangkat
 * (outbox + pull). Penyimpanan lokal terjadi SEKALI pada penulisan; baris
 * outbox menangani pengiriman ke server secara asinkron.
 */
interface StokRekeningRepository {

    // ── Penyesuaian stok ──

    /** Mengamati seluruh riwayat penyesuaian stok (terbaru dulu). */
    fun amatiSemuaPenyesuaian(): Flow<List<PenyesuaianStok>>

    /** Mengamati riwayat penyesuaian untuk satu entitas (produk/bahan). */
    fun amatiPenyesuaianBerdasarkanEntitas(
        jenis: StokJenis,
        entitasId: String,
    ): Flow<List<PenyesuaianStok>>

    /** Menyimpan penyesuaian stok lokal + mencatatnya ke outbox push. */
    suspend fun simpanPenyesuaian(penyesuaian: PenyesuaianStok)

    // ── Mutasi rekening ──

    /** Mengamati seluruh mutasi rekening (terbaru dulu). */
    fun amatiMutasiRekening(): Flow<List<MutasiRekening>>

    /** Mengambil seluruh mutasi rekening (sekali jalan). */
    suspend fun ambilSemuaMutasiRekening(): List<MutasiRekening>

    /** Menyimpan mutasi rekening lokal + mencatatnya ke outbox push. */
    suspend fun simpanMutasiRekening(mutasi: MutasiRekening)
}
