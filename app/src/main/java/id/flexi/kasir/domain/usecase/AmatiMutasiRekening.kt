package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.MutasiRekening
import id.flexi.kasir.domain.repository.StokRekeningRepository
import kotlinx.coroutines.flow.Flow

/**
 * Mengamati seluruh mutasi rekening (terbaru dulu).
 */
class AmatiMutasiRekening(
    private val stokRekeningRepository: StokRekeningRepository,
) {
    operator fun invoke(): Flow<List<MutasiRekening>> = stokRekeningRepository.amatiMutasiRekening()
}
