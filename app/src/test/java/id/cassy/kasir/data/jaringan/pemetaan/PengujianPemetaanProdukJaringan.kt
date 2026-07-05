package id.cassy.kasir.data.jaringan.pemetaan

import id.cassy.kasir.data.jaringan.model.ResponsProdukJaringan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pengujian unit untuk pemetaan produk jaringan ke domain.
 *
 * Test ini menjaga agar data dari server dinormalisasi sebelum dipakai oleh
 * domain dan disimpan ke database lokal.
 */
class PengujianPemetaanProdukJaringan {

    @Test
    fun membersihkanSpasiPadaIdentitasNamaKodePindaiDanDeskripsi() {
        val produk = ResponsProdukJaringan(
            id = "  produk-kopi-susu  ",
            nama = "  Kopi Susu  ",
            harga = 18_000L,
            stokTersedia = 12,
            kodePindai = "  kode-minuman-001  ",
            deskripsi = "  Kopi susu untuk katalog jaringan.  ",
            aktif = true,
        ).keDomain()

        assertEquals("produk-kopi-susu", produk.id)
        assertEquals("Kopi Susu", produk.nama)
        assertEquals("kode-minuman-001", produk.kodePindai)
        assertEquals("Kopi susu untuk katalog jaringan.", produk.deskripsi)
    }

    @Test
    fun kodePindaiKosongMenjadiNull() {
        val produk = ResponsProdukJaringan(
            id = "produk-teh",
            nama = "Teh Manis",
            harga = 5_000L,
            stokTersedia = 20,
            kodePindai = "   ",
            deskripsi = "Minuman teh.",
            aktif = true,
        ).keDomain()

        assertNull(produk.kodePindai)
    }

    @Test
    fun deskripsiNullMenjadiStringKosong() {
        val produk = ResponsProdukJaringan(
            id = "produk-air-mineral",
            nama = "Air Mineral",
            harga = 5_000L,
            stokTersedia = 50,
            kodePindai = null,
            deskripsi = null,
            aktif = true,
        ).keDomain()

        assertEquals("", produk.deskripsi)
    }
}
