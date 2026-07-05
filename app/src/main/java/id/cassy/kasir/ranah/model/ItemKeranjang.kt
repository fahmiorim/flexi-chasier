package id.cassy.kasir.ranah.model

/**
 * Merepresentasikan produk yang dimasukkan ke dalam keranjang belanja.
 *
 * @property produk Data [Produk] terkait.
 * @property jumlah Kuantitas produk yang dipesan.
 * @property catatan Informasi tambahan dari pelanggan untuk item ini (misal: "kurangi gula").
 */
data class ItemKeranjang(
    val produk: Produk,
    val jumlah: Int,
    val catatan: String? = null,
)
