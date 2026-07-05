package id.cassy.kasir.antarmuka.detail

import androidx.compose.runtime.Immutable

/**
 * Representasi efek sekali pakai dari layar detail produk.
 *
 * Efek dipakai untuk kejadian sesaat yang harus ditangani
 * oleh lapisan luar, seperti integrasi dengan transaksi aktif.
 */
@Immutable
sealed interface EfekLayarDetailProduk {

    /**
     * Permintaan untuk menambahkan produk ke keranjang transaksi aktif.
     */
    data class MintaTambahKeKeranjang(
        val produkId: String,
        val namaProduk: String,
    ) : EfekLayarDetailProduk
}
