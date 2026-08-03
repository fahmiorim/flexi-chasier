package id.flexi.kasir.ui.main

import id.flexi.kasir.domain.usecase.GetTableList
import id.flexi.kasir.domain.usecase.ObservePendingOrders
import id.flexi.kasir.domain.usecase.AmatiStorePreference
import id.flexi.kasir.domain.usecase.AmbilStoreSetting
import id.flexi.kasir.domain.usecase.PayPendingOrder
import id.flexi.kasir.domain.usecase.DeletePendingOrder
import id.flexi.kasir.domain.usecase.ResumePendingOrder
import id.flexi.kasir.domain.usecase.LoadProductCatalog
import id.flexi.kasir.domain.usecase.CompleteLocalCheckout
import id.flexi.kasir.domain.usecase.SimpanStorePreference
import id.flexi.kasir.domain.usecase.UpdatePopularFavorites
import id.flexi.kasir.domain.usecase.SeedDemoData
import id.flexi.kasir.domain.model.NetworkOperationResult
import id.flexi.kasir.domain.model.StoreSetting
import id.flexi.kasir.domain.model.StorePreference
import id.flexi.kasir.domain.model.Meja
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.model.PaymentMethod
import id.flexi.kasir.domain.model.SyncStatus
import id.flexi.kasir.domain.model.TransactionStatus
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.model.Uang
import id.flexi.kasir.domain.repository.RepositoriStoreSetting
import id.flexi.kasir.domain.repository.RepositoriStorePreference
import id.flexi.kasir.domain.repository.ProductRepository
import id.flexi.kasir.print.ThermalPrinterManager
import id.flexi.kasir.domain.repository.TableRepository
import id.flexi.kasir.domain.repository.TransactionRepository
import id.flexi.kasir.domain.repository.CashRepository
import androidx.paging.PagingData
import org.mockito.Mockito
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
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

/**
 * Pengujian unit untuk [CashierMainViewModel].
 *
 * Pengujian memakai fake repository agar perilaku layar utama bisa dikunci
 * tanpa ketergantungan ke Room, Retrofit, atau DataStore asli.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PengujianCashierMainViewModel {

    private val pengaturUji = StandardTestDispatcher()
    private val cakupanPengujian = TestScope(pengaturUji)
    private val repositoriProdukPalsu = ProductRepositoryPalsu()
    private val updatePopularFavoritesPalsu = Mockito.mock(UpdatePopularFavorites::class.java)
    private val repositoriTransactionPalsu = TransactionRepositoryPalsu()
    private val repositoriStorePreferencePalsu = RepositoriStorePreferencePalsu()
    private val LoadProductCatalog = LoadProductCatalog(repositoriProdukPalsu)
    private val repositoriMejaPalsu = TableRepositoryPalsu()
    private val CompleteLocalCheckout = CompleteLocalCheckout(repositoriTransactionPalsu, repositoriMejaPalsu)
    private val repositoriStoreSettingPalsu = RepositoriStoreSettingPalsu()
    private val amatiStorePreference = AmatiStorePreference(repositoriStorePreferencePalsu)
    private val simpanStorePreference = SimpanStorePreference(repositoriStorePreferencePalsu)
    private val ambilStoreSetting = AmbilStoreSetting(repositoriStoreSettingPalsu)
    private val simpanStoreSetting = id.flexi.kasir.domain.usecase.SimpanStoreSetting(repositoriStoreSettingPalsu)
    private val ObservePendingOrders = ObservePendingOrders(repositoriTransactionPalsu)
    private val ResumePendingOrder = ResumePendingOrder(repositoriTransactionPalsu)
    private val PayPendingOrder = PayPendingOrder(repositoriTransactionPalsu)
    private val DeletePendingOrder = DeletePendingOrder(repositoriTransactionPalsu, repositoriMejaPalsu)

    private val printerPalsu = Mockito.mock(ThermalPrinterManager::class.java)
    private val ObserveProcessingOrders = id.flexi.kasir.domain.usecase.ObserveProcessingOrders(repositoriTransactionPalsu)
    private val SelesaikanTransaction = id.flexi.kasir.domain.usecase.SelesaikanTransaction(repositoriTransactionPalsu)
    private val GetTableList = GetTableList(repositoriMejaPalsu)
    private val amatiKasAktif = Mockito.mock(id.flexi.kasir.domain.usecase.AmatiKasAktif::class.java)
    private val cashRepositoryPalsu = Mockito.mock(CashRepository::class.java).apply {
        Mockito.`when`(amatiSemuaKas()).thenReturn(
            MutableStateFlow(
                listOf(
                    id.flexi.kasir.domain.model.CashKas(
                        id = "demo-shift-ada",
                        saldoAwal = Uang(100_000),
                        waktuBuka = 0L,
                        status = id.flexi.kasir.domain.model.CashKasStatus.Tutup,
                    ),
                ),
            ),
        )
    }
    private val seedDemoData = SeedDemoData(
        cashRepository = cashRepositoryPalsu,
        transactionRepository = repositoriTransactionPalsu,
    )

    private lateinit var pengelolaTampilan: CashierMainViewModel

    @Before
    fun siapkan() {
        Dispatchers.setMain(pengaturUji)
        Mockito.`when`(amatiKasAktif()).thenReturn(MutableStateFlow(null))
        pengelolaTampilan = CashierMainViewModel(
            LoadProductCatalog = LoadProductCatalog,
            CompleteLocalCheckout = CompleteLocalCheckout,
            amatiStorePreference = amatiStorePreference,
            simpanStorePreference = simpanStorePreference,
            ambilStoreSetting = ambilStoreSetting,
            simpanStoreSetting = simpanStoreSetting,
            ObservePendingOrders = ObservePendingOrders,
            ResumePendingOrder = ResumePendingOrder,
            PayPendingOrder = PayPendingOrder,
            DeletePendingOrder = DeletePendingOrder,
            GetTableList = GetTableList,
            ThermalPrinterManager = printerPalsu,
            updatePopularFavorites = updatePopularFavoritesPalsu,
            ObserveProcessingOrders = ObserveProcessingOrders,
            SelesaikanTransaction = SelesaikanTransaction,
            amatiKasAktif = amatiKasAktif,
            seedDemoData = seedDemoData,
        )
    }

    @After
    fun bersihkan() {
        Dispatchers.resetMain()
    }

    @Test
    fun inisialisasiMemintaKatalogAwalTersedia() = cakupanPengujian.runTest {
        advanceUntilIdle()

        assertEquals(1, repositoriProdukPalsu.jumlahPermintaanKatalogAwal)
    }

    @Test
    fun tambahProdukMemasukkanItemKeKeranjang() = cakupanPengujian.runTest {
        val produk = buatProdukContoh(
            identitasProduk = "produk-kopi",
            namaProduk = "Kopi Susu",
            stokTersedia = 5,
        )
        repositoriProdukPalsu.aturDaftarProduk(listOf(produk))

        val pekerjaanPengumpul = launch(UnconfinedTestDispatcher(testScheduler)) {
            pengelolaTampilan.modelTampilan.collect()
        }

        advanceUntilIdle()

        pengelolaTampilan.tanganiAksi(
            CashierMainAction.AddProductToCart(
                produkId = produk.id,
            ),
        )
        advanceUntilIdle()

        val modelTampilan = pengelolaTampilan.modelTampilan.value
        assertEquals(1, modelTampilan.daftarCartItem.size)
        assertEquals(1, modelTampilan.daftarCartItem.first().jumlah)
        assertEquals(produk.id, modelTampilan.daftarCartItem.first().produk.id)
        assertEquals(1, modelTampilan.statusBeranda.jumlahCartItem)
        assertTrue(modelTampilan.statusBeranda.syncStatus is SyncStatus.LocalChanges)

        pekerjaanPengumpul.cancel()
    }

    @Test
    fun tambahProdukMelebihiStokMengirimPesanSingkat() = cakupanPengujian.runTest {
        val produk = buatProdukContoh(
            identitasProduk = "produk-teh",
            namaProduk = "Teh Manis",
            stokTersedia = 1,
            apakahStokDiaktifkan = true,
        )
        repositoriProdukPalsu.aturDaftarProduk(listOf(produk))

        val daftarEfek = mutableListOf<CashierMainEffect>()
        val pekerjaanPengumpulModel = launch(UnconfinedTestDispatcher(testScheduler)) {
            pengelolaTampilan.modelTampilan.collect()
        }
        val pekerjaanPengumpulEfek = launch(UnconfinedTestDispatcher(testScheduler)) {
            pengelolaTampilan.efek.collect { efek ->
                daftarEfek.add(efek)
            }
        }

        advanceUntilIdle()

        pengelolaTampilan.tanganiAksi(
            CashierMainAction.AddProductToCart(
                produkId = produk.id,
            ),
        )
        advanceUntilIdle()

        pengelolaTampilan.tanganiAksi(
            CashierMainAction.AddProductToCart(
                produkId = produk.id,
            ),
        )
        advanceUntilIdle()

        val modelTampilan = pengelolaTampilan.modelTampilan.value
        assertEquals(1, modelTampilan.daftarCartItem.size)
        assertEquals(1, modelTampilan.daftarCartItem.first().jumlah)

        val efekTerakhir = daftarEfek.last() as CashierMainEffect.TampilkanPesanSingkat
        assertEquals(
            "Stok produk sudah mencapai batas maksimum.",
            efekTerakhir.pesan,
        )

        pekerjaanPengumpulEfek.cancel()
        pekerjaanPengumpulModel.cancel()
    }

    @Test
    fun checkoutBerhasilMengosongkanKeranjang() = cakupanPengujian.runTest {
        val produk = buatProdukContoh(
            identitasProduk = "produk-roti",
            namaProduk = "Roti Bakar",
            harga = 12_000L,
            stokTersedia = 3,
        )
        repositoriProdukPalsu.aturDaftarProduk(listOf(produk))

        val pekerjaanPengumpul = launch(UnconfinedTestDispatcher(testScheduler)) {
            pengelolaTampilan.modelTampilan.collect()
        }

        advanceUntilIdle()

        pengelolaTampilan.tanganiAksi(
            CashierMainAction.AddProductToCart(
                produkId = produk.id,
            ),
        )
        advanceUntilIdle()

        pengelolaTampilan.tanganiAksi(CashierMainAction.BukaDialogCheckout(modeSimpan = false))
        advanceUntilIdle()

        pengelolaTampilan.tanganiAksi(CashierMainAction.BayarSekarang)
        advanceUntilIdle()

        val modelTampilan = pengelolaTampilan.modelTampilan.value
        assertTrue(modelTampilan.daftarCartItem.isEmpty())
        assertTrue(modelTampilan.statusHasilCheckout.apakahTampil)
        assertEquals("Pesanan Diproses", modelTampilan.statusHasilCheckout.judul)
        assertEquals(1, repositoriTransactionPalsu.daftarTransactionTersimpan.size)
        assertTrue(modelTampilan.statusBeranda.syncStatus is SyncStatus.LocalChanges)

        pekerjaanPengumpul.cancel()
    }

    @Test
    fun kurangiProdukMengurangiJumlahCartItem() = cakupanPengujian.runTest {
        val produk = buatProdukContoh(
            identitasProduk = "produk-kopi",
            namaProduk = "Kopi Susu",
            stokTersedia = 5,
        )
        repositoriProdukPalsu.aturDaftarProduk(listOf(produk))

        val pekerjaanPengumpul = launch(UnconfinedTestDispatcher(testScheduler)) {
            pengelolaTampilan.modelTampilan.collect()
        }

        advanceUntilIdle()

        repeat(2) {
            pengelolaTampilan.tanganiAksi(
                CashierMainAction.AddProductToCart(
                    produkId = produk.id,
                ),
            )
            advanceUntilIdle()
        }

        pengelolaTampilan.tanganiAksi(
            CashierMainAction.DecreaseProductInCart(
                produkId = produk.id,
            ),
        )
        advanceUntilIdle()

        val modelTampilan = pengelolaTampilan.modelTampilan.value

        assertEquals(1, modelTampilan.daftarCartItem.size)
        assertEquals(1, modelTampilan.daftarCartItem.first().jumlah)

        pekerjaanPengumpul.cancel()
    }

    @Test
    fun DeleteProductMenghapusItemDariKeranjang() = cakupanPengujian.runTest {
        val produk = buatProdukContoh(
            identitasProduk = "produk-roti",
            namaProduk = "Roti Bakar",
            stokTersedia = 5,
        )
        repositoriProdukPalsu.aturDaftarProduk(listOf(produk))

        val pekerjaanPengumpul = launch(UnconfinedTestDispatcher(testScheduler)) {
            pengelolaTampilan.modelTampilan.collect()
        }

        advanceUntilIdle()

        pengelolaTampilan.tanganiAksi(
            CashierMainAction.AddProductToCart(
                produkId = produk.id,
            ),
        )
        advanceUntilIdle()

        pengelolaTampilan.tanganiAksi(
            CashierMainAction.RemoveProductFromCart(
                produkId = produk.id,
            ),
        )
        advanceUntilIdle()

        val modelTampilan = pengelolaTampilan.modelTampilan.value

        assertTrue(modelTampilan.daftarCartItem.isEmpty())
        assertEquals(0, modelTampilan.statusBeranda.jumlahCartItem)

        pekerjaanPengumpul.cancel()
    }

    @Test
    fun resetPencarianMengosongkanKataKunci() = cakupanPengujian.runTest {
        repositoriProdukPalsu.aturDaftarProduk(
            listOf(
                buatProdukContoh(
                    identitasProduk = "produk-kopi",
                    namaProduk = "Kopi Susu",
                    stokTersedia = 5,
                ),
                buatProdukContoh(
                    identitasProduk = "produk-teh",
                    namaProduk = "Teh Manis",
                    stokTersedia = 5,
                ),
            ),
        )

        val pekerjaanPengumpul = launch(UnconfinedTestDispatcher(testScheduler)) {
            pengelolaTampilan.modelTampilan.collect()
        }

        advanceUntilIdle()

        pengelolaTampilan.tanganiAksi(
            CashierMainAction.UbahKataKunciPencarian(
                kataKunciBaru = "kopi",
            ),
        )
        advanceUntilIdle()

        pengelolaTampilan.tanganiAksi(CashierMainAction.ResetPencarian)
        advanceUntilIdle()

        val modelTampilan = pengelolaTampilan.modelTampilan.value

        assertEquals("", modelTampilan.kataKunciPencarian)

        pekerjaanPengumpul.cancel()
    }

    private fun buatProdukContoh(
        identitasProduk: String,
        namaProduk: String,
        harga: Long = 10_000L,
        stokTersedia: Int,
        apakahStokDiaktifkan: Boolean = false,
    ): Produk {
        return Produk(
            id = identitasProduk,
            nama = namaProduk,
            harga = harga,
            stokTersedia = stokTersedia,
            apakahStokDiaktifkan = apakahStokDiaktifkan,
        )
    }

    private class ProductRepositoryPalsu : ProductRepository {
        private val daftarProduk = MutableStateFlow<List<Produk>>(emptyList())
        var jumlahPermintaanKatalogAwal: Int = 0
            private set

        fun aturDaftarProduk(daftarBaru: List<Produk>) {
            daftarProduk.value = daftarBaru
        }

        override fun amatiSemuaProduk(): Flow<List<Produk>> {
            return daftarProduk
        }

        override fun ObserveProductById(identitasProduk: String): Flow<Produk?> {
            return daftarProduk.map { daftarProduk ->
                daftarProduk.firstOrNull { produk ->
                    produk.id == identitasProduk
                }
            }
        }

        override fun cariProdukLokal(kataKunci: String): Flow<List<Produk>> {
            return daftarProduk.map { daftarProduk ->
                daftarProduk.filter { produk ->
                    produk.nama.contains(kataKunci, ignoreCase = true)
                }
            }
        }

        override suspend fun pastikanKatalogAwalTersedia() {
            jumlahPermintaanKatalogAwal += 1
        }

        override suspend fun sinkronkanKatalog(): NetworkOperationResult<Unit> {
            return NetworkOperationResult.Berhasil(Unit)
        }

        override suspend fun SaveProduct(produk: Produk) {
            daftarProduk.value = daftarProduk.value
                .filterNot { produkLama ->
                    produkLama.id == produk.id
                } + produk
        }

        override suspend fun DeleteProduct(identitasProduk: String) {
            daftarProduk.value = daftarProduk.value
                .filterNot { produk ->
                    produk.id == identitasProduk
                }
        }

        override suspend fun tandaiProdukFavorit(daftarIdProduk: List<String>) {
            // No-op untuk pengujian
        }
    }

    private class TransactionRepositoryPalsu : TransactionRepository {
        val daftarTransactionTersimpan = mutableListOf<Transaction>()

        override fun amatiSemuaTransaction(): Flow<List<Transaction>> {
            return MutableStateFlow(daftarTransactionTersimpan.toList())
        }

        override suspend fun simpanTransaction(Transaction: Transaction) {
            daftarTransactionTersimpan.add(Transaction)
        }

        override suspend fun simpanTransactionDanKurangiStok(Transaction: Transaction) {
            daftarTransactionTersimpan.add(Transaction)
        }

        override suspend fun simpanTransactionDenganDeltaStok(Transaction: Transaction, oldTransaction: Transaction?) {
            daftarTransactionTersimpan.add(Transaction)
        }

        override fun ObserveTransactionById(identitasTransaction: String): Flow<Transaction?> {
            return MutableStateFlow(
                daftarTransactionTersimpan.firstOrNull { Transaction ->
                    Transaction.id == identitasTransaction
                },
            )
        }

        override suspend fun ambilTransactionBerdasarkanIdentitas(identitasTransaction: String): Transaction? {
            return daftarTransactionTersimpan.firstOrNull { Transaction ->
                Transaction.id == identitasTransaction
            }
        }

        override fun amatiTransactionPending(): Flow<List<Transaction>> {
            return MutableStateFlow(
                daftarTransactionTersimpan.filter { it.status == TransactionStatus.Pending },
            )
        }

        override fun amatiTransactionDiproses(): Flow<List<Transaction>> {
            return MutableStateFlow(
                daftarTransactionTersimpan.filter { it.status == TransactionStatus.Processing },
            )
        }

        override suspend fun perbaruiStatusTransaction(identitasTransaction: String, status: TransactionStatus) = throw NotImplementedError()

        override suspend fun hapusTransactionDanKembalikanStok(identitasTransaction: String) {
            daftarTransactionTersimpan.removeAll { it.id == identitasTransaction }
        }

        override suspend fun perbaruiStatusDanPaymentTransaction(
            identitasTransaction: String,
            status: TransactionStatus,
            uangDibayar: Uang,
            paymentMethod: PaymentMethod,
            waktuDibayarEpochMili: Long?,
        ) {
            val indeks = daftarTransactionTersimpan.indexOfFirst { it.id == identitasTransaction }
            if (indeks >= 0) {
                daftarTransactionTersimpan[indeks] = daftarTransactionTersimpan[indeks].copy(
                    status = status,
                    uangDibayar = uangDibayar,
                    paymentMethod = paymentMethod,
                )
            }
        }

        override suspend fun perbaruiStatusDanWaktuTransaction(identitasTransaction: String, status: TransactionStatus, waktuDiprosesEpochMili: Long?, waktuSelesaiEpochMili: Long?, waktuDibayarEpochMili: Long?) = throw NotImplementedError()
        override suspend fun perbaruiWaktuSelesai(identitasTransaction: String, waktuSelesaiEpochMili: Long) = throw NotImplementedError()
        override suspend fun perbaruiWaktuDibayar(identitasTransaction: String, waktuDibayarEpochMili: Long) = throw NotImplementedError()
        override suspend fun tandaiItemSelesai(identitasTransaction: String) = throw NotImplementedError()

        override suspend fun pastikanDataAwalTersedia() {}

        override suspend fun ambilNomorAntrianBerikutnya(): Int = 1

        override suspend fun ambilIdProdukTerpopuler(batasJumlah: Int): List<String> {
            return emptyList()
        }

        override fun amatiTransactionLunas(): Flow<List<Transaction>> {
            return MutableStateFlow(
                daftarTransactionTersimpan.filter { it.paymentStatus == id.flexi.kasir.domain.model.PaymentStatus.SudahDibayar },
            )
        }

        override suspend fun batalkanTransaction(identitasTransaction: String, alasan: String?) {
            daftarTransactionTersimpan.removeAll { it.id == identitasTransaction }
        }

        override suspend fun ambilTransactionRentang(sejak: Long, sampai: Long): List<Transaction> {
            return daftarTransactionTersimpan.filter {
                it.waktuTransactionEpochMili >= sejak && it.waktuTransactionEpochMili < sampai
            }
        }

        override fun hitungTotalTunaiSemua(): Flow<Long> = MutableStateFlow(0L)
        override fun hitungTotalQRISSemua(): Flow<Long> = MutableStateFlow(0L)
        override fun hitungTotalTunaiSejak(sejak: Long): Flow<Long> = MutableStateFlow(0L)
        override fun hitungTotalQRISSejak(sejak: Long): Flow<Long> = MutableStateFlow(0L)
        override suspend fun hitungTotalTunaiRentang(sejak: Long, sampai: Long): Long = 0L
        override suspend fun hitungTotalQRISRentang(sejak: Long, sampai: Long): Long = 0L
        override fun amatiTransactionSejak(sejak: Long): Flow<List<Transaction>> {
            return MutableStateFlow(
                daftarTransactionTersimpan.filter { it.waktuTransactionEpochMili >= sejak },
            )
        }

        override fun amatiTransactionPaged(sejak: Long?, sampai: Long?): Flow<PagingData<Transaction>> {
            return MutableStateFlow(PagingData.empty())
        }
    }

    private class RepositoriStoreSettingPalsu : RepositoriStoreSetting {
        private val StoreSetting = MutableStateFlow(StoreSetting(manajemenKasAktif = false))

        override fun ambilPengaturan(): Flow<StoreSetting> {
            return StoreSetting
        }

        override suspend fun simpanPengaturan(pengaturan: StoreSetting) {
            this.StoreSetting.value = pengaturan
        }
    }

    private class TableRepositoryPalsu : TableRepository {
        private val daftarMeja = MutableStateFlow<List<Meja>>(emptyList())

        override fun amatiSemuaMeja(): Flow<List<Meja>> = daftarMeja

        override suspend fun SaveTable(meja: Meja) {
            daftarMeja.value = daftarMeja.value + meja
        }

        override suspend fun DeleteTable(id: String) {
            daftarMeja.value = daftarMeja.value.filter { it.id != id }
        }

        override suspend fun perbaruiTableStatus(id: String, tableStatus: id.flexi.kasir.domain.model.TableStatus, TransactionId: String?) {
            daftarMeja.value = daftarMeja.value.map {
                if (it.id == id) it.copy(tableStatus = tableStatus, TransactionId = TransactionId) else it
            }
        }
    }

    private class RepositoriStorePreferencePalsu : RepositoriStorePreference {
        private val StorePreference = MutableStateFlow(StorePreference())

        override fun amatiStorePreference(): Flow<StorePreference> {
            return StorePreference
        }

        override suspend fun simpanStorePreference(StorePreference: StorePreference) {
            this.StorePreference.value = StorePreference
        }

        override suspend fun simpanSinkronisasiKatalogBerhasil(waktuEpochMili: Long) {
            StorePreference.value = StorePreference.value.copy(
                waktuSinkronisasiKatalogTerakhirEpochMili = waktuEpochMili,
                pesanGagalSinkronisasiKatalogTerakhir = null,
            )
        }

        override suspend fun simpanSinkronisasiKatalogGagal(pesan: String) {
            StorePreference.value = StorePreference.value.copy(
                pesanGagalSinkronisasiKatalogTerakhir = pesan,
            )
        }
    }
}
