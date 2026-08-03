package id.flexi.kasir.ui.manage

/**
 * Representasi aksi pengguna pada layar formulir produk.
 */
sealed interface ProductFormAction {
    data class UbahNama(val nama: String) : ProductFormAction
    data class UbahHarga(val harga: String) : ProductFormAction
    data class UbahStok(val stok: String) : ProductFormAction
    data class UbahDeskripsi(val deskripsi: String) : ProductFormAction
    data class UbahKategori(val kategori: String) : ProductFormAction
    data class PilihFoto(val uri: String) : ProductFormAction
    data object HapusFoto : ProductFormAction
    data object ToggleFavorit : ProductFormAction
    data class UbahHargaModal(val hargaModal: String) : ProductFormAction
    data object ToggleTampilHargaModal : ProductFormAction
    data object ToggleTampilKelolaStok : ProductFormAction

    /** Aksi varian */
    data object ToggleTampilVarian : ProductFormAction
    data class TambahVarian(val nama: String, val harga: String) : ProductFormAction
    data class UbahNamaVarian(val indeks: Int, val nama: String) : ProductFormAction
    data class UbahHargaVarian(val indeks: Int, val harga: String) : ProductFormAction
    data object HapusVarianTerakhir : ProductFormAction

    data object Simpan : ProductFormAction

    /** Aksi saat pengguna menekan tombol "Hapus Produk" */
    data object MintaHapusProduk : ProductFormAction

    /** Aksi konfirmasi penghapusan produk */
    data object KonfirmasiHapusProduk : ProductFormAction

    /** Aksi pembatalan penghapusan produk */
    data object BatalkanHapusProduk : ProductFormAction
}
