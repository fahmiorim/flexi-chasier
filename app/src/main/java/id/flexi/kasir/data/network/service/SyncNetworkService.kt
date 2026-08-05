package id.flexi.kasir.data.network.service

import id.flexi.kasir.data.network.model.PerubahanResponse
import id.flexi.kasir.data.network.model.PushBahanRequest
import id.flexi.kasir.data.network.model.PushMejaRequest
import id.flexi.kasir.data.network.model.PushMutasiKasRequest
import id.flexi.kasir.data.network.model.PushPembelianBahanRequest
import id.flexi.kasir.data.network.model.PushPengaturanTokoRequest
import id.flexi.kasir.data.network.model.PushProdukRequest
import id.flexi.kasir.data.network.model.PushResepRequest
import id.flexi.kasir.data.network.model.PushResponse
import id.flexi.kasir.data.network.model.PushSetoranRequest
import id.flexi.kasir.data.network.model.PushShiftKasRequest
import id.flexi.kasir.data.network.model.PushTransaksiRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Kontrak endpoint sinkronisasi dua arah dengan backend.
 *
 * Seluruh endpoint TERPROTEKSI (memakai klien HTTP ber-AuthInterceptor),
 * sehingga token Bearer disisipkan otomatis dan refresh token terjadi
 * saat respons 401.
 */
interface SyncNetworkService {

    // ── PULL ──

    /**
     * Mengambil perubahan semua entitas sejak [sejakEpochMili].
     * Bila [terpotong] pada respons bernilai true, klien harus menarik lagi
     * dengan kursor baru hingga false (data melebihi [batas] baris).
     */
    @GET("api/sync/perubahan")
    suspend fun ambilPerubahan(
        @Query("geraiId")
        geraiId: String,
        @Query("sejakEpochMili")
        sejakEpochMili: Long,
        @Query("batas")
        batas: Int = 500,
    ): PerubahanResponse

    // ── PUSH ──

    @POST("api/sync/produk")
    suspend fun dorongProduk(@Body body: PushProdukRequest): PushResponse

    @POST("api/sync/transaksi")
    suspend fun dorongTransaksi(@Body body: PushTransaksiRequest): PushResponse

    @POST("api/sync/meja")
    suspend fun dorongMeja(@Body body: PushMejaRequest): PushResponse

    @POST("api/sync/shift-kas")
    suspend fun dorongShiftKas(@Body body: PushShiftKasRequest): PushResponse

    @POST("api/sync/mutasi-kas")
    suspend fun dorongMutasiKas(@Body body: PushMutasiKasRequest): PushResponse

    @POST("api/sync/setoran")
    suspend fun dorongSetoran(@Body body: PushSetoranRequest): PushResponse

    @POST("api/sync/bahan")
    suspend fun dorongBahan(@Body body: PushBahanRequest): PushResponse

    @POST("api/sync/pembelian-bahan")
    suspend fun dorongPembelianBahan(@Body body: PushPembelianBahanRequest): PushResponse

    @POST("api/sync/resep")
    suspend fun dorongResep(@Body body: PushResepRequest): PushResponse

    @POST("api/sync/pengaturan-toko")
    suspend fun dorongPengaturanToko(@Body body: PushPengaturanTokoRequest): PushResponse
}
