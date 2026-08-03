package id.flexi.kasir.ui.navigation

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.StateFlow

/**
 * Kumpulan helper untuk hasil navigasi lintas layar.
 *
 * Pada scope ini hasil navigasi dipakai untuk mengirim pesan sukses
 * setelah user menambah produk dari layar detail lalu kembali ke layar utama.
 */
object CashierNavigationResult {
    const val kunciPesanTambahProdukDariDetail: String = "kunci_pesan_tambah_produk_dari_detail"
}

// ── Produk Detail ──

/**
 * Menyimpan pesan hasil tambah produk dari layar detail.
 */
fun SavedStateHandle.simpanPesanTambahProdukDariDetail(
    pesan: String,
) {
    set(
        CashierNavigationResult.kunciPesanTambahProdukDariDetail,
        pesan,
    )
}

/**
 * Mengambil alur pesan hasil tambah produk dari layar detail.
 */
fun SavedStateHandle.ambilAlurPesanTambahProdukDariDetail(): StateFlow<String?> {
    return getStateFlow(
        CashierNavigationResult.kunciPesanTambahProdukDariDetail,
        null,
    )
}

/**
 * Mengosongkan pesan hasil tambah produk setelah dipakai.
 */
fun SavedStateHandle.konsumsiPesanTambahProdukDariDetail() {
    set(
        CashierNavigationResult.kunciPesanTambahProdukDariDetail,
        null,
    )
}
