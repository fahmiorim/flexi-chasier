package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.util.hitungJumlahItem
import id.flexi.kasir.domain.util.hitungKembalianUang
import id.flexi.kasir.domain.util.hitungTransactionCostBreakdown
import id.flexi.kasir.domain.util.sanitasiDaftarCartItemUntukCheckout
import id.flexi.kasir.domain.util.validasiDaftarItemCheckout
import id.flexi.kasir.domain.model.CheckoutValidationReason
import id.flexi.kasir.domain.model.TaxRule
import id.flexi.kasir.domain.model.CheckoutValidationResult
import id.flexi.kasir.domain.model.CartItem
import id.flexi.kasir.domain.model.TransactionCostBreakdown
import id.flexi.kasir.domain.model.Uang

/**
 * Ringkasan hasil perhitungan total belanja aktif.
 *
 * Model ini dipakai oleh layer presentasi untuk menampilkan subtotal,
 * potongan, pajak, total akhir, dan kembalian tanpa membaca angka mentah
 * dari banyak fungsi terpisah.
 */
data class PurchaseTotalSummary(
    val daftarCartItemBersih: List<CartItem>,
    val jumlahItem: Int,
    val TransactionCostBreakdown: TransactionCostBreakdown,
    val totalAkhir: Uang,
    val kembalian: Uang,
)

/**
 * Hasil use case perhitungan total belanja.
 */
sealed interface HasilCalculateTotalPurchase {

    /**
     * Perhitungan berhasil dan aman dipakai untuk tampilan.
     */
    data class Berhasil(
        val PurchaseTotalSummary: PurchaseTotalSummary,
    ) : HasilCalculateTotalPurchase

    /**
     * Perhitungan gagal karena data keranjang tidak sah.
     */
    data class Gagal(
        val alasan: CheckoutValidationReason,
    ) : HasilCalculateTotalPurchase
}

/**
 * Use case untuk menghitung total belanja aktif.
 *
 * Use case ini aman untuk keranjang kosong. Keranjang kosong bukan error
 * untuk tampilan ringkasan; hasilnya adalah total nol.
 */
class CalculateTotalPurchase {

    operator fun invoke(
        daftarCartItem: List<CartItem>,
        potongan: Uang = Uang.Nol,
        biayaLayanan: Uang = Uang.Nol,
        taxRule: TaxRule = TaxRule.NoTax,
        uangDibayar: Uang = Uang.Nol,
    ): HasilCalculateTotalPurchase {
        val daftarCartItemBersih = daftarCartItem
            .sanitasiDaftarCartItemUntukCheckout()

        if (daftarCartItemBersih.isNotEmpty()) {
            val hasilValidasiDaftarItem = validasiDaftarItemCheckout(
                daftarCartItem = daftarCartItemBersih,
            )

            if (hasilValidasiDaftarItem is CheckoutValidationResult.TidakSah) {
                return HasilCalculateTotalPurchase.Gagal(
                    alasan = hasilValidasiDaftarItem.alasan,
                )
            }
        }

        val TransactionCostBreakdown = hitungTransactionCostBreakdown(
            daftarCartItem = daftarCartItemBersih,
            potongan = potongan,
            biayaLayanan = biayaLayanan,
            taxRule = taxRule,
        )

        val kembalian = hitungKembalianUang(
            uangDibayar = uangDibayar,
            totalTransaction = TransactionCostBreakdown.totalAkhir,
        )

        return HasilCalculateTotalPurchase.Berhasil(
            PurchaseTotalSummary = PurchaseTotalSummary(
                daftarCartItemBersih = daftarCartItemBersih,
                jumlahItem = daftarCartItemBersih.hitungJumlahItem(),
                TransactionCostBreakdown = TransactionCostBreakdown,
                totalAkhir = TransactionCostBreakdown.totalAkhir,
                kembalian = kembalian,
            ),
        )
    }
}
