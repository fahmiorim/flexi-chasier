package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.PenyesuaianStok
import id.flexi.kasir.domain.model.StokJenis
import id.flexi.kasir.domain.repository.StokRekeningRepository
import kotlinx.coroutines.flow.Flow

/**
 * Mengamati riwayat penyesuaian stok untuk satu entitas (produk/bahan).
 */
class AmatiRiwayatPenyesuaian(
    private val stokRekeningRepository: StokRekeningRepository,
) {
    operator fun invoke(
        jenis: StokJenis,
        entitasId: String,
    ): Flow<List<PenyesuaianStok>> =
        stokRekeningRepository.amatiPenyesuaianBerdasarkanEntitas(jenis, entitasId)
}
