package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.repository.CashRepository

class TutupKas(
    private val cashRepository: CashRepository,
) {
    suspend operator fun invoke(
        id: String,
        saldoAkhir: Long,
        catatan: String? = null,
    ) {
        cashRepository.tutupKas(id, saldoAkhir, catatan)
    }
}
