package id.cassy.kasir.ranah.kasuspenggunaan

import id.cassy.kasir.ranah.repositori.RepositoriProduk

/**
 * Kasus penggunaan untuk menghapus produk dari katalog.
 *
 * @property repositori Kontrak repositori produk.
 */
class HapusProduk(
    private val repositori: RepositoriProduk,
) {

    /**
     * Menghapus produk berdasarkan identitas unik.
     *
     * @param identitasProduk ID produk yang akan dihapus.
     */
    suspend fun eksekusi(identitasProduk: String) {
        if (identitasProduk.isBlank()) {
            return
        }
        repositori.hapusProduk(identitasProduk)
    }
}
