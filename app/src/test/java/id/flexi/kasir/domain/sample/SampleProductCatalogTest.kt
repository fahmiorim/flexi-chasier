package id.flexi.kasir.domain.sample

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pengujian kualitas katalog produk awal.
 *
 * Test ini menjaga agar seed katalog tidak membawa data yang sulit dirawat,
 * seperti identitas duplikat, kode pindai duplikat, harga kosong, stok negatif,
 * atau teks produk yang tidak lengkap.
 */
class PengujianSampleProductCatalog {

    @Test
    fun semuaProdukMemilikiIdentitasUnik() {
        val daftarProduk = SampleProductCatalog.daftarAwal()
        val daftarIdentitas = daftarProduk.map { produk -> produk.id }

        assertEquals(
            "Identitas produk seed tidak boleh duplikat.",
            daftarIdentitas.size,
            daftarIdentitas.toSet().size,
        )
    }

    @Test
    fun semuaKodePindaiYangTerisiHarusUnik() {
        val daftarKodePindai = SampleProductCatalog.daftarAwal()
            .mapNotNull { produk -> produk.kodePindai }

        assertEquals(
            "Kode pindai produk seed tidak boleh duplikat.",
            daftarKodePindai.size,
            daftarKodePindai.toSet().size,
        )
    }

    @Test
    fun semuaProdukMemilikiNilaiDasarYangSah() {
        val daftarProduk = SampleProductCatalog.daftarAwal()

        assertTrue(
            "Katalog awal minimal berisi 25 produk untuk menu Signature Coffee & Eatery.",
            daftarProduk.size >= 25,
        )

        daftarProduk.forEach { produk ->
            assertTrue(
                "Identitas produk harus diawali produk-: ${produk.id}",
                produk.id.startsWith("produk-"),
            )

            assertTrue(
                "Identitas produk harus memakai huruf kecil, angka, dan strip: ${produk.id}",
                produk.id.matches(Regex("^[a-z0-9-]+$")),
            )

            assertTrue(
                "Nama produk wajib diisi: ${produk.id}",
                produk.nama.isNotBlank(),
            )

            assertTrue(
                "Harga produk wajib lebih dari nol: ${produk.id}",
                produk.harga > 0L,
            )

            assertTrue(
                "Stok produk tidak boleh negatif: ${produk.id}",
                produk.stokTersedia >= 0,
            )

            assertTrue(
                "Deskripsi produk wajib diisi: ${produk.id}",
                produk.deskripsi.isNotBlank(),
            )
        }
    }
}
