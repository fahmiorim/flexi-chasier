package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.PaymentMethod
import id.flexi.kasir.domain.model.TransactionStatus
import id.flexi.kasir.domain.model.TransactionCostBreakdown
import id.flexi.kasir.domain.model.Uang
import id.flexi.kasir.domain.repository.TransactionRepository

class PayPendingOrder(
    private val TransactionRepository: TransactionRepository,
) {
    suspend fun eksekusi(
        identitasTransaction: String,
        paymentMethod: PaymentMethod = PaymentMethod.Cash,
        uangDibayar: Long? = null,
    ) {
        val Transaction = TransactionRepository.ambilTransactionBerdasarkanIdentitas(identitasTransaction)
            ?: throw IllegalArgumentException("Pesanan tidak ditemukan.")

        val subtotal = Transaction.daftarCartItem.sumOf { item ->
            item.produk.harga * item.jumlah
        }

        /**
         * Gunakan [TransactionCostBreakdown] untuk menghitung total akhir dengan aman.
         * Jika potongan melebihi subtotal, potongan akan dianggap nol (tidak crash).
         */
        val breakdown = TransactionCostBreakdown(
            subtotal = Uang.dariRupiah(subtotal),
            potongan = Transaction.potongan,
            biayaLayanan = Transaction.biayaLayanan,
            pajak = Transaction.pajak,
        )
        val total = breakdown.totalAkhir

        // Jika uangDibayar diberikan (Tunai), pakai nominal dari user.
        // Jika null (QRIS), pakai total yang dihitung.
        val nominalBayar = uangDibayar ?: total.nilaiRupiah

        TransactionRepository.perbaruiStatusDanPaymentTransaction(
            identitasTransaction = identitasTransaction,
            status = TransactionStatus.Paid,
            uangDibayar = Uang.dariRupiah(nominalBayar),
            paymentMethod = paymentMethod,
            waktuDibayarEpochMili = System.currentTimeMillis(),
        )
    }
}
