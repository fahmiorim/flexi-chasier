package id.cassy.kasir.ranah.kasuspenggunaan

import id.cassy.kasir.ranah.model.PreferensiToko
import id.cassy.kasir.ranah.repositori.RepositoriPreferensiToko
import kotlinx.coroutines.flow.Flow

/**
 * Kasus penggunaan untuk memantau perubahan preferensi toko secara reaktif.
 */
class AmatiPreferensiToko(
    private val repositoriPreferensiToko: RepositoriPreferensiToko,
) {
    /**
     * Mengeksekusi aliran pengamatan preferensi.
     *
     * @return Aliran data preferensi toko terbaru.
     */
    operator fun invoke(): Flow<PreferensiToko> {
        return repositoriPreferensiToko.amatiPreferensiToko()
    }
}
