package id.flexi.kasir.domain.util

import id.flexi.kasir.domain.model.TaxRule
import id.flexi.kasir.domain.model.CartItem
import id.flexi.kasir.domain.model.TransactionCostBreakdown
import id.flexi.kasir.domain.model.Uang

/**
 * Membentuk rincian biaya Transaction dari daftar item keranjang.
 *
 * Fungsi ini adalah jalur kalkulasi domain utama untuk scope baru.
 * Semua komponen biaya memakai [Uang] agar tidak tersebar sebagai Long mentah.
 *
 * @param daftarCartItem Daftar item yang sedang dibeli.
 * @param potongan Nilai diskon atau pengurang harga.
 * @param biayaLayanan Biaya tambahan layanan.
 * @param TaxRule Aturan pajak yang diterapkan pada subtotal setelah potongan belum dikurangi.
 * @return Rincian biaya Transaction lengkap.
 */
fun hitungTransactionCostBreakdown(
    daftarCartItem: List<CartItem>,
    potongan: Uang = Uang.Nol,
    biayaLayanan: Uang = Uang.Nol,
    taxRule: TaxRule = TaxRule.NoTax,
): TransactionCostBreakdown {
    val subtotal = daftarCartItem.hitungSubtotalKeranjangUang()
    val pajak = taxRule.hitungDariSubtotal(subtotal)

    return TransactionCostBreakdown(
        subtotal = subtotal,
        potongan = potongan,
        biayaLayanan = biayaLayanan,
        pajak = pajak,
    )
}

/**
 * Menghitung total akhir Transaction dalam bentuk [Uang].
 *
 * @param daftarCartItem Daftar item yang sedang dibeli.
 * @param potongan Nilai diskon atau pengurang harga.
 * @param biayaLayanan Biaya tambahan layanan.
 * @param TaxRule Aturan pajak Transaction.
 * @return Total akhir Transaction.
 */
fun hitungTotalTransactionUang(
    daftarCartItem: List<CartItem>,
    potongan: Uang = Uang.Nol,
    biayaLayanan: Uang = Uang.Nol,
    taxRule: TaxRule = TaxRule.NoTax,
): Uang {
    return hitungTransactionCostBreakdown(
        daftarCartItem = daftarCartItem,
        potongan = potongan,
        biayaLayanan = biayaLayanan,
        taxRule = taxRule,
    ).totalAkhir
}

/**
 * Menghitung nominal akhir yang harus dibayarkan pelanggan dalam bentuk Long.
 *
 * Fungsi ini dipertahankan sebagai wrapper kompatibilitas untuk alur lama.
 *
 * @param daftarCartItem List belanjaan.
 * @param potongan Nilai pengurangan harga.
 * @param biayaLayanan Nilai penambahan biaya.
 * @param pajak Nilai pajak.
 * @return Total akhir dalam Rupiah.
 */
fun hitungTotalTransaction(
    daftarCartItem: List<CartItem>,
    potongan: Long,
    biayaLayanan: Long,
    pajak: Long,
): Long {
    val TransactionCostBreakdown = TransactionCostBreakdown(
        subtotal = daftarCartItem.hitungSubtotalKeranjangUang(),
        potongan = Uang.dariRupiah(potongan),
        biayaLayanan = Uang.dariRupiah(biayaLayanan),
        pajak = Uang.dariRupiah(pajak),
    )

    return TransactionCostBreakdown.totalAkhir.nilaiRupiah
}

/**
 * Menghitung sisa uang yang harus dikembalikan ke pelanggan dalam bentuk [Uang].
 *
 * @param uangDibayar Nominal uang dari pelanggan.
 * @param totalTransaction Kewajiban bayar.
 * @return Nilai kembalian.
 */
fun hitungKembalianUang(
    uangDibayar: Uang,
    totalTransaction: Uang,
): Uang {
    return if (uangDibayar.nilaiRupiah >= totalTransaction.nilaiRupiah) {
        uangDibayar.kurangi(totalTransaction)
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
 * @param totalTransaction Kewajiban bayar.
 * @return Nilai kembalian dalam Rupiah.
 */
fun hitungKembalian(
    uangDibayar: Long,
    totalTransaction: Long,
): Long {
    return hitungKembalianUang(
        uangDibayar = Uang.dariRupiah(uangDibayar),
        totalTransaction = Uang.dariRupiah(totalTransaction),
    ).nilaiRupiah
}

/**
 * Memvalidasi apakah sebuah Transaction sudah sah secara logika untuk disimpan.
 *
 * Fungsi ini masih memakai Long agar kompatibel dengan alur lama.
 * Validasi sebenarnya sudah diarahkan ke hasil validasi checkout eksplisit.
 *
 * @return True jika keranjang tidak kosong, semua item valid, dan uang Payment mencukupi.
 */
fun TransactionSiapDiproses(
    daftarCartItem: List<CartItem>,
    uangDibayar: Long,
    potongan: Long = 0,
    biayaLayanan: Long = 0,
    pajak: Long = 0,
): Boolean {
    if (uangDibayar < 0L || potongan < 0L || biayaLayanan < 0L || pajak < 0L) {
        return false
    }

    return CheckoutValidationDenganPajakManual(
        daftarCartItem = daftarCartItem,
        uangDibayar = Uang.dariRupiah(uangDibayar),
        potongan = Uang.dariRupiah(potongan),
        biayaLayanan = Uang.dariRupiah(biayaLayanan),
        pajak = Uang.dariRupiah(pajak),
    ).apakahSah
}
