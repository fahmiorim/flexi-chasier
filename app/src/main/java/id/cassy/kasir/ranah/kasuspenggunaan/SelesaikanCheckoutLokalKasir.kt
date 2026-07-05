package id.cassy.kasir.ranah.kasuspenggunaan

import id.cassy.kasir.ranah.identitas.PembangkitIdentitasTransaksi
import id.cassy.kasir.ranah.fungsi.hitungJumlahItem
import id.cassy.kasir.ranah.fungsi.hitungSubtotalKeranjangUang
import id.cassy.kasir.ranah.fungsi.hitungTotalTransaksiUang
import id.cassy.kasir.ranah.fungsi.sanitasiDaftarItemKeranjangUntukCheckout
import id.cassy.kasir.ranah.fungsi.validasiCheckout
import id.cassy.kasir.ranah.fungsi.validasiDaftarItemCheckout
import id.cassy.kasir.ranah.model.HasilValidasiCheckout
import id.cassy.kasir.ranah.model.ItemKeranjang
import id.cassy.kasir.ranah.model.AturanPajak
import id.cassy.kasir.ranah.model.MetodeBayar
import id.cassy.kasir.ranah.model.StatusSinkronisasi
import id.cassy.kasir.ranah.model.StatusTransaksi
import id.cassy.kasir.ranah.model.TipeOrder
import id.cassy.kasir.ranah.model.Transaksi
import id.cassy.kasir.ranah.model.Uang
import id.cassy.kasir.ranah.repositori.RepositoriTransaksi

/**
 * Hasil penyelesaian checkout lokal.
 */
data class HasilCheckoutLokalKasir(
    val daftarItemKeranjangBaru: List<ItemKeranjang>,
    val statusSinkronisasiBaru: StatusSinkronisasi,
    val jumlahItemCheckout: Int,
    val totalCheckout: Long,
)

/**
 * Kasus penggunaan untuk menyelesaikan checkout secara lokal.
 *
 * Use case ini menjadi pagar domain sebelum transaksi disimpan ke Room.
 */
class SelesaikanCheckoutLokalKasir(
    private val repositori: RepositoriTransaksi,
) {

    suspend fun eksekusi(
        daftarItemKeranjang: List<ItemKeranjang>,
        catatan: String? = null,
        status: StatusTransaksi = StatusTransaksi.Lunas,
        aturanPajak: AturanPajak = AturanPajak.TanpaPajak,
        biayaLayanan: Uang = Uang.Nol,
        potongan: Uang = Uang.Nol,
        metodeBayar: MetodeBayar = MetodeBayar.Tunai,
        tipeOrder: TipeOrder = TipeOrder.DineIn,
    ): HasilCheckoutLokalKasir {
        val daftarItemKeranjangBersih = daftarItemKeranjang
            .sanitasiDaftarItemKeranjangUntukCheckout()

        validasiDaftarItemCheckout(
            daftarItemKeranjang = daftarItemKeranjangBersih,
        ).pastikanSah()

        val totalCheckout = hitungTotalTransaksiUang(
            daftarItemKeranjang = daftarItemKeranjangBersih,
            potongan = potongan,
            biayaLayanan = biayaLayanan,
            aturanPajak = aturanPajak,
        )

        if (status == StatusTransaksi.Lunas) {
            validasiCheckout(
                daftarItemKeranjang = daftarItemKeranjangBersih,
                uangDibayar = totalCheckout,
                potongan = potongan,
                biayaLayanan = biayaLayanan,
                aturanPajak = aturanPajak,
            ).pastikanSah()
        }

        val jumlahItemCheckout = daftarItemKeranjangBersih.hitungJumlahItem()

        val rincianPajak = aturanPajak.hitungDariSubtotal(
            daftarItemKeranjangBersih.hitungSubtotalKeranjangUang(),
        )

        val transaksi = Transaksi(
            id = PembangkitIdentitasTransaksi.buatIdentitasBaru(),
            daftarItemKeranjang = daftarItemKeranjangBersih,
            uangDibayar = if (status == StatusTransaksi.Lunas) totalCheckout else Uang.Nol,
            potongan = potongan,
            biayaLayanan = biayaLayanan,
            pajak = rincianPajak,
            waktuTransaksiEpochMili = System.currentTimeMillis(),
            catatan = catatan,
            status = status,
            metodeBayar = metodeBayar,
            tipeOrder = tipeOrder,
        )

        repositori.simpanTransaksiDanKurangiStok(transaksi)

        return HasilCheckoutLokalKasir(
            daftarItemKeranjangBaru = emptyList(),
            statusSinkronisasiBaru = StatusSinkronisasi.SinkronLokal,
            jumlahItemCheckout = jumlahItemCheckout,
            totalCheckout = totalCheckout.nilaiRupiah,
        )
    }
}

/**
 * Mengubah hasil validasi tidak sah menjadi exception domain yang jelas.
 *
 * ViewModel menangkap [IllegalArgumentException] agar pesan validasi bisa
 * ditampilkan sebagai pesan singkat kepada kasir.
 */
private fun HasilValidasiCheckout.pastikanSah() {
    if (this is HasilValidasiCheckout.TidakSah) {
        throw IllegalArgumentException(pesan)
    }
}
