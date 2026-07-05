package id.cassy.kasir.ranah.kasuspenggunaan

import id.cassy.kasir.ranah.model.ItemKeranjang
import id.cassy.kasir.ranah.model.Produk
import id.cassy.kasir.ranah.model.StatusSinkronisasi
import id.cassy.kasir.ranah.model.StatusTransaksi
import id.cassy.kasir.ranah.model.Transaksi
import id.cassy.kasir.ranah.model.Uang
import id.cassy.kasir.ranah.repositori.RepositoriTransaksi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pengujian unit untuk kasus penggunaan [SelesaikanCheckoutLokalKasir].
 */
class PengujianSelesaikanCheckoutLokalKasir {

    private val repositoriPalsu = RepositoriTransaksiPalsu()
    private val selesaikanCheckoutLokalKasir = SelesaikanCheckoutLokalKasir(repositoriPalsu)

    @Test
    fun checkoutBerhasilMenyimpanTransaksiDanMengosongkanKeranjang() = runBlocking {
        val daftarItem = listOf(
            ItemKeranjang(
                produk = Produk(
                    id = "produk-1",
                    nama = "Kopi",
                    harga = 10_000L,
                    stokTersedia = 10,
                ),
                jumlah = 2,
            )
        )

        val hasil = selesaikanCheckoutLokalKasir.eksekusi(daftarItem)

        // Verifikasi hasil keluaran kasus penggunaan.
        assertTrue(hasil.daftarItemKeranjangBaru.isEmpty())
        assertEquals(StatusSinkronisasi.SinkronLokal, hasil.statusSinkronisasiBaru)
        assertEquals(2, hasil.jumlahItemCheckout)
        assertEquals(20_000L, hasil.totalCheckout)

        // Verifikasi apakah repositori dipanggil.
        assertEquals(1, repositoriPalsu.daftarTransaksiTersimpan.size)
        val transaksiTersimpan = repositoriPalsu.daftarTransaksiTersimpan.first()
        assertEquals(Uang.dariRupiah(20_000L), transaksiTersimpan.uangDibayar)
        assertEquals(1, transaksiTersimpan.daftarItemKeranjang.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun checkoutGagalJikaKeranjangKosong() = runBlocking {
        selesaikanCheckoutLokalKasir.eksekusi(emptyList())
        Unit
    }

    @Test(expected = IllegalArgumentException::class)
    fun checkoutGagalJikaStokTidakCukup() = runBlocking {
        val daftarItem = listOf(
            ItemKeranjang(
                produk = Produk(
                    id = "produk-1",
                    nama = "Kopi",
                    harga = 10_000L,
                    stokTersedia = 1,
                ),
                jumlah = 2,
            )
        )

        selesaikanCheckoutLokalKasir.eksekusi(daftarItem)
        Unit
    }

    @Test(expected = IllegalArgumentException::class)
    fun checkoutGagalJikaProdukTidakAktif() = runBlocking {
        val daftarItem = listOf(
            ItemKeranjang(
                produk = Produk(
                    id = "produk-1",
                    nama = "Kopi",
                    harga = 10_000L,
                    stokTersedia = 10,
                    aktif = false,
                ),
                jumlah = 1,
            )
        )

        selesaikanCheckoutLokalKasir.eksekusi(daftarItem)
        Unit
    }

    /**
     * Implementasi palsu repositori untuk pengujian unit murni.
     */
    private class RepositoriTransaksiPalsu : RepositoriTransaksi {
        val daftarTransaksiTersimpan = mutableListOf<Transaksi>()

        override suspend fun simpanTransaksi(transaksi: Transaksi) {
            daftarTransaksiTersimpan.add(transaksi)
        }

        override suspend fun simpanTransaksiDanKurangiStok(transaksi: Transaksi) {
            daftarTransaksiTersimpan.add(transaksi)
        }

        override fun amatiSemuaTransaksi(): Flow<List<Transaksi>> = throw NotImplementedError()
        override fun amatiTransaksiBerdasarkanIdentitas(identitasTransaksi: String): Flow<Transaksi?> = throw NotImplementedError()
        override suspend fun ambilTransaksiBerdasarkanIdentitas(identitasTransaksi: String): Transaksi? = throw NotImplementedError()
        override fun amatiTransaksiPending(): Flow<List<Transaksi>> = throw NotImplementedError()
        override suspend fun hapusTransaksiDanKembalikanStok(identitasTransaksi: String) = throw NotImplementedError()
        override suspend fun perbaruiStatusDanPembayaranTransaksi(identitasTransaksi: String, status: StatusTransaksi, uangDibayar: Uang) = throw NotImplementedError()
    }
}
