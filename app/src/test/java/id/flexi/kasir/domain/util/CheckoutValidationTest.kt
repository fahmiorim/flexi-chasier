package id.flexi.kasir.domain.util

import id.flexi.kasir.domain.model.CheckoutValidationReason
import id.flexi.kasir.domain.model.CheckoutValidationResult
import id.flexi.kasir.domain.model.CartItem
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.model.Uang
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pengujian unit untuk validasi checkout.
 *
 * Fokusnya adalah memastikan aturan dasar Transaction tidak bisa ditembus
 * oleh data keranjang yang tidak sah.
 */
class PengujianCheckoutValidation {

    @Test
    fun keranjangKosongTidakSah() {
        val hasil = CheckoutValidation(
            daftarCartItem = emptyList(),
            uangDibayar = Uang.Nol,
        )

        assertTrue(hasil is CheckoutValidationResult.TidakSah)
        assertTrue(
            (hasil as CheckoutValidationResult.TidakSah).alasan
                is CheckoutValidationReason.KeranjangKosong,
        )
    }

    @Test
    fun jumlahItemNolTidakSah() {
        val hasil = CheckoutValidation(
            daftarCartItem = listOf(
                CartItem(
                    produk = produkContoh(),
                    jumlah = 0,
                ),
            ),
            uangDibayar = Uang.dariRupiah(10_000L),
        )

        assertTrue(hasil is CheckoutValidationResult.TidakSah)
        assertTrue(
            (hasil as CheckoutValidationResult.TidakSah).alasan
                is CheckoutValidationReason.JumlahItemTidakValid,
        )
    }

    @Test
    fun produkTidakAktifTidakSah() {
        val hasil = CheckoutValidation(
            daftarCartItem = listOf(
                CartItem(
                    produk = produkContoh(
                        aktif = false,
                    ),
                    jumlah = 1,
                ),
            ),
            uangDibayar = Uang.dariRupiah(10_000L),
        )

        assertTrue(hasil is CheckoutValidationResult.TidakSah)
        assertTrue(
            (hasil as CheckoutValidationResult.TidakSah).alasan
                is CheckoutValidationReason.ProdukTidakAktif,
        )
    }

    @Test
    fun stokTidakCukupTidakSah() {
        val hasil = CheckoutValidation(
            daftarCartItem = listOf(
                CartItem(
                    produk = produkContoh(
                        stokTersedia = 2,
                        apakahStokDiaktifkan = true,
                    ),
                    jumlah = 3,
                ),
            ),
            uangDibayar = Uang.dariRupiah(30_000L),
        )

        assertTrue(hasil is CheckoutValidationResult.TidakSah)
        assertTrue(
            (hasil as CheckoutValidationResult.TidakSah).alasan
                is CheckoutValidationReason.StokTidakCukup,
        )
    }

    @Test
    fun uangDibayarKurangTidakSah() {
        val hasil = CheckoutValidation(
            daftarCartItem = listOf(
                CartItem(
                    produk = produkContoh(
                        harga = 20_000L,
                    ),
                    jumlah = 1,
                ),
            ),
            uangDibayar = Uang.dariRupiah(10_000L),
        )

        assertTrue(hasil is CheckoutValidationResult.TidakSah)
        assertTrue(
            (hasil as CheckoutValidationResult.TidakSah).alasan
                is CheckoutValidationReason.UangDibayarKurang,
        )
    }

    @Test
    fun checkoutSahJikaKeranjangValidDanUangCukup() {
        val hasil = CheckoutValidation(
            daftarCartItem = listOf(
                CartItem(
                    produk = produkContoh(
                        harga = 20_000L,
                        stokTersedia = 5,
                    ),
                    jumlah = 2,
                ),
            ),
            uangDibayar = Uang.dariRupiah(40_000L),
        )

        assertTrue(hasil is CheckoutValidationResult.Sah)
    }

    private fun produkContoh(
        harga: Long = 10_000L,
        stokTersedia: Int = 10,
        aktif: Boolean = true,
        apakahStokDiaktifkan: Boolean = false,
    ): Produk {
        return Produk(
            id = "produk-contoh",
            nama = "Produk Contoh",
            harga = harga,
            stokTersedia = stokTersedia,
            aktif = aktif,
            apakahStokDiaktifkan = apakahStokDiaktifkan,
        )
    }
}
