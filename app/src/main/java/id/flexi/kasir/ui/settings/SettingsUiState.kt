package id.flexi.kasir.ui.settings

import id.flexi.kasir.domain.model.CatalogDisplay
import id.flexi.kasir.domain.model.LebarStruk
import id.flexi.kasir.domain.model.PrinterType
import id.flexi.kasir.domain.model.ReceiptPrintFormat

data class SettingsUiState(
    val judulLayar: String = "Pengaturan",
    val namaUsaha: String = "",
    val logoUri: String = "",
    val alamat: String = "",
    val tagline: String = "",
    val catalogDisplay: CatalogDisplay = CatalogDisplay.Grid,
    val PaymentMethodTunaiAktif: Boolean = true,
    val PaymentMethodQrisAktif: Boolean = true,
    val receiptPrintFormat: ReceiptPrintFormat = ReceiptPrintFormat.Manual,
    val printerType: PrinterType = PrinterType.None,
    val printerAddress: String = "",
    val printerName: String = "",
    val suaraNotifikasiAktif: Boolean = true,
    val satuanStokDefault: String = "pcs",
    val jumlahTopFavorit: String = "10",
    val manajemenKasAktif: Boolean = true,
    // Pengaturan struk baru
    val strukHeader: String = "",
    val strukFooter: String = "",
    val lebarStruk: LebarStruk = LebarStruk.Mm58,
    val jumlahCopyCetak: String = "1",
    val tampilkanLogoDiStruk: Boolean = true,
    val tampilkanPajakDiStruk: Boolean = true,
    val apakahSedangMemuat: Boolean = true,
    val apakahSedangMenyimpan: Boolean = false,
    val pesanBerhasil: String? = null,
)
