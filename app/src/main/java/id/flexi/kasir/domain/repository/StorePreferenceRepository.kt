package id.flexi.kasir.domain.repository

import id.flexi.kasir.domain.model.StorePreference
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak repository untuk preferensi toko.
 *
 * Mengikuti pola UDF, pengambilan preferensi bersifat reaktif melalui [Flow].
 */
interface RepositoriStorePreference {

    /**
     * Mengamati preferensi toko secara reaktif.
     */
    fun amatiStorePreference(): Flow<StorePreference>

    /**
     * Menyimpan preferensi toko ke media penyimpanan permanen.
     */
    suspend fun simpanStorePreference(
        StorePreference: StorePreference,
    )

    /**
     * Menyimpan metadata keberhasilan sinkronisasi katalog.
     */
    suspend fun simpanSinkronisasiKatalogBerhasil(
        waktuEpochMili: Long,
    )

    /**
     * Menyimpan metadata kegagalan sinkronisasi katalog.
     */
    suspend fun simpanSinkronisasiKatalogGagal(
        pesan: String,
    )
}
