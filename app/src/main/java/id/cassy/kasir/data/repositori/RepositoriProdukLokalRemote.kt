package id.cassy.kasir.data.repositori

import id.cassy.kasir.data.jaringan.layanan.LayananProdukJaringan
import id.cassy.kasir.data.jaringan.pemetaan.keDomain
import id.cassy.kasir.data.lokal.basisdata.BasisDataCassyKasir
import id.cassy.kasir.data.jaringan.validasi.validasiDaftarProdukJaringan
import id.cassy.kasir.data.lokal.pemetaan.keDomain
import id.cassy.kasir.data.lokal.pemetaan.keLokal
import id.cassy.kasir.ranah.contoh.KatalogProdukContoh
import id.cassy.kasir.ranah.model.HasilOperasiJaringan
import id.cassy.kasir.ranah.model.Produk
import id.cassy.kasir.ranah.repositori.RepositoriProduk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Implementasi repositori produk yang menggabungkan data lokal dan jaringan.
 *
 * Untuk Cassy Kasir saat ini, Room tetap menjadi sumber data utama.
 * Jaringan hanya menjadi sumber pembaruan katalog saat scope backend sudah siap.
 */
class RepositoriProdukLokalRemote(
    private val basisData: BasisDataCassyKasir,
    private val layananJaringan: LayananProdukJaringan,
) : RepositoriProduk {

    private val aksesDataProduk = basisData.aksesDataProdukLokal()

    override fun amatiSemuaProduk(): Flow<List<Produk>> {
        return aksesDataProduk.amatiSemuaProduk().map { daftarLokal ->
            daftarLokal.map { produkLokal ->
                produkLokal.keDomain()
            }
        }
    }

    override fun amatiProdukBerdasarkanIdentitas(
        identitasProduk: String,
    ): Flow<Produk?> {
        return aksesDataProduk
            .amatiProdukBerdasarkanIdentitas(identitasProduk)
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

        val daftarProdukAwal = KatalogProdukContoh.daftarAwal()
            .map { produk ->
                produk.keLokal()
            }

        aksesDataProduk.simpanBanyakProduk(daftarProdukAwal)
    }

    override suspend fun sinkronkanKatalog(): HasilOperasiJaringan<Unit> {
        return try {
            val responsJaringan = layananJaringan.ambilDaftarProduk()

            validasiDaftarProdukJaringan(
                daftarProduk = responsJaringan,
            )

            val daftarProdukDomain = responsJaringan.keDomain()

            aksesDataProduk.simpanBanyakProduk(
                daftarProdukDomain.map { produk ->
                    produk.keLokal()
                },
            )

            HasilOperasiJaringan.Berhasil(Unit)
        } catch (_: IOException) {
            HasilOperasiJaringan.GagalJaringan(
                pesan = "Koneksi internet bermasalah. Coba lagi?",
            )
        } catch (kesalahanHttp: HttpException) {
            HasilOperasiJaringan.GagalServer(
                kode = kesalahanHttp.code(),
                pesan = "Server bermasalah (Error ${kesalahanHttp.code()}). Coba beberapa saat lagi.",
            )
        } catch (_: SerializationException) {
            HasilOperasiJaringan.GagalServer(
                kode = 500,
                pesan = "Format data dari server tidak sesuai. Hubungi bantuan teknis.",
            )
        } catch (kesalahanValidasi: IllegalArgumentException) {
            HasilOperasiJaringan.GagalServer(
                kode = 422,
                pesan = kesalahanValidasi.message
                    ?: "Data dari server tidak valid. Hubungi bantuan teknis.",
            )
        } catch (_: Exception) {
            HasilOperasiJaringan.GagalServer(
                kode = 500,
                pesan = "Gagal memperbarui katalog. Coba lagi?",
            )
        }
    }

    override suspend fun simpanProduk(produk: Produk) {
        aksesDataProduk.simpanProduk(produk.keLokal())
    }

    override suspend fun hapusProduk(identitasProduk: String) {
        aksesDataProduk.hapusProduk(identitasProduk)
    }
}
