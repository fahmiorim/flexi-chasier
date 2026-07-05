package id.cassy.kasir.antarmuka.kelola

/**
 * State UI untuk layar formulir produk (Tambah/Ubah).
 */
data class ModelTampilanFormProduk(
    val judulLayar: String = "Tambah Produk",
    val nama: String = "",
    val harga: String = "",
    val stok: String = "",
    val deskripsi: String = "",
    val kategori: String = "",
    val pesanKesalahanNama: String? = null,
    val pesanKesalahanHarga: String? = null,
    val pesanKesalahanStok: String? = null,
    val apakahBisaSimpan: Boolean = false,
    val apakahSedangMenyimpan: Boolean = false,
    val apakahBerhasilDisimpan: Boolean = false,
)
