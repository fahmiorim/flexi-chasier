package id.cassy.kasir.ranah.contoh

import id.cassy.kasir.ranah.model.ItemKeranjang
import id.cassy.kasir.ranah.model.Transaksi
import id.cassy.kasir.ranah.model.Uang

/**
 * Data contoh sementara untuk alur riwayat transaksi.
 *
 * Dipakai hanya untuk membangun fondasi route dan layar detail transaksi
 * sebelum penyimpanan lokal berbasis Room masuk ke proyek.
 */
object RiwayatTransaksiContoh {

    /**
     * Menyediakan daftar awal transaksi contoh untuk keperluan pengembangan UI.
     *
     * @return List transaksi statis.
     */
    fun daftarAwal(): List<Transaksi> {
        val daftarProduk = KatalogProdukContoh.daftarAwal()

        return listOf(
            Transaksi(
                id = "TRX-20260418-001",
                daftarItemKeranjang = listOf(
                    ItemKeranjang(
                        produk = daftarProduk[0],
                        jumlah = 1,
                    ),
                    ItemKeranjang(
                        produk = daftarProduk[1],
                        jumlah = 1,
                    ),
                ),
                uangDibayar = Uang.dariRupiah(50000),
                waktuTransaksiEpochMili = 1744936200000,
                catatan = "Pembayaran tunai pelanggan pagi.",
            ),
            Transaksi(
                id = "TRX-20260418-002",
                daftarItemKeranjang = listOf(
                    ItemKeranjang(
                        produk = daftarProduk[2],
                        jumlah = 2,
                    ),
                    ItemKeranjang(
                        produk = daftarProduk[3],
                        jumlah = 1,
                    ),
                ),
                uangDibayar = Uang.dariRupiah(70000),
                waktuTransaksiEpochMili = 1744939800000,
                catatan = "Pesanan makan siang tanpa tambahan catatan item.",
            ),
        )
    }

    /**
     * Mencari transaksi berdasarkan identitas dari data contoh.
     *
     * @param identitasTransaksi Identitas transaksi yang dicari.
     * @return Objek [Transaksi] jika ditemukan, null jika tidak.
     */
    fun temukanBerdasarkanIdentitas(
        identitasTransaksi: String,
    ): Transaksi? {
        return daftarAwal().firstOrNull { transaksi ->
            transaksi.id == identitasTransaksi
        }
    }
}
