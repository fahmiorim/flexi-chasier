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
data class ApiErrorResponse(
    val error: String? = null,
)
