package id.cassy.kasir.ranah.kasuspenggunaan

import id.cassy.kasir.ranah.model.ItemKeranjang
import id.cassy.kasir.ranah.model.Produk
import id.cassy.kasir.ranah.model.Uang
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pengujian unit untuk use case HitungTotalBelanja.
 *
 * Fokus pengujian ini adalah memastikan kalkulasi kasir tetap benar
 * tanpa perlu Android, Room, atau Compose.
 */
class PengujianHitungTotalBelanja {

    private val hitungTotalBelanja = HitungTotalBelanja()

    @Test
    fun keranjangKosongMenghasilkanTotalNol() {
        val hasil = hitungTotalBelanja(
            daftarItemKeranjang = emptyList(),
        )

        assertTrue(hasil is HasilHitungTotalBelanja.Berhasil)

        val ringkasan = (hasil as HasilHitungTotalBelanja.Berhasil)
            .ringkasanTotalBelanja

        assertEquals(0, ringkasan.jumlahItem)
        assertEquals(Uang.Nol, ringkasan.rincianBiayaTransaksi.subtotal)
        assertEquals(Uang.Nol, ringkasan.totalAkhir)
        assertEquals(Uang.Nol, ringkasan.kembalian)
    }

    @Test
    fun menghitungSubtotalDanTotalAkhirDariKeranjangBerisiProduk() {
        val hasil = hitungTotalBelanja(
            daftarItemKeranjang = listOf(
                ItemKeranjang(
                    produk = produkContoh(
                        id = "produk-kopi",
                        nama = "Kopi Susu",
                        harga = 18_000L,
                        stokTersedia = 10,
                    ),
                    jumlah = 2,
                ),
                ItemKeranjang(
                    produk = produkContoh(
                        id = "produk-roti",
                        nama = "Roti Bakar",
                        harga = 12_000L,
                        stokTersedia = 10,
                    ),
                    jumlah = 1,
                ),
            ),
        )

        assertTrue(hasil is HasilHitungTotalBelanja.Berhasil)

        val ringkasan = (hasil as HasilHitungTotalBelanja.Berhasil)
            .ringkasanTotalBelanja

        assertEquals(3, ringkasan.jumlahItem)
        assertEquals(48_000L, ringkasan.rincianBiayaTransaksi.subtotal.nilaiRupiah)
        assertEquals(48_000L, ringkasan.totalAkhir.nilaiRupiah)
    }

    @Test
    fun menghitungTotalAkhirDenganPotonganDanBiayaLayanan() {
        val hasil = hitungTotalBelanja(
            daftarItemKeranjang = listOf(
                ItemKeranjang(
                    produk = produkContoh(
                        id = "produk-nasi",
                        nama = "Nasi Ayam",
                        harga = 25_000L,
                        stokTersedia = 10,
                    ),
                    jumlah = 2,
                ),
            ),
            potongan = Uang.dariRupiah(5_000L),
            biayaLayanan = Uang.dariRupiah(2_000L),
        )

        assertTrue(hasil is HasilHitungTotalBelanja.Berhasil)

        val ringkasan = (hasil as HasilHitungTotalBelanja.Berhasil)
            .ringkasanTotalBelanja

        assertEquals(50_000L, ringkasan.rincianBiayaTransaksi.subtotal.nilaiRupiah)
        assertEquals(47_000L, ringkasan.totalAkhir.nilaiRupiah)
    }

    private fun produkContoh(
        id: String,
        nama: String,
        harga: Long,
        stokTersedia: Int,
    ): Produk {
        return Produk(
            id = id,
            nama = nama,
            harga = harga,
            stokTersedia = stokTersedia,
            aktif = true,
        )
    }
}
