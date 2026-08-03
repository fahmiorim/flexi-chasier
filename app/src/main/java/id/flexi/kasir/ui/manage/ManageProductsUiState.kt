package id.flexi.kasir.ui.manage

import id.flexi.kasir.domain.model.Produk

/**
 * State UI untuk layar kelola produk.
 *
 * @property judulLayar Judul yang ditampilkan di TopAppBar.
 * @property daftarProduk Daftar produk yang ditampilkan kepada pengguna.
 * @property daftarKategori Daftar kategori unik dari semua produk.
 * @property kataKunciPencarian Teks pencarian aktif.
 * @property statusKonfirmasiHapus Status dialog konfirmasi penghapusan.
 * @property apakahSedangMemuat Status loading data.
 */
data class ManageProductsUiState(
    val judulLayar: String = "Kelola Produk",
    val daftarProduk: List<Produk> = emptyList(),
    val daftarKategori: List<String> = emptyList(),
    val kataKunciPencarian: String = "",
    val kategoriFilter: String = "",
    val statusKonfirmasiHapus: StatusKonfirmasiDeleteProduct = StatusKonfirmasiDeleteProduct(),
    val hppMap: Map<String, Long> = emptyMap(),
    val apakahSedangMemuat: Boolean = true,
)

/**
 * State untuk dialog konfirmasi penghapusan produk.
 */
data class StatusKonfirmasiDeleteProduct(
    val apakahTampil: Boolean = false,
    val identitasProduk: String = "",
    val namaProduk: String = "",
    val judul: String = "Hapus Produk?",
    val deskripsi: String = "",
)
