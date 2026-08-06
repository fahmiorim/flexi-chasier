package id.flexi.kasir.domain.model

enum class CatalogDisplay {
    List,
    Grid,
}

enum class ReceiptPrintFormat {
    Automatic,
    Manual,
}

enum class PrinterType {
    None,
    Bluetooth,
    Usb,
}

enum class LebarStruk(val mm: Int, val label: String) {
    Mm58(58, "58 mm"),
    Mm80(80, "80 mm"),
}

data class StoreSetting(
    val namaUsaha: String = "Flexi Kasir",
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
    val jumlahTopFavorit: Int = 10,
    val manajemenKasAktif: Boolean = true,
    // Pengaturan struk baru
    val strukHeader: String = "",
    val strukFooter: String = "",
    val lebarStruk: LebarStruk = LebarStruk.Mm58,
    val jumlahCopyCetak: Int = 1,
    val tampilkanLogoDiStruk: Boolean = true,
    val tampilkanPajakDiStruk: Boolean = true,
)
