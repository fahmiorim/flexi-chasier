package id.cassy.kasir.ranah.kasuspenggunaan

import id.cassy.kasir.ranah.fungsi.hitungJumlahItem
import id.cassy.kasir.ranah.fungsi.hitungKembalianUang
import id.cassy.kasir.ranah.fungsi.hitungRincianBiayaTransaksi
import id.cassy.kasir.ranah.fungsi.sanitasiDaftarItemKeranjangUntukCheckout
import id.cassy.kasir.ranah.fungsi.validasiDaftarItemCheckout
import id.cassy.kasir.ranah.model.AlasanValidasiCheckout
import id.cassy.kasir.ranah.model.AturanPajak
import id.cassy.kasir.ranah.model.HasilValidasiCheckout
import id.cassy.kasir.ranah.model.ItemKeranjang
import id.cassy.kasir.ranah.model.RincianBiayaTransaksi
import id.cassy.kasir.ranah.model.Uang

/**
 * Ringkasan hasil perhitungan total belanja aktif.
 *
 * Model ini dipakai oleh layer presentasi untuk menampilkan subtotal,
 * potongan, pajak, total akhir, dan kembalian tanpa membaca angka mentah
 * dari banyak fungsi terpisah.
 */
data class RingkasanTotalBelanja(
    val daftarItemKeranjangBersih: List<ItemKeranjang>,
    val jumlahItem: Int,
    val rincianBiayaTransaksi: RincianBiayaTransaksi,
    val totalAkhir: Uang,
    val kembalian: Uang,
)

/**
 * Hasil use case perhitungan total belanja.
 */
sealed interface HasilHitungTotalBelanja {

    /**
     * Perhitungan berhasil dan aman dipakai untuk tampilan.
     */
    data class Berhasil(
        val ringkasanTotalBelanja: RingkasanTotalBelanja,
    ) : HasilHitungTotalBelanja

    /**
     * Perhitungan gagal karena data keranjang tidak sah.
     */
    data class Gagal(
        val alasan: AlasanValidasiCheckout,
    ) : HasilHitungTotalBelanja
}

/**
 * Use case untuk menghitung total belanja aktif.
 *
 * Use case ini aman untuk keranjang kosong. Keranjang kosong bukan error
 * untuk tampilan ringkasan; hasilnya adalah total nol.
 */
class HitungTotalBelanja {

    operator fun invoke(
        daftarItemKeranjang: List<ItemKeranjang>,
        potongan: Uang = Uang.Nol,
        biayaLayanan: Uang = Uang.Nol,
        aturanPajak: AturanPajak = AturanPajak.TanpaPajak,
        uangDibayar: Uang = Uang.Nol,
    ): HasilHitungTotalBelanja {
        val daftarItemKeranjangBersih = daftarItemKeranjang
            .sanitasiDaftarItemKeranjangUntukCheckout()

        if (daftarItemKeranjangBersih.isNotEmpty()) {
            val hasilValidasiDaftarItem = validasiDaftarItemCheckout(
                daftarItemKeranjang = daftarItemKeranjangBersih,
            )

            if (hasilValidasiDaftarItem is HasilValidasiCheckout.TidakSah) {
                return HasilHitungTotalBelanja.Gagal(
                    alasan = hasilValidasiDaftarItem.alasan,
                )
            }
        }

        val rincianBiayaTransaksi = hitungRincianBiayaTransaksi(
            daftarItemKeranjang = daftarItemKeranjangBersih,
            potongan = potongan,
            biayaLayanan = biayaLayanan,
            aturanPajak = aturanPajak,
        )

        val kembalian = hitungKembalianUang(
            uangDibayar = uangDibayar,
            totalTransaksi = rincianBiayaTransaksi.totalAkhir,
        )

        return HasilHitungTotalBelanja.Berhasil(
            ringkasanTotalBelanja = RingkasanTotalBelanja(
                daftarItemKeranjangBersih = daftarItemKeranjangBersih,
                jumlahItem = daftarItemKeranjangBersih.hitungJumlahItem(),
                rincianBiayaTransaksi = rincianBiayaTransaksi,
                totalAkhir = rincianBiayaTransaksi.totalAkhir,
                kembalian = kembalian,
            ),
        )
    }
}
