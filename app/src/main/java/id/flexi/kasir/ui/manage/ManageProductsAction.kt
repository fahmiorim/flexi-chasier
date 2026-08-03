package id.flexi.kasir.ui.manage

/**
 * Representasi seluruh aksi pengguna pada layar kelola produk.
 */
sealed interface ManageProductsAction {

    /**
     * Aksi saat pengguna ingin menghapus produk.
     *
     * @property identitasProduk ID produk yang akan dihapus.
     * @property namaProduk Nama produk untuk ditampilkan di dialog konfirmasi.
     */
    data class MintaDeleteProduct(
        val identitasProduk: String,
        val namaProduk: String,
    ) : ManageProductsAction

    /**
     * Aksi konfirmasi penghapusan produk.
     */
    data object KonfirmasiDeleteProduct : ManageProductsAction

    /**
     * Aksi pembatalan penghapusan produk.
     */
    data object BatalkanDeleteProduct : ManageProductsAction

    /**
     * Aksi pencarian produk berdasarkan kata kunci.
     */
    data class PerbaruiKataKunciPencarian(val kataKunci: String) : ManageProductsAction

    /**
     * Aksi membersihkan kata kunci pencarian.
     */
    data object ResetPencarian : ManageProductsAction

    /**
     * Aksi menambah kategori baru.
     */
    data class TambahKategori(val nama: String) : ManageProductsAction

    /**
     * Aksi menghapus kategori dari semua produk.
     */
    data class HapusKategori(val nama: String) : ManageProductsAction

    data class PerbaruiKategoriFilter(val kategori: String) : ManageProductsAction
}
