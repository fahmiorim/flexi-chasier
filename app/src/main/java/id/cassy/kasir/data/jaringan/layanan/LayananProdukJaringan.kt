package id.cassy.kasir.data.jaringan.layanan

import id.cassy.kasir.data.jaringan.model.ResponsProdukJaringan
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Kontrak endpoint produk untuk sumber data jaringan.
 *
 * Belum dipakai oleh UI pada scope ini.
 * Scope ini hanya menyiapkan fondasi kontrak dan klien API.
 */
interface LayananProdukJaringan {

    @GET("api/produk")
    suspend fun ambilDaftarProduk(
        @Query("kata_kunci")
        kataKunci: String? = null,
    ): List<ResponsProdukJaringan>

    @GET("api/produk/{id}")
    suspend fun ambilDetailProduk(
        @Path("id")
        idProduk: String,
    ): ResponsProdukJaringan
}
