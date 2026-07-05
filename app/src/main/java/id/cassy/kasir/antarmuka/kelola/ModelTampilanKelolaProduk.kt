package id.cassy.kasir.antarmuka.kelola

import id.cassy.kasir.ranah.model.Produk

/**
 * State UI untuk layar kelola produk.
 *
 * @property judulLayar Judul yang ditampilkan di TopAppBar.
 * @property daftarProduk Daftar produk yang ditampilkan kepada pengguna.
 * @property kataKunciPencarian Teks pencarian aktif.
 * @property statusKonfirmasiHapus Status dialog konfirmasi penghapusan.
 * @property apakahSedangMemuat Status loading data.
 */
data class ModelTampilanKelolaProduk(
    val judulLayar: String = "Kelola Produk",
    val daftarProduk: List<Produk> = emptyList(),
    val kataKunciPencarian: String = "",
    val statusKonfirmasiHapus: StatusKonfirmasiHapusProduk = StatusKonfirmasiHapusProduk(),
    val apakahSedangMemuat: Boolean = true,
)

/**
 * State untuk dialog konfirmasi penghapusan produk.
 */
data class StatusKonfirmasiHapusProduk(
    val apakahTampil: Boolean = false,
    val identitasProduk: String = "",
    val namaProduk: String = "",
    val judul: String = "Hapus Produk?",
    val deskripsi: String = "",
)
