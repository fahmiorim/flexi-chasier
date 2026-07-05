package id.cassy.kasir.antarmuka.navigasi

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.StateFlow

/**
 * Kumpulan helper untuk hasil navigasi lintas layar.
 *
 * Pada scope ini hasil navigasi dipakai untuk mengirim pesan sukses
 * setelah user menambah produk dari layar detail lalu kembali ke layar utama.
 */
object HasilNavigasiKasir {
    const val kunciPesanTambahProdukDariDetail: String = "kunci_pesan_tambah_produk_dari_detail"
}

/**
 * Menyimpan pesan hasil tambah produk dari layar detail.
 */
fun SavedStateHandle.simpanPesanTambahProdukDariDetail(
    pesan: String,
) {
    set(
        HasilNavigasiKasir.kunciPesanTambahProdukDariDetail,
        pesan,
    )
}

/**
 * Mengambil alur pesan hasil tambah produk dari layar detail.
 */
fun SavedStateHandle.ambilAlurPesanTambahProdukDariDetail(): StateFlow<String?> {
    return getStateFlow(
        HasilNavigasiKasir.kunciPesanTambahProdukDariDetail,
        null,
    )
}

/**
 * Mengosongkan pesan hasil tambah produk setelah dipakai.
 */
fun SavedStateHandle.konsumsiPesanTambahProdukDariDetail() {
    set(
        HasilNavigasiKasir.kunciPesanTambahProdukDariDetail,
        null,
    )
}
