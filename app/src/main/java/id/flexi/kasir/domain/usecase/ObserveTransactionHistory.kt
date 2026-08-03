package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

/**
 * Kasus penggunaan untuk mengamati riwayat Transaction yang sudah lunas.
 *
 * Use case ini menjadi pintu baca daftar Transaction untuk layar riwayat,
 * sehingga ViewModel tidak perlu mengakses repository secara langsung.
 */
class ObserveTransactionHistory(
    private val TransactionRepository: TransactionRepository,
) {
    operator fun invoke(): Flow<List<Transaction>> {
        return TransactionRepository.amatiTransactionLunas()
    }
}
