package id.cassy.kasir.antarmuka.detail

import androidx.compose.runtime.Immutable

@Immutable
sealed interface StatusMuatDetailProduk {

    @Immutable
    data object Memuat : StatusMuatDetailProduk

    @Immutable
    data class Berhasil(
        val namaProduk: String,
        val hargaProduk: String,
        val stokTersedia: Int,
        val deskripsiProduk: String,
        val statusAksi: StatusAksiDetailProduk,
    ) : StatusMuatDetailProduk

    @Immutable
    data class Kosong(
        val judul: String,
        val deskripsi: String,
    ) : StatusMuatDetailProduk

    @Immutable
    data class Gagal(
        val judul: String,
        val deskripsi: String,
    ) : StatusMuatDetailProduk
}
