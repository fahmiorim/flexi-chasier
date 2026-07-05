package id.cassy.kasir.ranah.fungsi

import id.cassy.kasir.ranah.model.AlasanValidasiCheckout
import id.cassy.kasir.ranah.model.HasilValidasiCheckout
import id.cassy.kasir.ranah.model.ItemKeranjang
import id.cassy.kasir.ranah.model.Produk
import id.cassy.kasir.ranah.model.Uang
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pengujian unit untuk validasi checkout.
 *
 * Fokusnya adalah memastikan aturan dasar transaksi tidak bisa ditembus
 * oleh data keranjang yang tidak sah.
 */
class PengujianValidasiCheckout {

    @Test
    fun keranjangKosongTidakSah() {
        val hasil = validasiCheckout(
            daftarItemKeranjang = emptyList(),
            uangDibayar = Uang.Nol,
        )

        assertTrue(hasil is HasilValidasiCheckout.TidakSah)
        assertTrue(
            (hasil as HasilValidasiCheckout.TidakSah).alasan
                is AlasanValidasiCheckout.KeranjangKosong,
        )
    }

    @Test
    fun jumlahItemNolTidakSah() {
        val hasil = validasiCheckout(
            daftarItemKeranjang = listOf(
                ItemKeranjang(
                    produk = produkContoh(),
                    jumlah = 0,
                ),
            ),
            uangDibayar = Uang.dariRupiah(10_000L),
        )

        assertTrue(hasil is HasilValidasiCheckout.TidakSah)
        assertTrue(
            (hasil as HasilValidasiCheckout.TidakSah).alasan
                is AlasanValidasiCheckout.JumlahItemTidakValid,
        )
    }

    @Test
    fun produkTidakAktifTidakSah() {
        val hasil = validasiCheckout(
            daftarItemKeranjang = listOf(
                ItemKeranjang(
                    produk = produkContoh(
                        aktif = false,
                    ),
                    jumlah = 1,
                ),
            ),
            uangDibayar = Uang.dariRupiah(10_000L),
        )

        assertTrue(hasil is HasilValidasiCheckout.TidakSah)
        assertTrue(
            (hasil as HasilValidasiCheckout.TidakSah).alasan
                is AlasanValidasiCheckout.ProdukTidakAktif,
        )
    }

    @Test
    fun stokTidakCukupTidakSah() {
        val hasil = validasiCheckout(
            daftarItemKeranjang = listOf(
                ItemKeranjang(
                    produk = produkContoh(
                        stokTersedia = 2,
                    ),
                    jumlah = 3,
                ),
            ),
            uangDibayar = Uang.dariRupiah(30_000L),
        )

        assertTrue(hasil is HasilValidasiCheckout.TidakSah)
        assertTrue(
            (hasil as HasilValidasiCheckout.TidakSah).alasan
                is AlasanValidasiCheckout.StokTidakCukup,
        )
    }

    @Test
    fun uangDibayarKurangTidakSah() {
        val hasil = validasiCheckout(
            daftarItemKeranjang = listOf(
                ItemKeranjang(
                    produk = produkContoh(
                        harga = 20_000L,
                    ),
                    jumlah = 1,
                ),
            ),
            uangDibayar = Uang.dariRupiah(10_000L),
        )

        assertTrue(hasil is HasilValidasiCheckout.TidakSah)
        assertTrue(
            (hasil as HasilValidasiCheckout.TidakSah).alasan
                is AlasanValidasiCheckout.UangDibayarKurang,
        )
    }

    @Test
    fun checkoutSahJikaKeranjangValidDanUangCukup() {
        val hasil = validasiCheckout(
            daftarItemKeranjang = listOf(
                ItemKeranjang(
                    produk = produkContoh(
                        harga = 20_000L,
                        stokTersedia = 5,
                    ),
                    jumlah = 2,
                ),
            ),
            uangDibayar = Uang.dariRupiah(40_000L),
        )

        assertTrue(hasil is HasilValidasiCheckout.Sah)
    }

    private fun produkContoh(
        harga: Long = 10_000L,
        stokTersedia: Int = 10,
        aktif: Boolean = true,
    ): Produk {
        return Produk(
            id = "produk-contoh",
            nama = "Produk Contoh",
            harga = harga,
            stokTersedia = stokTersedia,
            aktif = aktif,
        )
    }
}
