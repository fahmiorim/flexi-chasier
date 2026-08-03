package id.flexi.kasir.domain.model

import kotlinx.serialization.Serializable

/**
 * Representasi varian produk (contoh: HOT, ICE, Large, Small).
 *
 * @property nama Nama varian, misal "HOT" atau "Ice".
 * @property harga Harga khusus untuk varian ini.
 */
@Serializable
data class Varian(
    val nama: String,
    val harga: Long,
)

/**
 * Representasi data untuk produk atau item yang dijual.
 *
 * @property id Identitas internal produk yang stabil. Untuk seed katalog,
 * identitas dibuat deterministik dan mudah dibaca; untuk produk input pengguna,
 * identitas dapat dibuat melalui pembangkit identitas produk lokal.
 * @property nama Nama tampilan produk yang muncul di katalog.
 * @property harga Nilai jual produk dalam Rupiah penuh. Jika produk memiliki varian,
 * harga ini menjadi harga dasar awal.
 * @property stokTersedia Kuantitas barang yang masih dapat dijual.
 * @property kodePindai Barcode atau kode internal toko untuk pencarian cepat.
 * @property deskripsi Informasi tambahan tentang produk.
 * @property aktif Status apakah produk ini dapat ditampilkan dan dijual.
 * @property kategori Kategori produk seperti Minuman, Makanan, Sembako, dll.
 * @property fotoUri URI lokal atau URL gambar produk.
 * @property favorit Status apakah produk ditandai sebagai favorit.
 * @property hargaModal Harga modal/pokok produk untuk perhitungan laba.
 * @property varian Daftar varian produk (contoh: HOT, ICE). Jika tidak kosong,
 * varian ini bisa dipilih saat menambahkan ke keranjang.
 */
data class Produk(
    val id: String,
    val nama: String,
    val harga: Long,
    val stokTersedia: Int,
    val kodePindai: String? = null,
    val deskripsi: String = "",
    val aktif: Boolean = true,
    val kategori: String = "",
    val fotoUri: String? = null,
    val favorit: Boolean = false,
    val hargaModal: Long? = null,
    val varian: List<Varian> = emptyList(),
    val apakahStokDiaktifkan: Boolean = false,
)
