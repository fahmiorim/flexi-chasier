package id.cassy.kasir.ranah.identitas

import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pengujian unit untuk pembangkit identitas produk.
 */
class PengujianPembangkitIdentitasProduk {

    @Test
    fun identitasProdukMemakaiAwalanProdukDanSlugNama() {
        val identitasProduk = PembangkitIdentitasProduk.buatIdentitasBaru(
            namaProduk = "Kopi Susu Gula Aren",
        )

        assertTrue(
            identitasProduk.startsWith("produk-kopi-susu-gula-aren-"),
        )
    }

    @Test
    fun identitasProdukMembersihkanKarakterTidakAman() {
        val identitasProduk = PembangkitIdentitasProduk.buatIdentitasBaru(
            namaProduk = "  Es Teh!!! 330ml  ",
        )

        assertTrue(
            identitasProduk.startsWith("produk-es-teh-330ml-"),
        )
    }

    @Test
    fun identitasProdukKosongMemakaiNamaFallback() {
        val identitasProduk = PembangkitIdentitasProduk.buatIdentitasBaru(
            namaProduk = "   ",
        )

        assertTrue(
            identitasProduk.startsWith("produk-tanpa-nama-"),
        )
    }

    @Test
    fun namaProdukSamaTetapMenghasilkanIdentitasBerbeda() {
        val identitasPertama = PembangkitIdentitasProduk.buatIdentitasBaru(
            namaProduk = "Kopi Susu",
        )

        val identitasKedua = PembangkitIdentitasProduk.buatIdentitasBaru(
            namaProduk = "Kopi Susu",
        )

        assertNotEquals(identitasPertama, identitasKedua)
    }
}
