package id.cassy.kasir.ranah.model

/**
 * Representasi data untuk produk atau item yang dijual.
 *
 * @property id Identitas internal produk yang stabil. Untuk seed katalog,
 * identitas dibuat deterministik dan mudah dibaca; untuk produk input pengguna,
 * identitas dapat dibuat melalui pembangkit identitas produk lokal.
 * @property nama Nama tampilan produk yang muncul di katalog.
 * @property harga Nilai jual produk dalam Rupiah penuh.
 * @property stokTersedia Kuantitas barang yang masih dapat dijual.
 * @property kodePindai Barcode atau kode internal toko untuk pencarian cepat.
 * @property deskripsi Informasi tambahan tentang produk.
 * @property aktif Status apakah produk ini dapat ditampilkan dan dijual.
 * @property kategori Kategori produk seperti Minuman, Makanan, Sembako, dll.
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
)
