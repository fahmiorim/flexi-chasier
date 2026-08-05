package id.flexi.kasir.data.network.config

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import id.flexi.kasir.data.network.service.AuthNetworkService
import id.flexi.kasir.data.network.service.ProductNetworkService
import id.flexi.kasir.data.network.service.SyncNetworkService
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Penyedia objek jaringan inti untuk Flexi Cashier.
 *
 * Tanggung jawab:
 * - membuat konfigurasi Json
 * - membuat OkHttpClient
 * - membuat Retrofit
 * - membuat layanan API
 */
object CashierNetworkProvider {

    val jsonUtama: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
        encodeDefaults = true
        isLenient = false
    }

    fun buatKlienHttp(
        modeDebug: Boolean,
        tambahanInterceptor: Interceptor? = null,
    ): OkHttpClient {
        val pembangun = OkHttpClient.Builder()
            .connectTimeout(
                CashierNetworkConfig.batasWaktuKoneksiDetik,
                TimeUnit.SECONDS,
            )
            .readTimeout(
                CashierNetworkConfig.batasWaktuBacaDetik,
                TimeUnit.SECONDS,
            )
            .writeTimeout(
                CashierNetworkConfig.batasWaktuTulisDetik,
                TimeUnit.SECONDS,
            )

        // Interceptor autentikasi didaftarkan sebelum logging agar log memuat header Authorization.
        tambahanInterceptor?.let { pembangun.addInterceptor(it) }

        if (modeDebug) {
            val interceptorLogging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            pembangun.addInterceptor(interceptorLogging)
        }

        return pembangun.build()
    }

    fun buatRetrofit(
        alamatDasarApi: String,
        klienHttp: OkHttpClient,
        json: Json = jsonUtama,
    ): Retrofit {
        val tipeKontenJson = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl(alamatDasarApi)
            .client(klienHttp)
            .addConverterFactory(json.asConverterFactory(tipeKontenJson))
            .build()
    }

    /**
     * Layanan produk TERPROTEKSI: memakai klien ber-AuthInterceptor agar
     * Bearer token disisipkan otomatis (endpoint /api/produk butuh login).
     */
    fun buatProductNetworkService(
        alamatDasarApi: String,
        modeDebug: Boolean,
        authInterceptor: Interceptor,
    ): ProductNetworkService {
        val klienHttp = buatKlienHttpOtentikasi(
            alamatDasarApi = alamatDasarApi,
            modeDebug = modeDebug,
            authInterceptor = authInterceptor,
        )
        val retrofit = buatRetrofit(
            alamatDasarApi = alamatDasarApi,
            klienHttp = klienHttp,
        )

        return retrofit.create(ProductNetworkService::class.java)
    }

    /**
     * Layanan sinkronisasi TERPROTEKSI: semua endpoint push/pull memakai
     * klien ber-AuthInterceptor (Bearer + refresh otomatis saat 401).
     */
    fun buatSyncNetworkService(
        alamatDasarApi: String,
        modeDebug: Boolean,
        authInterceptor: Interceptor,
    ): SyncNetworkService {
        val klienHttp = buatKlienHttpOtentikasi(
            alamatDasarApi = alamatDasarApi,
            modeDebug = modeDebug,
            authInterceptor = authInterceptor,
        )
        val retrofit = buatRetrofit(
            alamatDasarApi = alamatDasarApi,
            klienHttp = klienHttp,
        )

        return retrofit.create(SyncNetworkService::class.java)
    }

    /**
     * Layanan auth TANPA interceptor autentikasi. Dipakai untuk login/register
     * dan untuk menukar refresh token (menghindari perulangan 401).
     */
    fun buatAuthNetworkService(
        alamatDasarApi: String,
        modeDebug: Boolean,
    ): AuthNetworkService {
        val klienHttp = buatKlienHttp(modeDebug = modeDebug)
        val retrofit = buatRetrofit(
            alamatDasarApi = alamatDasarApi,
            klienHttp = klienHttp,
        )

        return retrofit.create(AuthNetworkService::class.java)
    }

    /**
     * Klien HTTP dengan interceptor autentikasi. Semua endpoint terproteksi
     * memakai klien ini agar otomatis menyisipkan Bearer token.
     */
    fun buatKlienHttpOtentikasi(
        alamatDasarApi: String,
        modeDebug: Boolean,
        authInterceptor: Interceptor,
    ): OkHttpClient {
        return buatKlienHttp(
            modeDebug = modeDebug,
            tambahanInterceptor = authInterceptor,
        )
    }
}
