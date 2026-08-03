package id.flexi.kasir.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Representasi produk mentah dari server.
 *
 * Ini bukan model domain.
 * Bentuknya mengikuti kontrak API dan boleh berubah terpisah dari UI.
 */
@Serializable
data class ProductNetworkResponse(
    val id: String,
    val nama: String,
    val harga: Long,
    @SerialName("stok_tersedia")
    val stokTersedia: Int,
    @SerialName("kode_pindai")
    val kodePindai: String? = null,
    val deskripsi: String? = null,
    val aktif: Boolean = true,
    @SerialName("foto_uri")
    val fotoUri: String? = null,
    val favorit: Boolean = false,
    @SerialName("harga_modal")
    val hargaModal: Long? = null,
    val kategori: String = "",
    @SerialName("varian_json")
    val varianJson: String? = null,
    @SerialName("apakah_stok_diaktifkan")
    val apakahStokDiaktifkan: Boolean = false,
)
