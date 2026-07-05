package id.cassy.kasir.ranah.model

enum class TampilanKatalog {
    List,
    Grid,
}

enum class FormatCetakStruk {
    Otomatis,
    Manual,
}

data class PengaturanToko(
    val namaUsaha: String = "Cassy Kasir",
    val logoUri: String = "",
    val alamat: String = "",
    val tampilanKatalog: TampilanKatalog = TampilanKatalog.Grid,
    val metodeBayarTunaiAktif: Boolean = true,
    val metodeBayarQrisAktif: Boolean = true,
    val formatCetakStruk: FormatCetakStruk = FormatCetakStruk.Manual,
    val suaraNotifikasiAktif: Boolean = true,
    val satuanStokDefault: String = "pcs",
)
