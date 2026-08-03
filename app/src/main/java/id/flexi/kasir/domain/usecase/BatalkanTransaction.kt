package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.repository.TransactionRepository

class BatalkanTransaction(
    private val TransactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(identitasTransaction: String, alasan: String? = null) {
        TransactionRepository.batalkanTransaction(identitasTransaction, alasan)
    }
}
