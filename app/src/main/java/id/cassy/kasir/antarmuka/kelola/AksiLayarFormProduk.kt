package id.cassy.kasir.antarmuka.kelola

/**
 * Representasi aksi pengguna pada layar formulir produk.
 */
sealed interface AksiLayarFormProduk {
    data class UbahNama(val nama: String) : AksiLayarFormProduk
    data class UbahHarga(val harga: String) : AksiLayarFormProduk
    data class UbahStok(val stok: String) : AksiLayarFormProduk
    data class UbahDeskripsi(val deskripsi: String) : AksiLayarFormProduk
    data class UbahKategori(val kategori: String) : AksiLayarFormProduk
    data object Simpan : AksiLayarFormProduk
}
