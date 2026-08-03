package id.flexi.kasir.ui.detail

import androidx.compose.runtime.Immutable

@Immutable
sealed interface ProductDetailLoadStatus {

    @Immutable
    data object Memuat : ProductDetailLoadStatus

    @Immutable
    data class Berhasil(
        val namaProduk: String,
        val hargaProduk: String,
        val hppProduk: String? = null,
        val marginProduk: String? = null,
        val stokTersedia: Int,
        val apakahStokDiaktifkan: Boolean = false,
        val deskripsiProduk: String,
        val fotoUri: String? = null,
        val statusAksi: ProductDetailActionStatus,
    ) : ProductDetailLoadStatus

    @Immutable
    data class Kosong(
        val judul: String,
        val deskripsi: String,
    ) : ProductDetailLoadStatus

    @Immutable
    data class Gagal(
        val judul: String,
        val deskripsi: String,
    ) : ProductDetailLoadStatus
}
