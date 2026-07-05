package id.cassy.kasir.ranah.repositori

import id.cassy.kasir.ranah.model.HasilOperasiJaringan
import id.cassy.kasir.ranah.model.Produk
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak repositori untuk mengelola data produk.
 */
interface RepositoriProduk {

    /**
     * Mengamati seluruh produk dari penyimpanan lokal secara reaktif.
     */
    fun amatiSemuaProduk(): Flow<List<Produk>>

    /**
     * Mengamati satu produk berdasarkan identitas produk.
     */
    fun amatiProdukBerdasarkanIdentitas(
        identitasProduk: String,
    ): Flow<Produk?>

    /**
     * Mencari produk berdasarkan kata kunci di lokal.
     */
    fun cariProdukLokal(kataKunci: String): Flow<List<Produk>>

    /**
     * Memastikan katalog awal tersedia untuk mode belajar dan install baru.
     */
    suspend fun pastikanKatalogAwalTersedia()

    /**
     * Memperbarui katalog lokal dengan mengambil data terbaru dari jaringan.
     */
    suspend fun sinkronkanKatalog(): HasilOperasiJaringan<Unit>

    /**
     * Menyimpan atau memperbarui produk di penyimpanan lokal.
     */
    suspend fun simpanProduk(produk: Produk)

    /**
     * Menghapus produk dari penyimpanan lokal berdasarkan identitas unik.
     */
    suspend fun hapusProduk(identitasProduk: String)
}
