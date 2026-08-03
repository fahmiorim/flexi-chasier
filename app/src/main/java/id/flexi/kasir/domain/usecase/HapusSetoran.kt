package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.repository.CashRepository

class HapusSetoran(
    private val cashRepository: CashRepository,
) {
    suspend operator fun invoke(id: String) {
        cashRepository.hapusSetoran(id)
    }
}
