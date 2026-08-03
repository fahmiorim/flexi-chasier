package id.flexi.kasir.domain.usecase

import id.flexi.kasir.domain.model.CartItem
import id.flexi.kasir.domain.model.CashExpenseCategory
import id.flexi.kasir.domain.model.CashKas
import id.flexi.kasir.domain.model.CashKasStatus
import id.flexi.kasir.domain.model.CashMutation
import id.flexi.kasir.domain.model.CashMutationType
import id.flexi.kasir.domain.model.PaymentMethod
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.model.TransactionStatus
import id.flexi.kasir.domain.model.Uang
import id.flexi.kasir.domain.repository.CashRepository
import id.flexi.kasir.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar

class SeedDemoData(
    private val cashRepository: CashRepository,
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke() {
        val existing = cashRepository.amatiSemuaKas().first()
        if (existing.isNotEmpty()) return

        fun produk(id: String, nama: String, harga: Long) = Produk(
            id = id, nama = nama, harga = harga, stokTersedia = 0
        )
        val p1 = produk("produk-sig-level-up-coffee", "Level Up Coffee", 20_000)
        val p2 = produk("produk-mb-sanger", "Sanger", 18_000)
        val p3 = produk("produk-snack-nugget", "Nugget", 10_000)
        val p4 = produk("produk-snack-kentang-goreng", "Kentang Goreng", 15_000)
        val p5 = produk("produk-nc-chocolate", "Chocolate", 20_000)
        val p6 = produk("produk-mb-caffe-latte", "Caffe Latte", 20_000)
        val p7 = produk("produk-am-americano", "Americano", 15_000)
        val p8 = produk("produk-mb-butterscotch", "Butterscotch", 22_000)

        // ═══════════════════════════════════
        // SHIFT 1: 2 hari lalu (18:00 - 23:45)
        // ═══════════════════════════════════
        val s1Buka = waktu(-2, 18, 0)
        val s1Tutup = waktu(-2, 23, 45)

        cashRepository.simpanKas(CashKas(
            id = "demo-shift-1",
            saldoAwal = Uang(200_000),
            saldoAkhir = Uang(410_000),
            waktuBuka = s1Buka,
            waktuTutup = s1Tutup,
            status = CashKasStatus.Tutup,
            catatanBuka = "Shift malam",
        ))

        simpanTransaksi("demo-tx-1", s1Buka + 2700_000, listOf(p1 to 2, p3 to 1), cash = 50_000)
        simpanTransaksi("demo-tx-2", s1Buka + 5400_000, listOf(p2 to 1, p4 to 2), qris = 48_000)
        simpanTransaksi("demo-tx-3", s1Buka + 9000_000, listOf(p5 to 1, p2 to 2), cash = 56_000)

        cashRepository.simpanMutasi(CashMutation(
            id = "demo-mut-1", shiftId = "demo-shift-1",
            tipe = CashMutationType.Pemasukan,
            nominal = Uang(50_000), catatan = "Isi ulang uang kecil",
            waktu = s1Buka + 3600_000,
        ))
        cashRepository.simpanMutasi(CashMutation(
            id = "demo-mut-2", shiftId = "demo-shift-1",
            tipe = CashMutationType.Pengeluaran, kategori = CashExpenseCategory.Lainnya,
            nominal = Uang(25_000), catatan = "Beli minyak goreng",
            waktu = s1Buka + 7200_000,
        ))

        // ═══════════════════════════════════
        // SHIFT 2: kemarin (18:00 - 00:30)
        // ═══════════════════════════════════
        val s2Buka = waktu(-1, 18, 0)
        val s2Tutup = waktu(0, 0, 30)

        cashRepository.simpanKas(CashKas(
            id = "demo-shift-2",
            saldoAwal = Uang(200_000),
            saldoAkhir = Uang(550_000),
            waktuBuka = s2Buka,
            waktuTutup = s2Tutup,
            status = CashKasStatus.Tutup,
        ))

        simpanTransaksi("demo-tx-4", s2Buka + 1800_000, listOf(p6 to 1, p7 to 2), cash = 50_000)
        simpanTransaksi("demo-tx-5", s2Buka + 5400_000, listOf(p8 to 2, p5 to 1), qris = 64_000)
        simpanTransaksi("demo-tx-6", s2Buka + 10800_000, listOf(p1 to 3), cash = 60_000)
        simpanTransaksi("demo-tx-7", s2Buka + 14400_000, listOf(p2 to 1, p6 to 1, p8 to 1), qris = 60_000)

        cashRepository.simpanMutasi(CashMutation(
            id = "demo-mut-3", shiftId = "demo-shift-2",
            tipe = CashMutationType.Pemasukan,
            nominal = Uang(100_000), catatan = "Setoran modal usaha",
            waktu = s2Buka + 9000_000,
        ))
    }

    private suspend fun simpanTransaksi(
        id: String, waktuTransaction: Long, items: List<Pair<Produk, Int>>,
        cash: Long? = null, qris: Long? = null,
    ) {
        val total = items.sumOf { (p, q) -> p.harga * q.toLong() }
        val method = if (qris != null) PaymentMethod.Qris else PaymentMethod.Cash
        val dibayar = cash ?: qris ?: total

        transactionRepository.simpanTransaction(
            Transaction(
                id = id,
                daftarCartItem = items.map { (p, q) -> CartItem(produk = p, jumlah = q, apakahSelesai = true) },
                uangDibayar = if (method == PaymentMethod.Cash) Uang(dibayar) else Uang(total),
                waktuTransactionEpochMili = waktuTransaction,
                status = TransactionStatus.Paid,
                paymentMethod = method,
                waktuDiprosesEpochMili = waktuTransaction,
                waktuSelesaiEpochMili = waktuTransaction,
                waktuDibayarEpochMili = waktuTransaction,
            )
        )
    }

    private fun waktu(dayOffset: Int, jam: Int, menit: Int = 0): Long {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, dayOffset)
        c.set(Calendar.HOUR_OF_DAY, jam)
        c.set(Calendar.MINUTE, menit)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }
}
