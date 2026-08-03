package id.flexi.kasir.data.network.interceptor

import id.flexi.kasir.data.auth.TokenStore
import id.flexi.kasir.data.network.model.RefreshRequest
import id.flexi.kasir.data.network.service.AuthNetworkService
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Menyisipkan token akses (Bearer) ke setiap permintaan, dan menukar ulang
 * dengan refresh token saat server membalas 401. Penukaran hanya dilakukan
 * sekali per permintaan untuk menghindari perulangan tak terbatas.
 */
class AuthInterceptor(
    private val tokenStore: TokenStore,
    private val layananAuth: AuthNetworkService,
) : Interceptor {

    override fun intercept(rantai: Interceptor.Chain): Response {
        val permintaan = rantai.request()
        val tokenAkses = tokenStore.aksesToken

        val permintaanTerautentikasi = if (tokenAkses != null) {
            permintaan.newBuilder()
                .header("Authorization", "Bearer $tokenAkses")
                .build()
        } else {
            permintaan
        }

        val respons = rantai.proceed(permintaanTerautentikasi)
        if (respons.code != 401) {
            return respons
        }

        // Permintaan ini sudah memakai Bearer, tetap 401 -> coba tukar refresh token sekali.
        if (permintaan.header("Authorization") == null) {
            return respons
        }

        val tokenRefresh = tokenStore.refreshToken ?: run {
            tokenStore.hapus()
            return respons
        }

        val hasilTukar = runCatching {
            layananAuth.refresh(RefreshRequest(refreshToken = tokenRefresh))
        }
        if (hasilTukar.isFailure) {
            // Refresh gagal -> sesi tidak valid lagi, bersihkan token.
            tokenStore.hapus()
            return respons
        }

        val pasanganToken = hasilTukar.getOrThrow()
        tokenStore.simpan(
            aksesToken = pasanganToken.accessToken,
            refreshToken = pasanganToken.refreshToken,
        )

        val permintaanUlang = permintaan.newBuilder()
            .header("Authorization", "Bearer ${pasanganToken.accessToken}")
            .build()

        return rantai.proceed(permintaanUlang)
    }
}
