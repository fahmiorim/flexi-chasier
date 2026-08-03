package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class ObservePendingOrders(
    private val TransactionRepository: TransactionRepository,
) {
    operator fun invoke(): Flow<List<Transaction>> {
        return TransactionRepository.amatiTransactionPending()
    }
}
