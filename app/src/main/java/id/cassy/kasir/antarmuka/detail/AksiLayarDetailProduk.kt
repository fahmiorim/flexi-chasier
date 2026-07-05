package id.cassy.kasir.antarmuka.detail

import androidx.compose.runtime.Immutable

/**
 * Representasi aksi dari antarmuka detail produk ke ViewModel detail produk.
 */
@Immutable
sealed interface AksiLayarDetailProduk {

    /**
     * Aksi saat pengguna mencoba menambahkan produk ke keranjang.
     */
    data object CobaTambahKeKeranjang : AksiLayarDetailProduk

    /**
     * Aksi saat pengguna ingin memuat ulang detail produk.
     */
    data object CobaMuatUlang : AksiLayarDetailProduk
}
