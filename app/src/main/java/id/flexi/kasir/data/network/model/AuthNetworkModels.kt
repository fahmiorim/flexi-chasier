package id.flexi.kasir.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Model permintaan dan respons autentikasi Flexi Kasir.
 * Bentuknya mengikuti kontrak API backend (snake_case / camelCase sesuai endpoint).
 */
@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class RegisterRequest(
    @SerialName("namaUsaha")
    val namaUsaha: String,
    @SerialName("namaUser")
    val namaUser: String,
    val email: String,
    val password: String,
)

@Serializable
data class RefreshRequest(
    @SerialName("refreshToken")
    val refreshToken: String,
)

@Serializable
data class LogoutRequest(
    @SerialName("refreshToken")
    val refreshToken: String,
)

@Serializable
data class AuthUserNetwork(
    val id: String,
    val nama: String,
    val email: String,
    val peran: String,
)

@Serializable
data class GeraiNetwork(
    val id: String,
    val nama: String,
    val alamat: String? = null,
    @SerialName("tagline") val tagline: String? = null,
    // Struk
    @SerialName("headerStruk") val headerStruk: String? = null,
    @SerialName("footerStruk") val footerStruk: String? = null,
    @SerialName("ukuranKertas") val ukuranKertas: String? = null,
    // Pajak & Biaya
    @SerialName("tarifPajak") val tarifPajak: Double? = null,
    @SerialName("biayaLayanan") val biayaLayanan: Int? = null,
    // Pembayaran
    @SerialName("metodePembayaran") val metodePembayaran: String? = null,
    // Printer
    @SerialName("printerNama") val printerNama: String? = null,
    @SerialName("printerTipe") val printerTipe: String? = null,
    @SerialName("autoCetak") val autoCetak: Boolean? = null,
    // Branding
    @SerialName("logoUri") val logoUri: String? = null,
    @SerialName("telepon") val telepon: String? = null,
    @SerialName("emailToko") val emailToko: String? = null,
    @SerialName("instagram") val instagram: String? = null,
    @SerialName("whatsapp") val whatsapp: String? = null,
)

@Serializable
data class AuthNetworkResponse(
    @SerialName("accessToken")
    val accessToken: String,
    @SerialName("refreshToken")
    val refreshToken: String,
    val user: AuthUserNetwork,
    val gerai: List<GeraiNetwork> = emptyList(),
)

/**
 * Respons register: backend menahan token sampai email diverifikasi,
 * jadi hanya `perluVerifikasiEmail` + `email` yang dikembalikan.
 */
@Serializable
data class RegisterNetworkResponse(
    val ok: Boolean = false,
    @SerialName("perluVerifikasiEmail")
    val perluVerifikasiEmail: Boolean = false,
    val email: String? = null,
)

@Serializable
data class VerifikasiEmailRequest(
    val email: String,
    val kode: String,
)

@Serializable
data class KirimUlangVerifikasiRequest(
    val email: String,
)

@Serializable
data class VerifikasiNetworkResponse(
    val ok: Boolean = false,
    @SerialName("emailTerverifikasi")
    val emailTerverifikasi: Boolean = false,
)

@Serializable
data class RefreshNetworkResponse(
    @SerialName("accessToken")
    val accessToken: String,
    @SerialName("refreshToken")
    val refreshToken: String,
)

@Serializable
data class LogoutNetworkResponse(
    val ok: Boolean = true,
)

@Serializable
data class LupaPasswordRequest(
    val email: String,
)

@Serializable
data class ResetPasswordRequest(
    val email: String,
    val kode: String,
    @SerialName("passwordBaru")
    val passwordBaru: String,
)

@Serializable
data class ResetNetworkResponse(
    val ok: Boolean = false,
)

@Serializable
data class ApiErrorResponse(
    val error: String? = null,
)
