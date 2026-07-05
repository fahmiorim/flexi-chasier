package id.cassy.kasir.ranah.fungsi

import id.cassy.kasir.ranah.model.AturanPajak
import id.cassy.kasir.ranah.model.ItemKeranjang
import id.cassy.kasir.ranah.model.RincianBiayaTransaksi
import id.cassy.kasir.ranah.model.Uang

/**
 * Membentuk rincian biaya transaksi dari daftar item keranjang.
 *
 * Fungsi ini adalah jalur kalkulasi domain utama untuk scope baru.
 * Semua komponen biaya memakai [Uang] agar tidak tersebar sebagai Long mentah.
 *
 * @param daftarItemKeranjang Daftar item yang sedang dibeli.
 * @param potongan Nilai diskon atau pengurang harga.
 * @param biayaLayanan Biaya tambahan layanan.
 * @param aturanPajak Aturan pajak yang diterapkan pada subtotal setelah potongan belum dikurangi.
 * @return Rincian biaya transaksi lengkap.
 */
fun hitungRincianBiayaTransaksi(
    daftarItemKeranjang: List<ItemKeranjang>,
    potongan: Uang = Uang.Nol,
    biayaLayanan: Uang = Uang.Nol,
    aturanPajak: AturanPajak = AturanPajak.TanpaPajak,
): RincianBiayaTransaksi {
    val subtotal = daftarItemKeranjang.hitungSubtotalKeranjangUang()
    val pajak = aturanPajak.hitungDariSubtotal(subtotal)

    return RincianBiayaTransaksi(
        subtotal = subtotal,
        potongan = potongan,
        biayaLayanan = biayaLayanan,
        pajak = pajak,
    )
}

/**
 * Menghitung total akhir transaksi dalam bentuk [Uang].
 *
 * @param daftarItemKeranjang Daftar item yang sedang dibeli.
 * @param potongan Nilai diskon atau pengurang harga.
 * @param biayaLayanan Biaya tambahan layanan.
 * @param aturanPajak Aturan pajak transaksi.
 * @return Total akhir transaksi.
 */
fun hitungTotalTransaksiUang(
    daftarItemKeranjang: List<ItemKeranjang>,
    potongan: Uang = Uang.Nol,
    biayaLayanan: Uang = Uang.Nol,
    aturanPajak: AturanPajak = AturanPajak.TanpaPajak,
): Uang {
    return hitungRincianBiayaTransaksi(
        daftarItemKeranjang = daftarItemKeranjang,
        potongan = potongan,
        biayaLayanan = biayaLayanan,
        aturanPajak = aturanPajak,
    ).totalAkhir
}

/**
 * Menghitung nominal akhir yang harus dibayarkan pelanggan dalam bentuk Long.
 *
 * Fungsi ini dipertahankan sebagai wrapper kompatibilitas untuk alur lama.
 *
 * @param daftarItemKeranjang List belanjaan.
 * @param potongan Nilai pengurangan harga.
 * @param biayaLayanan Nilai penambahan biaya.
 * @param pajak Nilai pajak.
 * @return Total akhir dalam Rupiah.
 */
fun hitungTotalTransaksi(
    daftarItemKeranjang: List<ItemKeranjang>,
    potongan: Long,
    biayaLayanan: Long,
    pajak: Long,
): Long {
    val rincianBiayaTransaksi = RincianBiayaTransaksi(
        subtotal = daftarItemKeranjang.hitungSubtotalKeranjangUang(),
        potongan = Uang.dariRupiah(potongan),
        biayaLayanan = Uang.dariRupiah(biayaLayanan),
        pajak = Uang.dariRupiah(pajak),
    )

    return rincianBiayaTransaksi.totalAkhir.nilaiRupiah
}

/**
 * Menghitung sisa uang yang harus dikembalikan ke pelanggan dalam bentuk [Uang].
 *
 * @param uangDibayar Nominal uang dari pelanggan.
 * @param totalTransaksi Kewajiban bayar.
 * @return Nilai kembalian.
 */
fun hitungKembalianUang(
    uangDibayar: Uang,
    totalTransaksi: Uang,
): Uang {
    return if (uangDibayar.nilaiRupiah >= totalTransaksi.nilaiRupiah) {
        uangDibayar.kurangi(totalTransaksi)
    } else {
        Uang.Nol
    }
}

/**
 * Menghitung sisa uang yang harus dikembalikan ke pelanggan dalam bentuk Long.
 *
 * Fungsi ini dipertahankan sebagai wrapper kompatibilitas untuk kode lama.
 *
 * @param uangDibayar Nominal uang dari pelanggan.
 * @param totalTransaksi Kewajiban bayar.
 * @return Nilai kembalian dalam Rupiah.
 */
fun hitungKembalian(
    uangDibayar: Long,
    totalTransaksi: Long,
): Long {
    return hitungKembalianUang(
        uangDibayar = Uang.dariRupiah(uangDibayar),
        totalTransaksi = Uang.dariRupiah(totalTransaksi),
    ).nilaiRupiah
}

/**
 * Memvalidasi apakah sebuah transaksi sudah sah secara logika untuk disimpan.
 *
 * Fungsi ini masih memakai Long agar kompatibel dengan alur lama.
 * Validasi sebenarnya sudah diarahkan ke hasil validasi checkout eksplisit.
 *
 * @return True jika keranjang tidak kosong, semua item valid, dan uang pembayaran mencukupi.
 */
fun transaksiSiapDiproses(
    daftarItemKeranjang: List<ItemKeranjang>,
    uangDibayar: Long,
    potongan: Long = 0,
    biayaLayanan: Long = 0,
    pajak: Long = 0,
): Boolean {
    if (uangDibayar < 0L || potongan < 0L || biayaLayanan < 0L || pajak < 0L) {
        return false
    }

    return validasiCheckoutDenganPajakManual(
        daftarItemKeranjang = daftarItemKeranjang,
        uangDibayar = Uang.dariRupiah(uangDibayar),
        potongan = Uang.dariRupiah(potongan),
        biayaLayanan = Uang.dariRupiah(biayaLayanan),
        pajak = Uang.dariRupiah(pajak),
    ).apakahSah
}
