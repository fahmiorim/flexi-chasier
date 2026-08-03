package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.StorePreference
import id.flexi.kasir.domain.repository.RepositoriStorePreference

/**
 * Kasus penggunaan untuk memperbarui pengaturan atau preferensi toko.
 */
class SimpanStorePreference(
    private val repositoriStorePreference: RepositoriStorePreference,
) {
    /**
     * Menyimpan data preferensi toko baru.
     *
     * @param StorePreference Objek model preferensi yang akan disimpan.
     */
    suspend operator fun invoke(
        StorePreference: StorePreference,
    ) {
        repositoriStorePreference.simpanStorePreference(
            StorePreference = StorePreference,
        )
    }

    /**
     * Menyimpan metadata keberhasilan sinkronisasi katalog.
     */
    suspend fun simpanSinkronisasiKatalogBerhasil(
        waktuEpochMili: Long,
    ) {
        repositoriStorePreference.simpanSinkronisasiKatalogBerhasil(
            waktuEpochMili = waktuEpochMili,
        )
    }

    /**
     * Menyimpan metadata kegagalan sinkronisasi katalog.
     */
    suspend fun simpanSinkronisasiKatalogGagal(
        pesan: String,
    ) {
        repositoriStorePreference.simpanSinkronisasiKatalogGagal(
            pesan = pesan,
        )
    }
}
