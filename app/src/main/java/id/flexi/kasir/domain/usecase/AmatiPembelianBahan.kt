package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.PembelianBahan
import id.flexi.kasir.domain.repository.BahanRepository
import kotlinx.coroutines.flow.Flow

class AmatiPembelianBahan(
    private val BahanRepository: BahanRepository,
) {
    operator fun invoke(bahanId: String): Flow<List<PembelianBahan>> {
        return BahanRepository.amatiPembelianBahan(bahanId)
    }
}
