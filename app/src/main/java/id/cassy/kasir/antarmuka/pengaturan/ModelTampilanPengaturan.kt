package id.cassy.kasir.antarmuka.pengaturan

import id.cassy.kasir.ranah.model.FormatCetakStruk
import id.cassy.kasir.ranah.model.TampilanKatalog

data class ModelTampilanPengaturan(
    val judulLayar: String = "Pengaturan",
    val namaUsaha: String = "",
    val logoUri: String = "",
    val alamat: String = "",
    val tampilanKatalog: TampilanKatalog = TampilanKatalog.Grid,
    val metodeBayarTunaiAktif: Boolean = true,
    val metodeBayarQrisAktif: Boolean = true,
    val formatCetakStruk: FormatCetakStruk = FormatCetakStruk.Manual,
    val suaraNotifikasiAktif: Boolean = true,
    val satuanStokDefault: String = "pcs",
    val apakahSedangMemuat: Boolean = true,
    val apakahSedangMenyimpan: Boolean = false,
    val pesanBerhasil: String? = null,
)
