package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.TableStatus
import id.flexi.kasir.domain.repository.TableRepository
import id.flexi.kasir.domain.repository.TransactionRepository

class DeletePendingOrder(
    private val TransactionRepository: TransactionRepository,
    private val TableRepository: TableRepository,
) {
    suspend fun eksekusi(
        identitasTransaction: String,
    ) {
        val Transaction = TransactionRepository.ambilTransactionBerdasarkanIdentitas(identitasTransaction)
        val mejaId = Transaction?.mejaId

        TransactionRepository.hapusTransactionDanKembalikanStok(identitasTransaction)

        if (mejaId != null) {
            TableRepository.perbaruiTableStatus(mejaId, TableStatus.Available)
        }
    }
}
