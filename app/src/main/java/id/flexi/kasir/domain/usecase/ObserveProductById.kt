package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow

/**
 * Kasus penggunaan untuk mengamati satu produk berdasarkan identitas produk.
 */
class ObserveProductById(
    private val repositori: ProductRepository,
) {

    operator fun invoke(
        identitasProduk: String,
    ): Flow<Produk?> {
        return repositori.ObserveProductById(identitasProduk)
    }
}
