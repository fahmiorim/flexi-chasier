package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.NetworkOperationResult
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow

/**
 * Kasus penggunaan untuk memuat katalog produk dengan strategi local-first.
 */
class LoadProductCatalog(
    private val repositori: ProductRepository,
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
    suspend fun sinkronkan(): NetworkOperationResult<Unit> {
        return repositori.sinkronkanKatalog()
    }
}
