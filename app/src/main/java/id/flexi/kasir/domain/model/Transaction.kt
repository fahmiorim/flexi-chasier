package id.flexi.kasir.domain.model

enum class TransactionStatus {
    Pending,
    Processing,
    Ready,
    Paid,
}

/**
 * Status pembayaran — apakah uang sudah diterima atau belum.
 */
enum class PaymentStatus {
    BelumDibayar,
    SudahDibayar,
}

/**
 * Status dapur — progress pesanan di kitchen.
 */
enum class KitchenStatus {
    Dicatat,
    Diproses,
    Siap,
    Selesai,
}

enum class PaymentMethod(val label: String) {
    Cash(label = "Tunai"),
    Qris(label = "QRIS"),
}

enum class OrderType(val label: String) {
    DineIn(label = "Dine In"),
    TakeAway(label = "Take Away"),
}

data class Payment(
    val paymentMethod: PaymentMethod,
    val jumlah: Uang,
)

data class Transaction(
    val id: String,
    val daftarCartItem: List<CartItem>,
    val uangDibayar: Uang = Uang.Nol,
    val potongan: Uang = Uang.Nol,
    val biayaLayanan: Uang = Uang.Nol,
    val pajak: Uang = Uang.Nol,
    val waktuTransactionEpochMili: Long,
    val catatan: String? = null,
    val status: TransactionStatus = TransactionStatus.Paid,
    val paymentMethod: PaymentMethod = PaymentMethod.Cash,
    val orderType: OrderType = OrderType.DineIn,
    val nomorAntrian: Int? = null,
    val mejaId: String? = null,
    val daftarPayment: List<Payment> = emptyList(),
    val waktuDiprosesEpochMili: Long? = null,
    val waktuSelesaiEpochMili: Long? = null,
    val waktuDibayarEpochMili: Long? = null,
    val dibatalkan: Boolean = false,
    val alasanPembatalan: String? = null,
) {
    /** Status pembayaran — derived dari [status] database. */
    val paymentStatus: PaymentStatus get() = when (status) {
        TransactionStatus.Pending -> PaymentStatus.BelumDibayar
        TransactionStatus.Processing,
        TransactionStatus.Ready,
        TransactionStatus.Paid -> PaymentStatus.SudahDibayar
    }

    /** Status dapur — derived dari [status] database. */
    val kitchenStatus: KitchenStatus get() = when (status) {
        TransactionStatus.Pending -> KitchenStatus.Dicatat
        TransactionStatus.Processing -> KitchenStatus.Diproses
        TransactionStatus.Ready -> KitchenStatus.Siap
        TransactionStatus.Paid -> KitchenStatus.Selesai
    }
}
