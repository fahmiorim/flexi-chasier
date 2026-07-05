package id.cassy.kasir.ranah.kasuspenggunaan

import id.cassy.kasir.ranah.model.StatusTransaksi
import id.cassy.kasir.ranah.model.Uang
import id.cassy.kasir.ranah.repositori.RepositoriTransaksi

class BayarPesananPending(
    private val repositoriTransaksi: RepositoriTransaksi,
) {
    suspend fun eksekusi(
        identitasTransaksi: String,
    ) {
        val transaksi = repositoriTransaksi.ambilTransaksiBerdasarkanIdentitas(identitasTransaksi)
            ?: throw IllegalArgumentException("Pesanan tidak ditemukan.")

        val subtotal = transaksi.daftarItemKeranjang.sumOf { item ->
            item.produk.harga * item.jumlah
        }
        val total = Uang.dariRupiah(subtotal)
            .tambah(transaksi.biayaLayanan)
            .tambah(transaksi.pajak)
            .kurangi(transaksi.potongan)

        repositoriTransaksi.perbaruiStatusDanPembayaranTransaksi(
            identitasTransaksi = identitasTransaksi,
            status = StatusTransaksi.Lunas,
            uangDibayar = total,
        )
    }
}
