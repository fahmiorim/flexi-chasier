package id.flexi.kasir.domain.model

/**
 * Merepresentasikan produk yang dimasukkan ke dalam keranjang belanja.
 *
 * @property produk Data [Produk] terkait.
 * @property jumlah Kuantitas produk yang dipesan.
 * @property catatan Informasi tambahan dari pelanggan untuk item ini (misal: "kurangi gula").
 * @property varian Varian yang dipilih (misal: HOT, ICE). Null jika produk tidak punya varian.
 */
data class CartItem(
    val produk: Produk,
    val jumlah: Int,
    val catatan: String? = null,
    val varian: Varian? = null,
    val apakahSelesai: Boolean = false,
)
