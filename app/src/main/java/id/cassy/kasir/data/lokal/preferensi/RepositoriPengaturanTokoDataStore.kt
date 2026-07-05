package id.cassy.kasir.data.lokal.preferensi

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import id.cassy.kasir.ranah.model.FormatCetakStruk
import id.cassy.kasir.ranah.model.PengaturanToko
import id.cassy.kasir.ranah.model.TampilanKatalog
import id.cassy.kasir.ranah.repositori.RepositoriPengaturanToko
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStorePengaturanToko by preferencesDataStore(
    name = "pengaturan_toko",
)

class RepositoriPengaturanTokoDataStore(
    private val konteks: Context,
) : RepositoriPengaturanToko {

    override fun ambilPengaturan(): Flow<PengaturanToko> {
        return konteks.dataStorePengaturanToko.data.map { prefs ->
            PengaturanToko(
                namaUsaha = prefs[Kunci.namaUsaha] ?: "Cassy Kasir",
                logoUri = prefs[Kunci.logoUri] ?: "",
                alamat = prefs[Kunci.alamat] ?: "",
                tampilanKatalog = prefs[Kunci.tampilanKatalog]?.let { nilai ->
                    try { TampilanKatalog.valueOf(nilai) } catch (_: Exception) { TampilanKatalog.Grid }
                } ?: TampilanKatalog.Grid,
                metodeBayarTunaiAktif = prefs[Kunci.metodeBayarTunaiAktif] ?: true,
                metodeBayarQrisAktif = prefs[Kunci.metodeBayarQrisAktif] ?: true,
                formatCetakStruk = prefs[Kunci.formatCetakStruk]?.let { nilai ->
                    try { FormatCetakStruk.valueOf(nilai) } catch (_: Exception) { FormatCetakStruk.Manual }
                } ?: FormatCetakStruk.Manual,
                suaraNotifikasiAktif = prefs[Kunci.suaraNotifikasiAktif] ?: true,
                satuanStokDefault = prefs[Kunci.satuanStokDefault] ?: "pcs",
            )
        }
    }

    override suspend fun simpanPengaturan(pengaturan: PengaturanToko) {
        konteks.dataStorePengaturanToko.edit { prefs ->
            prefs[Kunci.namaUsaha] = pengaturan.namaUsaha.trim()
            prefs[Kunci.logoUri] = pengaturan.logoUri.trim()
            prefs[Kunci.alamat] = pengaturan.alamat.trim()
            prefs[Kunci.tampilanKatalog] = pengaturan.tampilanKatalog.name
            prefs[Kunci.metodeBayarTunaiAktif] = pengaturan.metodeBayarTunaiAktif
            prefs[Kunci.metodeBayarQrisAktif] = pengaturan.metodeBayarQrisAktif
            prefs[Kunci.formatCetakStruk] = pengaturan.formatCetakStruk.name
            prefs[Kunci.suaraNotifikasiAktif] = pengaturan.suaraNotifikasiAktif
            prefs[Kunci.satuanStokDefault] = pengaturan.satuanStokDefault.trim()
        }
    }

    private object Kunci {
        val namaUsaha = stringPreferencesKey("nama_usaha")
        val logoUri = stringPreferencesKey("logo_uri")
        val alamat = stringPreferencesKey("alamat")
        val tampilanKatalog = stringPreferencesKey("tampilan_katalog")
        val metodeBayarTunaiAktif = booleanPreferencesKey("metode_bayar_tunai_aktif")
        val metodeBayarQrisAktif = booleanPreferencesKey("metode_bayar_qris_aktif")
        val formatCetakStruk = stringPreferencesKey("format_cetak_struk")
        val suaraNotifikasiAktif = booleanPreferencesKey("suara_notifikasi_aktif")
        val satuanStokDefault = stringPreferencesKey("satuan_stok_default")
    }
}
