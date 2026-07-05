package id.cassy.kasir.ranah.kasuspenggunaan

import id.cassy.kasir.ranah.model.Produk
import id.cassy.kasir.ranah.repositori.RepositoriProduk
import kotlinx.coroutines.flow.Flow

/**
 * Kasus penggunaan untuk mengamati satu produk berdasarkan identitas produk.
 */
class AmatiProdukBerdasarkanIdentitas(
    private val repositori: RepositoriProduk,
) {

    operator fun invoke(
        identitasProduk: String,
    ): Flow<Produk?> {
        return repositori.amatiProdukBerdasarkanIdentitas(identitasProduk)
    }
}
