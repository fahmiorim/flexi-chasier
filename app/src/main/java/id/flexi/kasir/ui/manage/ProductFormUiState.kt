package id.flexi.kasir.ui.manage

/**
 * State UI untuk layar formulir produk (Tambah/Ubah).
 *
 * @property varianDraft Daftar varian sementara yang sedang diedit.
 * Setiap varian berisi nama dan harga (string untuk input).
 */
data class ProductFormUiState(
    val judulLayar: String = "Tambah Produk",
    val nama: String = "",
    val harga: String = "",
    val stok: String = "",
    val deskripsi: String = "",
    val kategori: String = "",
    val fotoUri: String? = null,
    val favorit: Boolean = false,
    val hargaModal: String = "",
    val apakahTampilHargaModal: Boolean = false,
    val apakahTampilKelolaStok: Boolean = false,
    val apakahTampilVarian: Boolean = false,
    val varianDraft: List<VarianDraft> = emptyList(),
    val daftarKategori: List<String> = emptyList(),
    val pesanKesalahanNama: String? = null,
    val pesanKesalahanHarga: String? = null,
    val pesanKesalahanStok: String? = null,
    val apakahBisaSimpan: Boolean = false,
    val apakahSedangMenyimpan: Boolean = false,
    val apakahBerhasilDisimpan: Boolean = false,
    val apakahTampilDialogHapus: Boolean = false,
)

/**
 * Draft varian yang sedang diedit di form.
 */
data class VarianDraft(
    val nama: String = "",
    val harga: String = "",
)
