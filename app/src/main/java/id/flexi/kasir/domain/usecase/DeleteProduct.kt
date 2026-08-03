package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.repository.ProductRepository

/**
 * Kasus penggunaan untuk menghapus produk dari katalog.
 *
 * @property repositori Kontrak repositori produk.
 */
class DeleteProduct(
    private val repositori: ProductRepository,
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
        repositori.DeleteProduct(identitasProduk)
    }
}
