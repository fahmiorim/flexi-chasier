package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.CartItem
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.model.Varian

/**
 * Menambahkan satu produk ke keranjang.
 *
 * Jika produk (dengan varian yang sama) sudah ada di keranjang, jumlahnya ditambah satu.
 * Jika belum ada, item baru dibuat.
 * Jika stok sudah habis, data keranjang dikembalikan apa adanya.
 *
 * Pencocokan item didasarkan pada ID produk + nama varian (jika ada).
 * Produk yang sama dengan varian berbeda dianggap sebagai item terpisah.
 */
class AddProductToCart {

    /**
     * Menjalankan penambahan produk ke daftar keranjang.
     *
     * @param daftarCartItem Daftar item saat ini.
     * @param produk Data produk yang akan ditambahkan.
     * @param varian Varian yang dipilih (nullable).
     * @return Daftar item baru setelah produk ditambahkan atau jumlahnya diperbarui.
     */
    operator fun invoke(
        daftarCartItem: List<CartItem>,
        produk: Produk,
        varian: Varian? = null,
    ): List<CartItem> {
        if (!produk.aktif || (produk.apakahStokDiaktifkan && produk.stokTersedia <= 0)) {
            return daftarCartItem
        }

        val daftarBaru = daftarCartItem.toMutableList()

        // Cari item dengan produk + varian yang sama
        val indeksItemLama = daftarBaru.indexOfFirst { item ->
            item.produk.id == produk.id && item.varian?.nama == varian?.nama
        }

        if (indeksItemLama >= 0) {
            val itemLama = daftarBaru[indeksItemLama]

            if (produk.apakahStokDiaktifkan && itemLama.jumlah >= produk.stokTersedia) {
                return daftarCartItem
            }

            daftarBaru[indeksItemLama] = itemLama.copy(
                jumlah = itemLama.jumlah + 1,
            )
        } else {
            daftarBaru.add(
                CartItem(
                    produk = produk,
                    jumlah = 1,
                    varian = varian,
                ),
            )
        }

        return daftarBaru
    }
}
