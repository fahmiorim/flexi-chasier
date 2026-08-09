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

        val daftarItemBaru = Transaction.daftarCartItem.map { item ->
            val produkSaatIniItem = produkSaatIni[item.produk.id]
            // Produk yang sudah dihapus dari katalog TIDAK di-drop diam-diam:
            // pakai snapshot tersimpan agar item tetap muncul & bisa dilanjutkan
            // (DB masih menyimpan item tersebut, sehingga menjatuhkannya hanya
            // akan membuat baris yatim yang tidak pernah bisa dilihat/diubah).
            item.copy(
                produk = produkSaatIniItem ?: item.produk,
                apakahSelesai = false,
            )
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
