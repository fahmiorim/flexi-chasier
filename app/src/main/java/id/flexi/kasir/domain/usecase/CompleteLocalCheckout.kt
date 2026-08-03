package id.flexi.kasir.domain.usecase

import androidx.room.RoomDatabase
import id.flexi.kasir.domain.identity.TransactionIdGenerator
import id.flexi.kasir.domain.util.hitungJumlahItem
import id.flexi.kasir.domain.util.hitungSubtotalKeranjangUang
import id.flexi.kasir.domain.util.hitungTotalTransactionUang
import id.flexi.kasir.domain.util.sanitasiDaftarCartItemUntukCheckout
import id.flexi.kasir.domain.util.CheckoutValidation
import id.flexi.kasir.domain.util.validasiDaftarItemCheckout
import id.flexi.kasir.domain.model.CheckoutValidationResult
import id.flexi.kasir.domain.model.CartItem
import id.flexi.kasir.domain.model.TaxRule
import id.flexi.kasir.domain.model.PaymentMethod
import id.flexi.kasir.domain.model.TableStatus
import id.flexi.kasir.domain.model.SyncStatus
import id.flexi.kasir.domain.model.TransactionStatus
import id.flexi.kasir.domain.model.OrderType
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.model.Uang
import id.flexi.kasir.domain.repository.BahanRepository
import id.flexi.kasir.domain.repository.TableRepository
import id.flexi.kasir.domain.repository.TransactionRepository

data class LocalCheckoutResult(
    val daftarCartItemBaru: List<CartItem>,
    val SyncStatusBaru: SyncStatus,
    val jumlahItemCheckout: Int,
    val totalCheckout: Long,
    val nomorAntrian: Int? = null,
)

class CompleteLocalCheckout(
    private val transactionRepository: TransactionRepository,
    private val tableRepository: TableRepository,
    private val BahanRepository: BahanRepository? = null,
    private val basisData: RoomDatabase? = null,
) {

    suspend fun eksekusi(
        daftarCartItem: List<CartItem>,
        catatan: String? = null,
        status: TransactionStatus = TransactionStatus.Paid,
        taxRule: TaxRule = id.flexi.kasir.domain.model.TaxRule.NoTax,
        biayaLayanan: Uang = Uang.Nol,
        potongan: Uang = Uang.Nol,
        paymentMethod: PaymentMethod = PaymentMethod.Cash,
        orderType: OrderType = OrderType.DineIn,
        mejaId: String? = null,
        uangDibayar: Long? = null,
        identitasTransaction: String? = null,
    ): LocalCheckoutResult {
        val daftarCartItemBersih = daftarCartItem
            .sanitasiDaftarCartItemUntukCheckout()

        validasiDaftarItemCheckout(
            daftarCartItem = daftarCartItemBersih,
        ).pastikanSah()

        val totalCheckout = hitungTotalTransactionUang(
            daftarCartItem = daftarCartItemBersih,
            potongan = potongan,
            biayaLayanan = biayaLayanan,
            taxRule = taxRule,
        )

        val sudahDibayar = status == TransactionStatus.Paid || status == TransactionStatus.Processing

        // Validasi nominal bayar: pakai nominal asli dari user (uangDibayar) jika ada
        val nominalValidasi = if (uangDibayar != null) {
            Uang.dariRupiah(uangDibayar)
        } else if (sudahDibayar) {
            totalCheckout
        } else {
            Uang.Nol
        }

        if (sudahDibayar) {
            CheckoutValidation(
                daftarCartItem = daftarCartItemBersih,
                uangDibayar = nominalValidasi,
                potongan = potongan,
                biayaLayanan = biayaLayanan,
                taxRule = taxRule,
            ).pastikanSah()
        }

        val jumlahItemCheckout = daftarCartItemBersih.hitungJumlahItem()

        val rincianPajak = taxRule.hitungDariSubtotal(
            daftarCartItemBersih.hitungSubtotalKeranjangUang(),
        )

        val nomorAntrian = if (sudahDibayar && orderType == OrderType.DineIn) {
            transactionRepository.ambilNomorAntrianBerikutnya()
        } else {
            null
        }

        val nominalBayar = uangDibayar ?: if (sudahDibayar) totalCheckout.nilaiRupiah else 0L

        val waktuSekarang = System.currentTimeMillis()

        // Kalau ada identitasTransaction dari resume, ambil data lama untuk
        // pertahankan timestamp asli (urutan antrian) + delta stock
        val oldTransaction = if (identitasTransaction != null) {
            transactionRepository.ambilTransactionBerdasarkanIdentitas(identitasTransaction)
        } else {
            null
        }
        val waktuTransaction = oldTransaction?.waktuTransactionEpochMili ?: waktuSekarang

        val Transaction = Transaction(
            id = identitasTransaction ?: TransactionIdGenerator.buatIdentitasBaru(),
            daftarCartItem = daftarCartItemBersih,
            uangDibayar = Uang.dariRupiah(nominalBayar),
            potongan = potongan,
            biayaLayanan = biayaLayanan,
            pajak = rincianPajak,
            waktuTransactionEpochMili = waktuTransaction,
            catatan = catatan,
            status = status,
            paymentMethod = paymentMethod,
            orderType = orderType,
            nomorAntrian = nomorAntrian,
            mejaId = mejaId,
            waktuDiprosesEpochMili = if (status == TransactionStatus.Processing) waktuSekarang else null,
            waktuDibayarEpochMili = if (status == TransactionStatus.Paid) waktuSekarang else null,
        )

        // Bungkus dalam transaksi database agar atomic: jika salah satu gagal, semua di-rollback
        val aksiDatabase: suspend () -> Unit = {
            transactionRepository.simpanTransactionDenganDeltaStok(
                Transaction = Transaction,
                oldTransaction = oldTransaction,
            )

            // Kurangi stok bahan baku berdasarkan resep produk yang dibayar
            if (BahanRepository != null && sudahDibayar) {
                kurangiStokBahanDariCheckout(daftarCartItemBersih)
            }
        }

        if (basisData != null) {
            basisData.runInTransaction {
                kotlinx.coroutines.runBlocking {
                    aksiDatabase()
                }
            }
        } else {
            aksiDatabase()
        }

        if (mejaId != null) {
            val sudahLunas = status == TransactionStatus.Processing || status == TransactionStatus.Paid
            val statusMeja = if (sudahLunas) {
                TableStatus.Available
            } else {
                TableStatus.Occupied
            }
            tableRepository.perbaruiTableStatus(
                id = mejaId,
                tableStatus = statusMeja,
                TransactionId = if (sudahLunas) null else Transaction.id,
            )
        }

        return LocalCheckoutResult(
            daftarCartItemBaru = emptyList(),
            SyncStatusBaru = SyncStatus.LocalChanges,
            jumlahItemCheckout = jumlahItemCheckout,
            totalCheckout = totalCheckout.nilaiRupiah,
            nomorAntrian = nomorAntrian,
        )
    }

    private suspend fun kurangiStokBahanDariCheckout(daftarCartItem: List<CartItem>) {
        for (item in daftarCartItem) {
            if (item.jumlah <= 0) continue
            val resep = BahanRepository?.ambilResepByProdukId(item.produk.id) ?: continue
            for (bahan in resep.daftarBahan) {
                if (bahan.jumlah <= 0) continue
                val totalDipakai = bahan.jumlah * item.jumlah
                BahanRepository.perbaruiStokBahan(bahan.bahanId, -totalDipakai)
            }
        }
    }
}

private fun CheckoutValidationResult.pastikanSah() {
    if (this is CheckoutValidationResult.TidakSah) {
        throw IllegalArgumentException(pesan)
    }
}
