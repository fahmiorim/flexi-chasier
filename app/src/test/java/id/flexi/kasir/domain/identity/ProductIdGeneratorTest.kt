package id.flexi.kasir.domain.identity

import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pengujian unit untuk pembangkit identitas produk.
 */
class PengujianProductIdGenerator {

    @Test
    fun identitasProdukMemakaiAwalanProdukDanSlugNama() {
        val identitasProduk = ProductIdGenerator.buatIdentitasBaru(
            namaProduk = "Kopi Susu Gula Aren",
        )

        assertTrue(
            identitasProduk.startsWith("produk-kopi-susu-gula-aren-"),
        )
    }

    @Test
    fun identitasProdukMembersihkanKarakterTidakAman() {
        val identitasProduk = ProductIdGenerator.buatIdentitasBaru(
            namaProduk = "  Es Teh!!! 330ml  ",
        )

        assertTrue(
            identitasProduk.startsWith("produk-es-teh-330ml-"),
        )
    }

    @Test
    fun identitasProdukKosongMemakaiNamaFallback() {
        val identitasProduk = ProductIdGenerator.buatIdentitasBaru(
            namaProduk = "   ",
        )

        assertTrue(
            identitasProduk.startsWith("produk-tanpa-nama-"),
        )
    }

    @Test
    fun namaProdukSamaTetapMenghasilkanIdentitasBerbeda() {
        val identitasPertama = ProductIdGenerator.buatIdentitasBaru(
            namaProduk = "Kopi Susu",
        )

        val identitasKedua = ProductIdGenerator.buatIdentitasBaru(
            namaProduk = "Kopi Susu",
        )

        assertNotEquals(identitasPertama, identitasKedua)
    }
}
