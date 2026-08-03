package id.flexi.kasir.data.network.service

import id.flexi.kasir.data.network.model.AuthNetworkResponse
import id.flexi.kasir.data.network.model.LoginRequest
import id.flexi.kasir.data.network.model.LogoutNetworkResponse
import id.flexi.kasir.data.network.model.RefreshNetworkResponse
import id.flexi.kasir.data.network.model.RefreshRequest
import id.flexi.kasir.data.network.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Layanan jaringan untuk autentikasi (register, login, refresh token, logout).
 * Interface ini dipakai oleh [id.flexi.kasir.data.auth.AuthRepositoryImpl]
 * dan juga oleh AuthInterceptor untuk menukar refresh token saat token akses kedaluwarsa.
 */
interface AuthNetworkService {
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthNetworkResponse

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthNetworkResponse

    @POST("api/auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): RefreshNetworkResponse

    @POST("api/auth/logout")
    suspend fun logout(): LogoutNetworkResponse
}
