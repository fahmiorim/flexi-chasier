package id.flexi.kasir.ui.detail

import androidx.compose.runtime.Immutable

/**
 * Representasi status aksi utama pada layar detail produk.
 */
@Immutable
data class ProductDetailActionStatus(
    val label: String,
    val aktif: Boolean,
    val keterangan: String? = null,
)
