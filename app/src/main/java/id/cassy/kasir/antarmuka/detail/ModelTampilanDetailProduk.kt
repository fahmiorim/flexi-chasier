package id.cassy.kasir.antarmuka.detail

import androidx.compose.runtime.Immutable

/**
 * Representasi status UI untuk layar detail produk.
 *
 * Model ini menjadi satu sumber kebenaran bagi layar detail produk.
 */
@Immutable
data class ModelTampilanDetailProduk(
    val produkId: String = "",
    val judulLayar: String = "Detail Produk",
    val statusMuat: StatusMuatDetailProduk = StatusMuatDetailProduk.Memuat,
)
