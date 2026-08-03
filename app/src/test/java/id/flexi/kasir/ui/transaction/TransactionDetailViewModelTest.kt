package id.flexi.kasir.ui.transaction

import androidx.lifecycle.SavedStateHandle
import id.flexi.kasir.domain.usecase.GetTableList
import id.flexi.kasir.domain.usecase.ObserveTransactionById
import id.flexi.kasir.domain.model.CartItem
import id.flexi.kasir.domain.model.Meja
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.model.TransactionStatus
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.model.Uang
import id.flexi.kasir.domain.model.TableStatus
import id.flexi.kasir.domain.repository.TableRepository
import id.flexi.kasir.domain.repository.TransactionRepository
import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PengujianTransactionDetailViewModel {

    private val pengaturUji = StandardTestDispatcher()
    private val cakupanPengujian = TestScope(pengaturUji)
    private val repositoriPalsu = TransactionRepositoryPalsu()
    private val ObserveTransactionById = ObserveTransactionById(repositoriPalsu)

    private lateinit var pengelolaTampilan: TransactionDetailViewModel

    @Before
    fun siapkan() {
        Dispatchers.setMain(pengaturUji)
    }

    @After
    fun bersihkan() {
        Dispatchers.resetMain()
    }

    private val GetTableList = GetTableList(object : TableRepository {
        override fun amatiSemuaMeja(): Flow<List<Meja>> = flowOf(emptyList())
        override suspend fun SaveTable(meja: Meja) {}
        override suspend fun DeleteTable(id: String) {}
        override suspend fun perbaruiTableStatus(id: String, tableStatus: TableStatus, TransactionId: String?) {}
    })

    private fun buatViewModel(identitasTransaction: String) {
        val savedStateHandle = SavedStateHandle(mapOf("identitasTransaction" to identitasTransaction))
        pengelolaTampilan = TransactionDetailViewModel(
            savedStateHandle = savedStateHandle,
            ObserveTransactionById = ObserveTransactionById,
            GetTableList = GetTableList,
            batalkanTransaction = id.flexi.kasir.domain.usecase.BatalkanTransaction(repositoriPalsu),
        )
    }

    @Test
    fun berhasilMemuatDetailTransaction() = cakupanPengujian.runTest {
        val identitasTransaction = "TRX-123"
        buatViewModel(identitasTransaction)

        // Mulai mengumpulkan aliran agar StateFlow aktif.
        val pekerjaanPengumpul = launch(UnconfinedTestDispatcher(testScheduler)) {
            pengelolaTampilan.modelTampilan.collect()
        }

        val Transaction = Transaction(
            id = identitasTransaction,
            daftarCartItem = listOf(
                CartItem(
                    produk = Produk(id = "P1", nama = "Kopi", harga = 10_000L, stokTersedia = 10),
                    jumlah = 2
                )
            ),
            uangDibayar = Uang.dariRupiah(20_000L),
            waktuTransactionEpochMili = System.currentTimeMillis()
        )

        repositoriPalsu.emit(Transaction)
        advanceUntilIdle()

        val statusMuat = pengelolaTampilan.modelTampilan.value.statusMuat
        assertTrue("Status seharusnya Berhasil, tapi: $statusMuat", statusMuat is StatusMuatDetailTransaction.Berhasil)
        val berhasil = statusMuat as StatusMuatDetailTransaction.Berhasil
        assertEquals(identitasTransaction, berhasil.TransactionId)
        assertEquals(1, berhasil.daftarItem.size)
        assertEquals("Kopi", berhasil.daftarItem.first().namaProduk)

        pekerjaanPengumpul.cancel()
    }

    @Test
    fun TransactionTidakDitemukan() = cakupanPengujian.runTest {
        val identitasTransaction = "TRX-404"
        buatViewModel(identitasTransaction)

        val pekerjaanPengumpul = launch(UnconfinedTestDispatcher(testScheduler)) {
            pengelolaTampilan.modelTampilan.collect()
        }

        // Mengirim nilai null untuk meniru Transaction yang tidak ditemukan.
        repositoriPalsu.emit(null)
        advanceUntilIdle()

        val statusMuat = pengelolaTampilan.modelTampilan.value.statusMuat
        assertTrue("Status seharusnya Kosong, tapi: $statusMuat", statusMuat is StatusMuatDetailTransaction.Kosong)

        pekerjaanPengumpul.cancel()
    }

    private class TransactionRepositoryPalsu : TransactionRepository {
        private val flow = MutableSharedFlow<Transaction?>(replay = 1)

        suspend fun emit(Transaction: Transaction?) {
            flow.emit(Transaction)
        }

        override fun amatiSemuaTransaction(): Flow<List<Transaction>> = throw NotImplementedError()

        override fun ObserveTransactionById(identitasTransaction: String): Flow<Transaction?> {
            return flow
        }

        override suspend fun simpanTransaction(Transaction: Transaction) = throw NotImplementedError()

        override suspend fun simpanTransactionDanKurangiStok(Transaction: Transaction) = throw NotImplementedError()

        override suspend fun simpanTransactionDenganDeltaStok(Transaction: Transaction, oldTransaction: Transaction?) = throw NotImplementedError()

        override suspend fun ambilTransactionBerdasarkanIdentitas(identitasTransaction: String): Transaction? = throw NotImplementedError()
        override fun amatiTransactionPending(): Flow<List<Transaction>> = throw NotImplementedError()
        override fun amatiTransactionDiproses(): Flow<List<Transaction>> = throw NotImplementedError()
        override suspend fun perbaruiStatusTransaction(identitasTransaction: String, status: TransactionStatus) = throw NotImplementedError()
        override suspend fun hapusTransactionDanKembalikanStok(identitasTransaction: String) = throw NotImplementedError()
        override suspend fun perbaruiStatusDanPaymentTransaction(identitasTransaction: String, status: TransactionStatus, uangDibayar: Uang, paymentMethod: id.flexi.kasir.domain.model.PaymentMethod, waktuDibayarEpochMili: Long?) = throw NotImplementedError()
        override suspend fun perbaruiStatusDanWaktuTransaction(identitasTransaction: String, status: TransactionStatus, waktuDiprosesEpochMili: Long?, waktuSelesaiEpochMili: Long?, waktuDibayarEpochMili: Long?) = throw NotImplementedError()
        override suspend fun perbaruiWaktuSelesai(identitasTransaction: String, waktuSelesaiEpochMili: Long) = throw NotImplementedError()
        override suspend fun perbaruiWaktuDibayar(identitasTransaction: String, waktuDibayarEpochMili: Long) = throw NotImplementedError()
        override suspend fun tandaiItemSelesai(identitasTransaction: String) = throw NotImplementedError()
        override suspend fun pastikanDataAwalTersedia() {}
        override suspend fun ambilNomorAntrianBerikutnya(): Int = 1
        override suspend fun ambilIdProdukTerpopuler(batasJumlah: Int): List<String> = emptyList()

        override fun amatiTransactionLunas(): Flow<List<Transaction>> = throw NotImplementedError()

        override suspend fun batalkanTransaction(identitasTransaction: String, alasan: String?) {}

        override suspend fun ambilTransactionRentang(sejak: Long, sampai: Long): List<Transaction> = emptyList()
        override fun hitungTotalTunaiSemua(): Flow<Long> = flowOf(0L)
        override fun hitungTotalQRISSemua(): Flow<Long> = flowOf(0L)
        override fun hitungTotalTunaiSejak(sejak: Long): Flow<Long> = flowOf(0L)
        override fun hitungTotalQRISSejak(sejak: Long): Flow<Long> = flowOf(0L)
        override suspend fun hitungTotalTunaiRentang(sejak: Long, sampai: Long): Long = 0L
        override suspend fun hitungTotalQRISRentang(sejak: Long, sampai: Long): Long = 0L
        override fun amatiTransactionSejak(sejak: Long): Flow<List<Transaction>> = throw NotImplementedError()
        override fun amatiTransactionPaged(sejak: Long?, sampai: Long?): Flow<PagingData<Transaction>> = throw NotImplementedError()
    }
}
