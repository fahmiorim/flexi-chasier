package id.cassy.kasir.data.jaringan.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Representasi produk mentah dari server.
 *
 * Ini bukan model domain.
 * Bentuknya mengikuti kontrak API dan boleh berubah terpisah dari UI.
 */
@Serializable
data class ResponsProdukJaringan(
    val id: String,
    val nama: String,
    val harga: Long,
    @SerialName("stok_tersedia")
    val stokTersedia: Int,
    @SerialName("kode_pindai")
    val kodePindai: String? = null,
    val deskripsi: String? = null,
    val aktif: Boolean = true,
)
