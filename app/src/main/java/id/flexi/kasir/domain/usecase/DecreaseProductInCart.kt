package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.CartItem

/**
 * Mengurangi jumlah item di keranjang satu per satu.
 *
 * Jika jumlah item masih lebih dari satu, jumlah akan dikurangi.
 * Jika jumlah sudah satu, data dikembalikan apa adanya.
 * Penghapusan total tetap ditangani oleh use case khusus hapus item.
 *
 * Pencocokan item didasarkan pada ID produk + nama varian (jika ada).
 */
class DecreaseProductInCart {

    /**
     * Menjalankan pengurangan jumlah item di daftar keranjang.
     *
     * @param daftarCartItem Daftar item saat ini.
     * @param produkId ID produk yang akan dikurangi jumlahnya.
     * @param varianNama Nama varian (nullable). Jika null, cocokkan tanpa varian.
     * @return Daftar item baru dengan jumlah yang sudah diperbarui.
     */
    operator fun invoke(
        daftarCartItem: List<CartItem>,
        produkId: String,
        varianNama: String? = null,
    ): List<CartItem> {
        val daftarBaru = daftarCartItem.toMutableList()

        val indeksItem = daftarBaru.indexOfFirst { item ->
            item.produk.id == produkId && item.varian?.nama == varianNama
        }

        if (indeksItem < 0) {
            return daftarCartItem
        }

        val itemLama = daftarBaru[indeksItem]

        if (itemLama.jumlah <= 1) {
            return daftarCartItem
        }

        daftarBaru[indeksItem] = itemLama.copy(
            jumlah = itemLama.jumlah - 1,
        )

        return daftarBaru
    }
}
