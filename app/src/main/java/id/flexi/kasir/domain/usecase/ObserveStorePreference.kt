package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.StorePreference
import id.flexi.kasir.domain.repository.RepositoriStorePreference
import kotlinx.coroutines.flow.Flow

/**
 * Kasus penggunaan untuk memantau perubahan preferensi toko secara reaktif.
 */
class AmatiStorePreference(
    private val repositoriStorePreference: RepositoriStorePreference,
) {
    /**
     * Mengeksekusi aliran pengamatan preferensi.
     *
     * @return Aliran data preferensi toko terbaru.
     */
    operator fun invoke(): Flow<StorePreference> {
        return repositoriStorePreference.amatiStorePreference()
    }
}
