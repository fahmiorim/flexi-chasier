package id.cassy.kasir.ranah.contoh

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
class PengujianKatalogProdukContoh {

    @Test
    fun semuaProdukMemilikiIdentitasUnik() {
        val daftarProduk = KatalogProdukContoh.daftarAwal()
        val daftarIdentitas = daftarProduk.map { produk -> produk.id }

        assertEquals(
            "Identitas produk seed tidak boleh duplikat.",
            daftarIdentitas.size,
            daftarIdentitas.toSet().size,
        )
    }

    @Test
    fun semuaKodePindaiYangTerisiHarusUnik() {
        val daftarKodePindai = KatalogProdukContoh.daftarAwal()
            .mapNotNull { produk -> produk.kodePindai }

        assertEquals(
            "Kode pindai produk seed tidak boleh duplikat.",
            daftarKodePindai.size,
            daftarKodePindai.toSet().size,
        )
    }

    @Test
    fun semuaProdukMemilikiNilaiDasarYangSah() {
        val daftarProduk = KatalogProdukContoh.daftarAwal()

        assertTrue(
            "Katalog awal minimal berisi 40 produk agar cukup untuk demo POS.",
            daftarProduk.size >= 40,
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
