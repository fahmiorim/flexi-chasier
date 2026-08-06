package id.flexi.kasir.data.network.service

import id.flexi.kasir.data.network.model.AuthNetworkResponse
import id.flexi.kasir.data.network.model.KirimUlangVerifikasiRequest
import id.flexi.kasir.data.network.model.LoginRequest
import id.flexi.kasir.data.network.model.LogoutNetworkResponse
import id.flexi.kasir.data.network.model.LupaPasswordRequest
import id.flexi.kasir.data.network.model.ResetNetworkResponse
import id.flexi.kasir.data.network.model.ResetPasswordRequest
import id.flexi.kasir.data.network.model.RefreshNetworkResponse
import id.flexi.kasir.data.network.model.RefreshRequest
import id.flexi.kasir.data.network.model.RegisterNetworkResponse
import id.flexi.kasir.data.network.model.RegisterRequest
import id.flexi.kasir.data.network.model.VerifikasiEmailRequest
import id.flexi.kasir.data.network.model.VerifikasiNetworkResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Layanan jaringan untuk autentikasi (register, login, refresh token, logout,
 * verifikasi email). Interface ini dipakai oleh [id.flexi.kasir.data.auth.AuthRepositoryImpl]
 * dan juga oleh AuthInterceptor untuk menukar refresh token saat token akses kedaluwarsa.
 */
interface AuthNetworkService {
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): RegisterNetworkResponse

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthNetworkResponse

    @POST("api/auth/verifikasi-email")
    suspend fun verifikasiEmail(@Body body: VerifikasiEmailRequest): VerifikasiNetworkResponse

    @POST("api/auth/kirim-ulang-verifikasi")
    suspend fun kirimUlangVerifikasi(@Body body: KirimUlangVerifikasiRequest): VerifikasiNetworkResponse

    @POST("api/auth/lupa-password")
    suspend fun lupaPassword(@Body body: LupaPasswordRequest): ResetNetworkResponse

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): ResetNetworkResponse

    @POST("api/auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): RefreshNetworkResponse

    @POST("api/auth/logout")
    suspend fun logout(): LogoutNetworkResponse
}
