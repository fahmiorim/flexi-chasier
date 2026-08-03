package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.TransactionStatus
import id.flexi.kasir.domain.repository.TransactionRepository

class SelesaikanTransaction(
    private val TransactionRepository: TransactionRepository,
) {
    /**
     * Menandai pesanan selesai di antrian.
     *
     * @param identitasTransaction ID transaksi.
     * @param ubahStatusKeReady Jika true (default), status diubah ke Ready.
     *        Jika false (untuk Pending), status tetap Pending tapi item ditandai selesai
     *        dan waktuSelesai dicatat.
     */
    suspend operator fun invoke(
        identitasTransaction: String,
        ubahStatusKeReady: Boolean = true,
    ) {
        val waktuSekarang = System.currentTimeMillis()
        TransactionRepository.tandaiItemSelesai(identitasTransaction)
        TransactionRepository.perbaruiWaktuSelesai(identitasTransaction, waktuSekarang)
        if (ubahStatusKeReady) {
            TransactionRepository.perbaruiStatusTransaction(
                identitasTransaction = identitasTransaction,
                status = TransactionStatus.Ready,
            )
        }
    }
}
