package id.cassy.kasir.data.jaringan.validasi

import id.cassy.kasir.data.jaringan.model.ResponsProdukJaringan
import org.junit.Test

/**
 * Pengujian unit untuk validasi produk jaringan.
 */
class PengujianValidasiProdukJaringan {

    @Test
    fun daftarProdukValidDiterima() {
        validasiDaftarProdukJaringan(
            daftarProduk = listOf(
                produkContoh(
                    id = "produk-kopi",
                    kodePindai = "kode-001",
                ),
                produkContoh(
                    id = "produk-teh",
                    kodePindai = "kode-002",
                ),
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun daftarKosongDitolak() {
        validasiDaftarProdukJaringan(
            daftarProduk = emptyList(),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun identitasKosongDitolak() {
        validasiDaftarProdukJaringan(
            daftarProduk = listOf(
                produkContoh(id = ""),
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun namaKosongDitolak() {
        validasiDaftarProdukJaringan(
            daftarProduk = listOf(
                produkContoh(
                    id = "produk-tanpa-nama",
                    nama = "",
                ),
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun identitasDuplikatDitolak() {
        validasiDaftarProdukJaringan(
            daftarProduk = listOf(
                produkContoh(id = "produk-sama"),
                produkContoh(id = "produk-sama"),
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun kodePindaiDuplikatDitolak() {
        validasiDaftarProdukJaringan(
            daftarProduk = listOf(
                produkContoh(
                    id = "produk-satu",
                    kodePindai = "kode-sama",
                ),
                produkContoh(
                    id = "produk-dua",
                    kodePindai = "kode-sama",
                ),
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun hargaNolDitolak() {
        validasiDaftarProdukJaringan(
            daftarProduk = listOf(
                produkContoh(harga = 0L),
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun stokNegatifDitolak() {
        validasiDaftarProdukJaringan(
            daftarProduk = listOf(
                produkContoh(stokTersedia = -1),
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun identitasHanyaSpasiDitolak() {
        validasiDaftarProdukJaringan(
            daftarProduk = listOf(
                produkContoh(id = "   "),
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun namaHanyaSpasiDitolak() {
        validasiDaftarProdukJaringan(
            daftarProduk = listOf(
                produkContoh(nama = "   "),
            ),
        )
    }

    @Test
    fun stokNolDiterima() {
        validasiDaftarProdukJaringan(
            daftarProduk = listOf(
                produkContoh(stokTersedia = 0),
            ),
        )
    }

    @Test
    fun kodePindaiKosongDiterima() {
        validasiDaftarProdukJaringan(
            daftarProduk = listOf(
                produkContoh(kodePindai = null),
                produkContoh(id = "produk-2", kodePindai = ""),
                produkContoh(id = "produk-3", kodePindai = "  "),
            ),
        )
    }

    private fun produkContoh(
        id: String = "produk-contoh",
        nama: String = "Produk Contoh",
        harga: Long = 10_000L,
        stokTersedia: Int = 10,
        kodePindai: String? = "kode-contoh",
    ): ResponsProdukJaringan {
        return ResponsProdukJaringan(
            id = id,
            nama = nama,
            harga = harga,
            stokTersedia = stokTersedia,
            kodePindai = kodePindai,
            deskripsi = "Produk untuk pengujian.",
            aktif = true,
        )
    }
}
