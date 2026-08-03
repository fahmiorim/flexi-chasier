package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.CartItem
import id.flexi.kasir.domain.model.OrderType
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.repository.TransactionRepository

/**
 * Hasil resume pesanan pending, berisi daftar item keranjang
 * serta metadata yang perlu dipulihkan (order ID, meja, catatan, tipe pesanan).
 */
data class ResumePendingOrderResult(
    val daftarCartItem: List<CartItem>,
    val identitasTransaction: String,
    val mejaId: String? = null,
    val catatan: String? = null,
    val orderType: OrderType = OrderType.DineIn,
)

/**
 * Use case untuk melanjutkan pesanan pending ke keranjang.
 *
 * Order TIDAK dihapus dari DB — item tetap di database.
 * Stock TIDAK dikembalikan — stock sudah dikurangi saat Simpan pertama.
 * Saat Simpan nanti, CompleteLocalCheckout akan handle stock update.
 */
class ResumePendingOrder(
    private val TransactionRepository: TransactionRepository,
) {
    suspend fun eksekusi(
        identitasTransaction: String,
        daftarProdukSaatIni: List<Produk>,
    ): ResumePendingOrderResult {
        val Transaction = TransactionRepository.ambilTransactionBerdasarkanIdentitas(identitasTransaction)
            ?: throw IllegalArgumentException("Pesanan tidak ditemukan.")

        val produkSaatIni = daftarProdukSaatIni.associateBy { it.id }

        val daftarItemBaru = Transaction.daftarCartItem.mapNotNull { item ->
            val produkSaatIniItem = produkSaatIni[item.produk.id] ?: return@mapNotNull null
            // Reset apakahSelesai ke false agar item bisa dimodifikasi lagi
            item.copy(produk = produkSaatIniItem, apakahSelesai = false)
        }

        if (daftarItemBaru.isEmpty()) {
            throw IllegalArgumentException("Tidak ada produk yang tersedia untuk dilanjutkan.")
        }

        return ResumePendingOrderResult(
            daftarCartItem = daftarItemBaru,
            identitasTransaction = Transaction.id,
            mejaId = Transaction.mejaId,
            catatan = Transaction.catatan,
            orderType = Transaction.orderType,
        )
    }
}
