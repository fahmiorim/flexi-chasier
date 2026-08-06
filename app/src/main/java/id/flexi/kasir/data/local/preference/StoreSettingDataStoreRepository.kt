package id.flexi.kasir.data.local.preference

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import id.flexi.kasir.domain.model.LebarStruk
import id.flexi.kasir.domain.model.PrinterType
import id.flexi.kasir.domain.model.ReceiptPrintFormat
import id.flexi.kasir.domain.model.StoreSetting
import id.flexi.kasir.domain.model.CatalogDisplay
import id.flexi.kasir.domain.repository.RepositoriStoreSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStoreStoreSetting by preferencesDataStore(
    name = "pengaturan_toko",
)

class RepositoriStoreSettingDataStore(
    private val konteks: Context,
) : RepositoriStoreSetting {

    override fun ambilPengaturan(): Flow<StoreSetting> {
        return konteks.dataStoreStoreSetting.data.map { prefs ->
            StoreSetting(
                namaUsaha = prefs[Kunci.namaUsaha] ?: "Flexi Kasir",
                logoUri = prefs[Kunci.logoUri] ?: "",
                alamat = prefs[Kunci.alamat] ?: "",
                tagline = prefs[Kunci.tagline] ?: "",
                catalogDisplay = prefs[Kunci.CatalogDisplay]?.let { nilai ->
                    try { CatalogDisplay.valueOf(nilai) } catch (_: Exception) { CatalogDisplay.Grid }
                } ?: CatalogDisplay.Grid,
                PaymentMethodTunaiAktif = prefs[Kunci.PaymentMethodTunaiAktif] ?: true,
                PaymentMethodQrisAktif = prefs[Kunci.PaymentMethodQrisAktif] ?: true,
                receiptPrintFormat = prefs[Kunci.ReceiptPrintFormat]?.let { nilai ->
                    try { ReceiptPrintFormat.valueOf(nilai) } catch (_: Exception) { ReceiptPrintFormat.Manual }
                } ?: ReceiptPrintFormat.Manual,
                printerType = prefs[Kunci.PrinterType]?.let { nilai ->
                    try { PrinterType.valueOf(nilai) } catch (_: Exception) { PrinterType.None }
                } ?: PrinterType.None,
                printerAddress = prefs[Kunci.PrinterAddress] ?: "",
                printerName = prefs[Kunci.PrinterName] ?: "",
                suaraNotifikasiAktif = prefs[Kunci.suaraNotifikasiAktif] ?: true,
                satuanStokDefault = prefs[Kunci.satuanStokDefault] ?: "pcs",
                jumlahTopFavorit = prefs[Kunci.jumlahTopFavorit] ?: 10,
                manajemenKasAktif = prefs[Kunci.manajemenKasAktif] ?: true,
                strukHeader = prefs[Kunci.strukHeader] ?: "",
                strukFooter = prefs[Kunci.strukFooter] ?: "",
                lebarStruk = prefs[Kunci.lebarStruk]?.let { nilai ->
                    try { LebarStruk.valueOf(nilai) } catch (_: Exception) { LebarStruk.Mm58 }
                } ?: LebarStruk.Mm58,
                jumlahCopyCetak = prefs[Kunci.jumlahCopyCetak]?.coerceIn(1, 5) ?: 1,
                tampilkanLogoDiStruk = prefs[Kunci.tampilkanLogoDiStruk] ?: true,
                tampilkanPajakDiStruk = prefs[Kunci.tampilkanPajakDiStruk] ?: true,
            )
        }
    }

    override suspend fun simpanPengaturan(pengaturan: StoreSetting) {
        konteks.dataStoreStoreSetting.edit { prefs ->
            prefs[Kunci.namaUsaha] = pengaturan.namaUsaha.trim()
            prefs[Kunci.logoUri] = pengaturan.logoUri.trim()
            prefs[Kunci.alamat] = pengaturan.alamat.trim()
            prefs[Kunci.tagline] = pengaturan.tagline.trim()
            prefs[Kunci.CatalogDisplay] = pengaturan.catalogDisplay.name
            prefs[Kunci.PaymentMethodTunaiAktif] = pengaturan.PaymentMethodTunaiAktif
            prefs[Kunci.PaymentMethodQrisAktif] = pengaturan.PaymentMethodQrisAktif
            prefs[Kunci.ReceiptPrintFormat] = pengaturan.receiptPrintFormat.name
            prefs[Kunci.PrinterType] = pengaturan.printerType.name
            prefs[Kunci.PrinterAddress] = pengaturan.printerAddress.trim()
            prefs[Kunci.PrinterName] = pengaturan.printerName.trim()
            prefs[Kunci.suaraNotifikasiAktif] = pengaturan.suaraNotifikasiAktif
            prefs[Kunci.satuanStokDefault] = pengaturan.satuanStokDefault.trim()
            prefs[Kunci.jumlahTopFavorit] = pengaturan.jumlahTopFavorit
            prefs[Kunci.manajemenKasAktif] = pengaturan.manajemenKasAktif
            prefs[Kunci.strukHeader] = pengaturan.strukHeader.trim()
            prefs[Kunci.strukFooter] = pengaturan.strukFooter.trim()
            prefs[Kunci.lebarStruk] = pengaturan.lebarStruk.name
            prefs[Kunci.jumlahCopyCetak] = pengaturan.jumlahCopyCetak.coerceIn(1, 5)
            prefs[Kunci.tampilkanLogoDiStruk] = pengaturan.tampilkanLogoDiStruk
            prefs[Kunci.tampilkanPajakDiStruk] = pengaturan.tampilkanPajakDiStruk
        }
    }

    private object Kunci {
        val namaUsaha = stringPreferencesKey("nama_usaha")
        val logoUri = stringPreferencesKey("logo_uri")
        val alamat = stringPreferencesKey("alamat")
        val tagline = stringPreferencesKey("tagline")
        val CatalogDisplay = stringPreferencesKey("tampilan_katalog")
        val PaymentMethodTunaiAktif = booleanPreferencesKey("metode_bayar_tunai_aktif")
        val PaymentMethodQrisAktif = booleanPreferencesKey("metode_bayar_qris_aktif")
        val ReceiptPrintFormat = stringPreferencesKey("format_cetak_struk")
        val PrinterType = stringPreferencesKey("tipe_printer")
        val PrinterAddress = stringPreferencesKey("alamat_printer")
        val PrinterName = stringPreferencesKey("nama_printer")
        val suaraNotifikasiAktif = booleanPreferencesKey("suara_notifikasi_aktif")
        val satuanStokDefault = stringPreferencesKey("satuan_stok_default")
        val jumlahTopFavorit = intPreferencesKey("jumlah_top_favorit")
        val manajemenKasAktif = booleanPreferencesKey("manajemen_kas_aktif")
        val strukHeader = stringPreferencesKey("struk_header")
        val strukFooter = stringPreferencesKey("struk_footer")
        val lebarStruk = stringPreferencesKey("lebar_struk")
        val jumlahCopyCetak = intPreferencesKey("jumlah_copy_cetak")
        val tampilkanLogoDiStruk = booleanPreferencesKey("tampilkan_logo_di_struk")
        val tampilkanPajakDiStruk = booleanPreferencesKey("tampilkan_pajak_di_struk")
    }
}
