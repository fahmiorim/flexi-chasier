package id.cassy.kasir.antarmuka.detail

import androidx.compose.runtime.Immutable

/**
 * Representasi status aksi utama pada layar detail produk.
 */
@Immutable
data class StatusAksiDetailProduk(
    val label: String,
    val aktif: Boolean,
    val keterangan: String? = null,
)
