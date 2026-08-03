package id.flexi.kasir.data.local.preference

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import id.flexi.kasir.domain.model.StorePreference
import id.flexi.kasir.domain.repository.RepositoriStorePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Delegasi untuk mengakses DataStore Preferences.
 */
private val Context.dataStoreStorePreference by preferencesDataStore(
    name = "preferensi_toko",
)

/**
 * Implementasi repository preferensi toko berbasis Jetpack DataStore Preferences.
 *
 * @property konteks Context Android untuk akses file DataStore.
 */
class RepositoriStorePreferenceDataStore(
    private val konteks: Context,
) : RepositoriStorePreference {

    override fun amatiStorePreference(): Flow<StorePreference> {
        return konteks.dataStoreStorePreference.data.map { preferensi ->
            StorePreference(
                namaToko = preferensi[KunciPreferensi.namaToko] ?: "Flexi Cashier",
                alamatToko = preferensi[KunciPreferensi.alamatToko] ?: "",
                basisPoinPajakDefault = preferensi[KunciPreferensi.basisPoinPajakDefault] ?: 0,
                biayaLayananDefault = preferensi[KunciPreferensi.biayaLayananDefault] ?: 0L,
                waktuSinkronisasiKatalogTerakhirEpochMili = preferensi[KunciPreferensi.waktuSinkronisasiKatalogTerakhirEpochMili],
                pesanGagalSinkronisasiKatalogTerakhir = preferensi[KunciPreferensi.pesanGagalSinkronisasiKatalogTerakhir],
                gridBaris = preferensi[KunciPreferensi.gridBaris] ?: 0,
                gridKolom = preferensi[KunciPreferensi.gridKolom] ?: 0,
            )
        }
    }

    override suspend fun simpanStorePreference(
        StorePreference: StorePreference,
    ) {
        konteks.dataStoreStorePreference.edit { preferensi ->
            preferensi[KunciPreferensi.namaToko] = StorePreference.namaToko.trim()
            preferensi[KunciPreferensi.alamatToko] = StorePreference.alamatToko.trim()
            preferensi[KunciPreferensi.basisPoinPajakDefault] =
                StorePreference.basisPoinPajakDefault
            preferensi[KunciPreferensi.biayaLayananDefault] =
                StorePreference.biayaLayananDefault

            StorePreference.waktuSinkronisasiKatalogTerakhirEpochMili?.let { waktu ->
                preferensi[KunciPreferensi.waktuSinkronisasiKatalogTerakhirEpochMili] = waktu
            } ?: preferensi.remove(KunciPreferensi.waktuSinkronisasiKatalogTerakhirEpochMili)

            StorePreference.pesanGagalSinkronisasiKatalogTerakhir
                ?.trim()
                ?.takeIf { pesan -> pesan.isNotBlank() }
                ?.let { pesan ->
                    preferensi[KunciPreferensi.pesanGagalSinkronisasiKatalogTerakhir] = pesan
                } ?: preferensi.remove(KunciPreferensi.pesanGagalSinkronisasiKatalogTerakhir)

            preferensi[KunciPreferensi.gridBaris] = StorePreference.gridBaris
            preferensi[KunciPreferensi.gridKolom] = StorePreference.gridKolom
        }
    }

    override suspend fun simpanSinkronisasiKatalogBerhasil(
        waktuEpochMili: Long,
    ) {
        require(waktuEpochMili >= 0L) {
            "Waktu sinkronisasi katalog tidak valid."
        }

        konteks.dataStoreStorePreference.edit { preferensi ->
            preferensi[KunciPreferensi.waktuSinkronisasiKatalogTerakhirEpochMili] = waktuEpochMili
            preferensi.remove(KunciPreferensi.pesanGagalSinkronisasiKatalogTerakhir)
        }
    }

    override suspend fun simpanSinkronisasiKatalogGagal(
        pesan: String,
    ) {
        val pesanBersih = pesan.trim().ifBlank {
            "Sinkronisasi katalog gagal."
        }

        konteks.dataStoreStorePreference.edit { preferensi ->
            preferensi.remove(KunciPreferensi.waktuSinkronisasiKatalogTerakhirEpochMili)
            preferensi[KunciPreferensi.pesanGagalSinkronisasiKatalogTerakhir] = pesanBersih
        }
    }

    /**
     * Kunci identifikasi untuk setiap item data di DataStore.
     */
    private object KunciPreferensi {
        val namaToko = stringPreferencesKey("nama_toko")
        val alamatToko = stringPreferencesKey("alamat_toko")
        val basisPoinPajakDefault = intPreferencesKey("basis_poin_pajak_default")
        val biayaLayananDefault = longPreferencesKey("biaya_layanan_default")
        val waktuSinkronisasiKatalogTerakhirEpochMili =
            longPreferencesKey("waktu_sinkronisasi_katalog_terakhir_epoch_mili")
        val pesanGagalSinkronisasiKatalogTerakhir =
            stringPreferencesKey("pesan_gagal_sinkronisasi_katalog_terakhir")
        val gridBaris = intPreferencesKey("grid_baris")
        val gridKolom = intPreferencesKey("grid_kolom")
    }
}
