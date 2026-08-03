package id.flexi.kasir.ui.detail

import androidx.compose.runtime.Immutable

/**
 * Representasi efek sekali pakai dari layar detail produk.
 *
 * Efek dipakai untuk kejadian sesaat yang harus ditangani
 * oleh lapisan luar, seperti integrasi dengan Transaction aktif.
 */
@Immutable
sealed interface ProductDetailEffect {

    /**
     * Permintaan untuk menambahkan produk ke keranjang Transaction aktif.
     */
    data class MintaTambahKeKeranjang(
        val produkId: String,
        val namaProduk: String,
    ) : ProductDetailEffect
}
