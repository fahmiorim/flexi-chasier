package id.flexi.kasir.ui.history

import androidx.paging.PagingData
import id.flexi.kasir.domain.model.CartItem
import id.flexi.kasir.domain.model.Meja
import id.flexi.kasir.domain.model.PaymentMethod
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.model.TableStatus
import id.flexi.kasir.domain.model.TransactionStatus
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.model.Uang
import id.flexi.kasir.domain.repository.TableRepository
import id.flexi.kasir.domain.repository.TransactionRepository
import id.flexi.kasir.domain.usecase.GetTableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.Calendar

/**
 * Pengujian unit untuk [TransactionHistoryViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PengujianTransactionHistoryViewModel {

    private val pengaturUji = StandardTestDispatcher()
    private val cakupanPengujian = kotlinx.coroutines.test.TestScope(pengaturUji)
    private val repositoriPalsu = TransactionRepositoryPalsu()
    private lateinit var pengelolaTampilan: TransactionHistoryViewModel

    @Before
    fun siapkan() {
        Dispatchers.setMain(pengaturUji)
        pengelolaTampilan = TransactionHistoryViewModel(
            transactionRepository = repositoriPalsu,
            GetTableList = GetTableList(TableRepositoryPalsu()),
        )
    }

    @After
    fun bersihkan() {
        Dispatchers.resetMain()
    }

    @Test
    fun filterAwalAdalahSemua() {
        assertEquals(FilterTanggalRiwayat.Semua, pengelolaTampilan.filterTanggal.value)
    }

    @Test
    fun perbaruiFilterMengubahFilter() = cakupanPengujian.runTest {
        pengelolaTampilan.perbaruiFilterTanggal(FilterTanggalRiwayat.HariIni)
        assertEquals(FilterTanggalRiwayat.HariIni, pengelolaTampilan.filterTanggal.value)
    }

    @Test
    fun filterKustomMenyetelFilterDanLabel() = cakupanPengujian.runTest {
        val kal = Calendar.getInstance()
        kal.set(2024, Calendar.JANUARY, 1, 0, 0, 0)
        kal.set(Calendar.MILLISECOND, 0)
        val mulai = kal.timeInMillis
        kal.set(2024, Calendar.JANUARY, 3, 0, 0, 0)
        kal.set(Calendar.MILLISECOND, 0)
        val selesai = kal.timeInMillis

        val pekerjaanPengumpul = launch(UnconfinedTestDispatcher(testScheduler)) {
            pengelolaTampilan.labelRentangKustom.collect()
        }

        pengelolaTampilan.perbaruiFilterTanggalKustom(mulai, selesai)
        advanceUntilIdle()

        assertEquals(FilterTanggalRiwayat.Kustom, pengelolaTampilan.filterTanggal.value)
        assertEquals("01 Jan 2024 - 03 Jan 2024", pengelolaTampilan.labelRentangKustom.value)
        pekerjaanPengumpul.cancel()
    }

    @Test
    fun ringkasanMenyertakanNamaMejaDariIdMeja() {
        val transaksi = Transaction(
            id = "TRX-1",
            daftarCartItem = emptyList(),
            uangDibayar = Uang.dariRupiah(10_000L),
            waktuTransactionEpochMili = System.currentTimeMillis(),
            mejaId = "meja-5",
        )

        // Id meja diselesaikan menjadi nama meja (sama seperti Detail Transaksi).
        val ringkasan = transaksi.keRingkasanTransactionRiwayat(mapOf("meja-5" to "5"))
        assertEquals("Meja 5", ringkasan.labelMeja)

        // Take Away tanpa meja → tidak ada label meja.
        val takeAway = transaksi.copy(mejaId = null)
        assertNull(takeAway.keRingkasanTransactionRiwayat(mapOf("meja-5" to "5")).labelMeja)

        // Meja tidak dikenal (dihapus / belum tersinkron) → fallback null.
        val mejaHilang = transaksi.copy(mejaId = "meja-99")
        assertNull(mejaHilang.keRingkasanTransactionRiwayat(mapOf("meja-5" to "5")).labelMeja)
    }

    @Test
    fun pagingDataDapatDiKoleksiSetelahAdaTransaksi() = cakupanPengujian.runTest {
        val pekerjaanPengumpul = launch(UnconfinedTestDispatcher(testScheduler)) {
            pengelolaTampilan.pagingData.collect()
        }

        repositoriPalsu.emit(
            listOf(
                Transaction(
                    id = "TRX-1",
                    daftarCartItem = listOf(
                        CartItem(
                            produk = Produk(id = "P1", nama = "Kopi", harga = 10_000L, stokTersedia = 10),
                            jumlah = 1,
                        ),
                    ),
                    uangDibayar = Uang.dariRupiah(10_000L),
                    waktuTransactionEpochMili = System.currentTimeMillis(),
                ),
            ),
        )
        advanceUntilIdle()

        assertNotNull(pengelolaTampilan.pagingData.value)
        pekerjaanPengumpul.cancel()
    }

    private class TableRepositoryPalsu : TableRepository {
        override fun amatiSemuaMeja(): Flow<List<Meja>> = flowOf(emptyList())
        override suspend fun SaveTable(meja: Meja) {}
        override suspend fun DeleteTable(id: String) {}
        override suspend fun perbaruiTableStatus(
            id: String,
            tableStatus: TableStatus,
            TransactionId: String?,
        ) {}
    }

    private class TransactionRepositoryPalsu : TransactionRepository {
        private val flow = MutableStateFlow<List<Transaction>>(emptyList())

        fun emit(daftar: List<Transaction>) {
            flow.value = daftar
        }

        override fun amatiSemuaTransaction(): Flow<List<Transaction>> = flow
        override fun amatiTransactionLunas(): Flow<List<Transaction>> = flow

        override suspend fun simpanTransaction(Transaction: Transaction) {}
        override suspend fun simpanTransactionDanKurangiStok(Transaction: Transaction) {}
        override suspend fun simpanTransactionDenganDeltaStok(Transaction: Transaction, oldTransaction: Transaction?) {}
        override fun ObserveTransactionById(identitasTransaction: String): Flow<Transaction?> = flowOf(null)
        override suspend fun ambilTransactionBerdasarkanIdentitas(identitasTransaction: String): Transaction? = null
        override fun amatiTransactionPending(): Flow<List<Transaction>> = flowOf(emptyList())
        override fun amatiTransactionDiproses(): Flow<List<Transaction>> = flowOf(emptyList())
        override suspend fun perbaruiStatusTransaction(identitasTransaction: String, status: TransactionStatus) {}
        override suspend fun hapusTransactionDanKembalikanStok(identitasTransaction: String) {}
        override suspend fun batalkanTransaction(identitasTransaction: String, alasan: String?) {}
        override suspend fun perbaruiStatusDanPaymentTransaction(
            identitasTransaction: String,
            status: TransactionStatus,
            uangDibayar: Uang,
            paymentMethod: PaymentMethod,
            waktuDibayarEpochMili: Long?,
        ) {}
        override suspend fun perbaruiStatusDanWaktuTransaction(
            identitasTransaction: String,
            status: TransactionStatus,
            waktuDiprosesEpochMili: Long?,
            waktuSelesaiEpochMili: Long?,
            waktuDibayarEpochMili: Long?,
        ) {}
        override suspend fun perbaruiWaktuSelesai(identitasTransaction: String, waktuSelesaiEpochMili: Long) {}
        override suspend fun perbaruiWaktuDibayar(identitasTransaction: String, waktuDibayarEpochMili: Long) {}
        override suspend fun perbaruiPaymentMethodTransaction(identitasTransaction: String, paymentMethod: id.flexi.kasir.domain.model.PaymentMethod, uangDibayar: Long, catatan: String?) {}
        override suspend fun tandaiItemSelesai(identitasTransaction: String) {}
        override suspend fun ambilNomorAntrianBerikutnya(): Int = 1
        override suspend fun pastikanDataAwalTersedia() {}
        override suspend fun ambilIdProdukTerpopuler(batasJumlah: Int): List<String> = emptyList()

        override suspend fun ambilTransactionRentang(sejak: Long, sampai: Long): List<Transaction> =
            flow.value.filter {
                it.waktuTransactionEpochMili >= sejak && it.waktuTransactionEpochMili < sampai
            }

        override fun hitungTotalQRISSemua(): Flow<Long> = flowOf(0L)
        override fun hitungTotalTunaiSejak(sejak: Long): Flow<Long> = flowOf(0L)
        override fun hitungTotalQRISSejak(sejak: Long): Flow<Long> = flowOf(0L)
        override suspend fun hitungTotalTunaiRentang(sejak: Long, sampai: Long): Long = 0L
        override suspend fun hitungTotalQRISRentang(sejak: Long, sampai: Long): Long = 0L
        override fun amatiTransactionSejak(sejak: Long): Flow<List<Transaction>> = flow

        override fun amatiTransactionPaged(sejak: Long?, sampai: Long?): Flow<PagingData<Transaction>> {
            val terfilter = flow.value.filter {
                (sejak == null || it.waktuTransactionEpochMili >= sejak) &&
                    (sampai == null || it.waktuTransactionEpochMili < sampai)
            }
            return flowOf(PagingData.from(terfilter))
        }
    }
}
