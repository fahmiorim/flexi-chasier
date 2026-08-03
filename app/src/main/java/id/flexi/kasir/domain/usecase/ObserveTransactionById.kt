package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

/**
 * Kasus penggunaan untuk mengamati satu Transaction berdasarkan identitas.
 *
 * Dipakai oleh layar detail Transaction agar pembacaan detail tidak
 * menembus repository langsung dari ViewModel.
 */
class ObserveTransactionById(
    private val TransactionRepository: TransactionRepository,
) {
    operator fun invoke(
        identitasTransaction: String,
    ): Flow<Transaction?> {
        return TransactionRepository.ObserveTransactionById(
            identitasTransaction = identitasTransaction,
        )
    }
}
