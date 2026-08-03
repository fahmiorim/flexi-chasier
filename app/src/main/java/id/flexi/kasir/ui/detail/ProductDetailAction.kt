package id.flexi.kasir.ui.detail

import androidx.compose.runtime.Immutable

/**
 * Representasi aksi dari antarmuka detail produk ke ViewModel detail produk.
 */
@Immutable
sealed interface ProductDetailAction {

    /**
     * Aksi saat pengguna mencoba menambahkan produk ke keranjang.
     */
    data object CobaTambahKeKeranjang : ProductDetailAction

    /**
     * Aksi saat pengguna ingin memuat ulang detail produk.
     */
    data object CobaMuatUlang : ProductDetailAction
}
