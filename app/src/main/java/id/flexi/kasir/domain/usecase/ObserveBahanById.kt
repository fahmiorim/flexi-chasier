package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.Bahan
import id.flexi.kasir.domain.repository.BahanRepository
import kotlinx.coroutines.flow.Flow

class ObserveBahanById(
    private val BahanRepository: BahanRepository,
) {
    operator fun invoke(id: String): Flow<Bahan?> {
        return BahanRepository.amatiBahanById(id)
    }
}
