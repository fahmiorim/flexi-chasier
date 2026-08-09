package id.flexi.kasir.domain.repository

import id.flexi.kasir.domain.model.NetworkOperationResult
import id.flexi.kasir.domain.model.Produk
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak repositori untuk mengelola data produk.
 */
interface ProductRepository {

    /**
     * Mengamati seluruh produk dari penyimpanan lokal secara reaktif.
     */
    fun amatiSemuaProduk(): Flow<List<Produk>>

    /**
     * Mengamati satu produk berdasarkan identitas produk.
     */
    fun ObserveProductById(
        identitasProduk: String,
    ): Flow<Produk?>

    /**
     * Mencari produk berdasarkan kata kunci di lokal.
     */
    fun cariProdukLokal(kataKunci: String): Flow<List<Produk>>

    /**
     * Memperbarui katalog lokal dengan mengambil data terbaru dari jaringan.
     */
    suspend fun sinkronkanKatalog(): NetworkOperationResult<Unit>

    /**
     * Menyimpan atau memperbarui produk di penyimpanan lokal.
     */
    suspend fun SaveProduct(produk: Produk)

    /**
     * Menghapus produk dari penyimpanan lokal berdasarkan identitas unik.
     */
    suspend fun DeleteProduct(identitasProduk: String)

    /**
     * Menandai daftar produk sebagai favorit berdasarkan daftar ID.
     *
     * @param daftarIdProduk Daftar ID produk yang akan difavoritkan.
     */
    suspend fun tandaiProdukFavorit(
        daftarIdProduk: List<String>,
    )
}
