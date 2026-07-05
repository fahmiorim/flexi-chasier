package id.cassy.kasir.ranah.kasuspenggunaan

import id.cassy.kasir.ranah.model.HasilOperasiJaringan
import id.cassy.kasir.ranah.model.Produk
import id.cassy.kasir.ranah.repositori.RepositoriProduk
import kotlinx.coroutines.flow.Flow

/**
 * Kasus penggunaan untuk memuat katalog produk dengan strategi local-first.
 */
class MuatKatalogProduk(
    private val repositori: RepositoriProduk,
) {

    /**
     * Mengambil aliran data produk dari penyimpanan lokal.
     */
    fun eksekusi(): Flow<List<Produk>> {
        return repositori.amatiSemuaProduk()
    }

    /**
     * Memastikan katalog awal tersedia pada install baru.
     */
    suspend fun pastikanKatalogAwalTersedia() {
        repositori.pastikanKatalogAwalTersedia()
    }

    /**
     * Memicu pembaruan data dari server ke penyimpanan lokal.
     */
    suspend fun sinkronkan(): HasilOperasiJaringan<Unit> {
        return repositori.sinkronkanKatalog()
    }
}
