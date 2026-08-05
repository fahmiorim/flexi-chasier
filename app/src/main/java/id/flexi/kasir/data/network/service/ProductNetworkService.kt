package id.flexi.kasir.data.network.service

import id.flexi.kasir.data.network.model.ProductNetworkResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Kontrak endpoint produk untuk sumber data jaringan.
 *
 * Belum dipakai oleh UI pada scope ini.
 * Scope ini hanya menyiapkan fondasi kontrak dan klien API.
 */
interface ProductNetworkService {

    @GET("api/produk")
    suspend fun ambilDaftarProduk(
        @Query("geraiId")
        geraiId: String,
        @Query("kata_kunci")
        kataKunci: String? = null,
    ): List<ProductNetworkResponse>

    @GET("api/produk/{id}")
    suspend fun ambilDetailProduk(
        @Path("id")
        idProduk: String,
    ): ProductNetworkResponse
}
