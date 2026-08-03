package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.repository.BahanRepository

class HapusBahan(
    private val BahanRepository: BahanRepository,
) {
    suspend operator fun invoke(id: String) {
        BahanRepository.deleteBahan(id)
    }
}
