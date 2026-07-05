package id.cassy.kasir.ranah.model

/**
 * Rincian biaya dalam satu transaksi kasir.
 *
 * Model ini mengelompokkan komponen biaya agar kalkulasi transaksi tidak lagi
 * tersebar sebagai angka mentah tanpa konteks.
 *
 * @property subtotal Nilai barang sebelum potongan, pajak, dan biaya layanan.
 * @property potongan Nilai pengurang harga.
 * @property biayaLayanan Nilai biaya tambahan layanan.
 * @property pajak Nilai pajak transaksi.
 */
data class RincianBiayaTransaksi(
    val subtotal: Uang,
    val potongan: Uang = Uang.Nol,
    val biayaLayanan: Uang = Uang.Nol,
    val pajak: Uang = Uang.Nol,
) {
    /**
     * Total akhir yang harus dibayar pelanggan.
     *
     * Rumus:
     * subtotal - potongan + biaya layanan + pajak.
     */
    val totalAkhir: Uang
        get() {
            val setelahPotong = if (potongan.nilaiRupiah > subtotal.nilaiRupiah) {
                Uang.Nol
            } else {
                subtotal.kurangi(potongan)
            }
            return setelahPotong
                .tambah(biayaLayanan)
                .tambah(pajak)
        }
}
