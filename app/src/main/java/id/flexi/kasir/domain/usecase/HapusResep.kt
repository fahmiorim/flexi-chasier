package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.repository.BahanRepository

class HapusResep(
    private val BahanRepository: BahanRepository,
) {
    /**
     * Menghapus resep beserta semua komposisi bahannya (CASCADE).
     */
    suspend operator fun invoke(id: String) {
        BahanRepository.deleteResep(id)
    }
}
