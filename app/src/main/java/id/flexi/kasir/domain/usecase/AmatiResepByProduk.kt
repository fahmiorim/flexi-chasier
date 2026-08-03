package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.Resep
import id.flexi.kasir.domain.repository.BahanRepository
import kotlinx.coroutines.flow.Flow

class AmatiResepByProduk(
    private val BahanRepository: BahanRepository,
) {
    operator fun invoke(produkId: String): Flow<Resep?> {
        return BahanRepository.amatiResepByProdukId(produkId)
    }
}
