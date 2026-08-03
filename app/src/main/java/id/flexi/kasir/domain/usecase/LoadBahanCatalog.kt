package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.Bahan
import id.flexi.kasir.domain.repository.BahanRepository
import kotlinx.coroutines.flow.Flow

class LoadBahanCatalog(
    private val BahanRepository: BahanRepository,
) {
    operator fun invoke(): Flow<List<Bahan>> {
        return BahanRepository.amatiSemuaBahan()
    }
}
