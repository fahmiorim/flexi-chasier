package id.flexi.kasir.ui.detail

import androidx.compose.runtime.Immutable

/**
 * Representasi status UI untuk layar detail produk.
 *
 * Model ini menjadi satu sumber kebenaran bagi layar detail produk.
 */
@Immutable
data class ProductDetailUiState(
    val produkId: String = "",
    val judulLayar: String = "Detail Produk",
    val statusMuat: ProductDetailLoadStatus = ProductDetailLoadStatus.Memuat,
)
