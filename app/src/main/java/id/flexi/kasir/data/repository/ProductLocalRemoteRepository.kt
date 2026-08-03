package id.flexi.kasir.data.repository

import id.flexi.kasir.data.network.service.ProductNetworkService
import id.flexi.kasir.data.network.mapping.keDomain
import id.flexi.kasir.data.local.database.FlexiCashierDatabase
import id.flexi.kasir.data.network.validation.validasiDaftarProdukJaringan
import id.flexi.kasir.data.local.mapping.keDomain
import id.flexi.kasir.data.local.mapping.keLokal
import id.flexi.kasir.domain.sample.SampleProductCatalog
import id.flexi.kasir.domain.model.NetworkOperationResult
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Implementasi repositori produk yang menggabungkan data lokal dan jaringan.
 *
 * Untuk Flexi Cashier saat ini, Room tetap menjadi sumber data utama.
 * Jaringan hanya menjadi sumber pembaruan katalog saat scope backend sudah siap.
 */
class ProductRepositoryLokalRemote(
    private val basisData: FlexiCashierDatabase,
    private val layananJaringan: ProductNetworkService,
) : ProductRepository {

    private val aksesDataProduk = basisData.LocalProductDao()

    override fun amatiSemuaProduk(): Flow<List<Produk>> {
        return aksesDataProduk.amatiSemuaProduk().map { daftarLokal ->
            daftarLokal.map { produkLokal ->
                produkLokal.keDomain()
            }
        }
    }

    override fun ObserveProductById(
        identitasProduk: String,
    ): Flow<Produk?> {
        return aksesDataProduk
            .ObserveProductById(identitasProduk)
            .map { produkLokal ->
                produkLokal?.keDomain()
            }
    }

    override fun cariProdukLokal(kataKunci: String): Flow<List<Produk>> {
        return aksesDataProduk.cariProduk(kataKunci).map { daftarLokal ->
            daftarLokal.map { produkLokal ->
                produkLokal.keDomain()
            }
        }
    }

    override suspend fun pastikanKatalogAwalTersedia() {
        if (aksesDataProduk.hitungJumlahProduk() > 0) {
            return
        }

        val daftarProdukAwal = SampleProductCatalog.daftarAwal()
            .map { produk ->
                produk.keLokal()
            }

        aksesDataProduk.simpanBanyakProduk(daftarProdukAwal)
    }

    override suspend fun sinkronkanKatalog(): NetworkOperationResult<Unit> {
        return try {
            val responsJaringan = layananJaringan.ambilDaftarProduk()

            validasiDaftarProdukJaringan(
                daftarProduk = responsJaringan,
            )

            val daftarProdukDomain = responsJaringan.keDomain()

            // Simpan favorit lokal SEBELUM sync agar tidak tertimpa data server
            val daftarIdProdukJaringan = daftarProdukDomain.map { it.id }
            val favoritLokal = aksesDataProduk
                .ambilProdukBerdasarkanDaftarIdentitas(daftarIdProdukJaringan)
                .groupBy { it.id }
                .mapValues { (_, daftar) -> daftar.any { it.favorit } }

            aksesDataProduk.simpanBanyakProduk(
                daftarProdukDomain.map { produk ->
                    produk.keLokal()
                },
            )

            // Pulihkan favorit lokal setelah sync
            if (favoritLokal.isNotEmpty()) {
                val daftarIdFavorit = favoritLokal.filter { it.value }.keys.toList()
                if (daftarIdFavorit.isNotEmpty()) {
                    aksesDataProduk.tandaiSebagaiFavorit(daftarIdFavorit)
                }
            }

            NetworkOperationResult.Berhasil(Unit)
        } catch (_: IOException) {
            NetworkOperationResult.GagalJaringan(
                pesan = "Koneksi internet bermasalah. Coba lagi?",
            )
        } catch (kesalahanHttp: HttpException) {
            NetworkOperationResult.GagalServer(
                kode = kesalahanHttp.code(),
                pesan = "Server bermasalah (Error ${kesalahanHttp.code()}). Coba beberapa saat lagi.",
            )
        } catch (_: SerializationException) {
            NetworkOperationResult.GagalServer(
                kode = 500,
                pesan = "Format data dari server tidak sesuai. Hubungi bantuan teknis.",
            )
        } catch (kesalahanValidasi: IllegalArgumentException) {
            NetworkOperationResult.GagalServer(
                kode = 422,
                pesan = kesalahanValidasi.message
                    ?: "Data dari server tidak valid. Hubungi bantuan teknis.",
            )
        } catch (_: Exception) {
            NetworkOperationResult.GagalServer(
                kode = 500,
                pesan = "Gagal memperbarui katalog. Coba lagi?",
            )
        }
    }

    override suspend fun SaveProduct(produk: Produk) {
        aksesDataProduk.SaveProduct(produk.keLokal())
    }

    override suspend fun DeleteProduct(identitasProduk: String) {
        aksesDataProduk.DeleteProduct(identitasProduk)
    }

    override suspend fun tandaiProdukFavorit(
        daftarIdProduk: List<String>,
    ) {
        if (daftarIdProduk.isEmpty()) return
        aksesDataProduk.tandaiSebagaiFavorit(daftarIdProduk)
    }
}
