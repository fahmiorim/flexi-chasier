package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.repository.CashRepository

class PerbaruiSetoran(
    private val cashRepository: CashRepository,
) {
    suspend operator fun invoke(id: String, catatan: String) {
        cashRepository.perbaruiSetoran(id, catatan)
    }
}
