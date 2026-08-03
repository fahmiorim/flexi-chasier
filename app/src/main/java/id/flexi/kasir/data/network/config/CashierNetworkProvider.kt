package id.flexi.kasir.data.network.config

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import id.flexi.kasir.data.network.service.ProductNetworkService
import kotlinx.serialization.json.Json
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

    fun buatProductNetworkService(
        alamatDasarApi: String,
        modeDebug: Boolean,
    ): ProductNetworkService {
        val klienHttp = buatKlienHttp(modeDebug = modeDebug)
        val retrofit = buatRetrofit(
            alamatDasarApi = alamatDasarApi,
            klienHttp = klienHttp,
        )

        return retrofit.create(ProductNetworkService::class.java)
    }
}
