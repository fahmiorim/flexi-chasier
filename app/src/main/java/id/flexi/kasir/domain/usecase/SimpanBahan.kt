package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.Bahan
import id.flexi.kasir.domain.repository.BahanRepository

class SimpanBahan(
    private val BahanRepository: BahanRepository,
) {
    suspend operator fun invoke(Bahan: Bahan) {
        BahanRepository.saveBahan(Bahan)
    }
}
