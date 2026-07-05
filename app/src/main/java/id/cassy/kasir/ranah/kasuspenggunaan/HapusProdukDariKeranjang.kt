package id.cassy.kasir.ranah.kasuspenggunaan

import id.cassy.kasir.ranah.model.ItemKeranjang

/**
 * Menghapus satu item sepenuhnya dari keranjang berdasarkan identitas produk.
 */
class HapusProdukDariKeranjang {

    /**
     * Menjalankan penghapusan item dari daftar keranjang.
     *
     * @param daftarItemKeranjang Daftar item saat ini.
     * @param produkId Identitas produk yang akan dihapus.
     * @return Daftar item baru setelah produk dihapus.
     */
    operator fun invoke(
        daftarItemKeranjang: List<ItemKeranjang>,
        produkId: String,
    ): List<ItemKeranjang> {
        return daftarItemKeranjang.filterNot { itemKeranjang ->
            itemKeranjang.produk.id == produkId
        }
    }
}
