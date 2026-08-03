package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.CartItem

/**
 * Menghapus satu item sepenuhnya dari keranjang berdasarkan identitas produk dan varian.
 *
 * Pencocokan item didasarkan pada ID produk + nama varian (jika ada).
 */
class RemoveProductFromCart {

    /**
     * Menjalankan penghapusan item dari daftar keranjang.
     *
     * @param daftarCartItem Daftar item saat ini.
     * @param produkId Identitas produk yang akan dihapus.
     * @param varianNama Nama varian (nullable). Jika null, cocokkan tanpa varian.
     * @return Daftar item baru setelah produk dihapus.
     */
    operator fun invoke(
        daftarCartItem: List<CartItem>,
        produkId: String,
        varianNama: String? = null,
    ): List<CartItem> {
        return daftarCartItem.filterNot { item ->
            item.produk.id == produkId && item.varian?.nama == varianNama
        }
    }
}
