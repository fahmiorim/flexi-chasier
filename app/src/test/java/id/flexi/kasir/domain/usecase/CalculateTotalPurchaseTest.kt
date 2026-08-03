package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.CartItem
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.model.Uang
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pengujian unit untuk use case CalculateTotalPurchase.
 *
 * Fokus pengujian ini adalah memastikan kalkulasi kasir tetap benar
 * tanpa perlu Android, Room, atau Compose.
 */
class PengujianCalculateTotalPurchase {

    private val CalculateTotalPurchase = CalculateTotalPurchase()

    @Test
    fun keranjangKosongMenghasilkanTotalNol() {
        val hasil = CalculateTotalPurchase(
            daftarCartItem = emptyList(),
        )

        assertTrue(hasil is HasilCalculateTotalPurchase.Berhasil)

        val ringkasan = (hasil as HasilCalculateTotalPurchase.Berhasil)
            .PurchaseTotalSummary

        assertEquals(0, ringkasan.jumlahItem)
        assertEquals(Uang.Nol, ringkasan.TransactionCostBreakdown.subtotal)
        assertEquals(Uang.Nol, ringkasan.totalAkhir)
        assertEquals(Uang.Nol, ringkasan.kembalian)
    }

    @Test
    fun menghitungSubtotalDanTotalAkhirDariKeranjangBerisiProduk() {
        val hasil = CalculateTotalPurchase(
            daftarCartItem = listOf(
                CartItem(
                    produk = produkContoh(
                        id = "produk-kopi",
                        nama = "Kopi Susu",
                        harga = 18_000L,
                        stokTersedia = 10,
                    ),
                    jumlah = 2,
                ),
                CartItem(
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

        assertTrue(hasil is HasilCalculateTotalPurchase.Berhasil)

        val ringkasan = (hasil as HasilCalculateTotalPurchase.Berhasil)
            .PurchaseTotalSummary

        assertEquals(3, ringkasan.jumlahItem)
        assertEquals(48_000L, ringkasan.TransactionCostBreakdown.subtotal.nilaiRupiah)
        assertEquals(48_000L, ringkasan.totalAkhir.nilaiRupiah)
    }

    @Test
    fun menghitungTotalAkhirDenganPotonganDanBiayaLayanan() {
        val hasil = CalculateTotalPurchase(
            daftarCartItem = listOf(
                CartItem(
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

        assertTrue(hasil is HasilCalculateTotalPurchase.Berhasil)

        val ringkasan = (hasil as HasilCalculateTotalPurchase.Berhasil)
            .PurchaseTotalSummary

        assertEquals(50_000L, ringkasan.TransactionCostBreakdown.subtotal.nilaiRupiah)
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
