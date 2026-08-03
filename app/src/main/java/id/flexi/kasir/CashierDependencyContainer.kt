package id.flexi.kasir

import android.content.Context
import androidx.room.Room
import id.flexi.kasir.data.auth.SesiStore
import id.flexi.kasir.data.auth.TokenStore
import id.flexi.kasir.data.network.config.CashierNetworkConfig
import id.flexi.kasir.data.network.config.CashierNetworkProvider
import id.flexi.kasir.data.network.interceptor.AuthInterceptor
import id.flexi.kasir.data.network.service.AuthNetworkService
import id.flexi.kasir.data.network.service.ProductNetworkService
import id.flexi.kasir.data.local.database.FlexiCashierDatabase
import id.flexi.kasir.data.local.database.CashierDatabaseMigration
import id.flexi.kasir.print.ThermalPrinterManager
import id.flexi.kasir.data.local.preference.RepositoriStoreSettingDataStore
import id.flexi.kasir.data.local.preference.RepositoriStorePreferenceDataStore
import id.flexi.kasir.data.local.repository.CashRepositoryLokal
import id.flexi.kasir.data.local.repository.TableRepositoryLokal
import id.flexi.kasir.data.local.repository.TransactionRepositoryLokal
import id.flexi.kasir.data.repository.ProductRepositoryLokalRemote
import id.flexi.kasir.data.repository.AuthRepositoryImpl
import id.flexi.kasir.domain.usecase.AmatiMutasiKas
import id.flexi.kasir.domain.usecase.AmatiSemuaKas
import id.flexi.kasir.domain.usecase.SeedDemoData
import id.flexi.kasir.domain.usecase.AmatiSetoran
import id.flexi.kasir.domain.usecase.AmatiKasAktif
import id.flexi.kasir.domain.usecase.BukaKas
import id.flexi.kasir.domain.usecase.CatatMutasiKas
import id.flexi.kasir.domain.usecase.CatatSetoran
import id.flexi.kasir.domain.usecase.GetTableList
import id.flexi.kasir.domain.usecase.HapusMutasiKas
import id.flexi.kasir.domain.usecase.HapusSetoran
import id.flexi.kasir.domain.usecase.PerbaruiSetoran
import id.flexi.kasir.domain.usecase.ObservePendingOrders
import id.flexi.kasir.domain.usecase.TutupKas
import id.flexi.kasir.domain.repository.CashRepository
import id.flexi.kasir.domain.usecase.ObserveProcessingOrders
import id.flexi.kasir.domain.usecase.SelesaikanTransaction
import id.flexi.kasir.domain.usecase.AmatiStorePreference
import id.flexi.kasir.domain.usecase.BatalkanTransaction
import id.flexi.kasir.domain.usecase.ObserveProductById
import id.flexi.kasir.domain.usecase.ObserveTransactionHistory
import id.flexi.kasir.domain.usecase.ObserveTransactionById
import id.flexi.kasir.domain.usecase.AmbilStoreSetting
import id.flexi.kasir.domain.usecase.PayPendingOrder
import id.flexi.kasir.domain.usecase.DeleteTable
import id.flexi.kasir.domain.usecase.DeletePendingOrder
import id.flexi.kasir.domain.usecase.DeleteProduct
import id.flexi.kasir.domain.usecase.ResumePendingOrder
import id.flexi.kasir.domain.usecase.LoadProductCatalog
import id.flexi.kasir.domain.usecase.CompleteLocalCheckout
import id.flexi.kasir.domain.usecase.SaveTable
import id.flexi.kasir.domain.usecase.SimpanStorePreference
import id.flexi.kasir.domain.usecase.SimpanStoreSetting
import id.flexi.kasir.domain.usecase.SaveProduct
import id.flexi.kasir.domain.usecase.UpdatePopularFavorites
import id.flexi.kasir.domain.usecase.SimpanBahan
import id.flexi.kasir.domain.usecase.HapusBahan
import id.flexi.kasir.domain.usecase.LoadBahanCatalog
import id.flexi.kasir.domain.usecase.ObserveBahanById
import id.flexi.kasir.domain.usecase.CatatPembelianBahan
import id.flexi.kasir.domain.usecase.HapusPembelianBahan
import id.flexi.kasir.domain.usecase.AmatiPembelianBahan
import id.flexi.kasir.domain.usecase.SimpanResep
import id.flexi.kasir.domain.usecase.HapusResep
import id.flexi.kasir.domain.usecase.AmatiResepByProduk
import id.flexi.kasir.domain.repository.BahanRepository
import id.flexi.kasir.data.local.repository.BahanRepositoryLokal
import id.flexi.kasir.domain.repository.TableRepository
import id.flexi.kasir.domain.repository.RepositoriStoreSetting
import id.flexi.kasir.domain.repository.RepositoriStorePreference
import id.flexi.kasir.domain.repository.ProductRepository
import id.flexi.kasir.domain.repository.TransactionRepository
import id.flexi.kasir.domain.repository.AuthRepository
import id.flexi.kasir.domain.usecase.AmatiSesi
import id.flexi.kasir.domain.usecase.KeluarAkun
import id.flexi.kasir.domain.usecase.LoginUser
import id.flexi.kasir.domain.usecase.PilihGerai
import id.flexi.kasir.domain.usecase.RegisterAkun

/**
 * Kontainer dependensi manual (Service Locator) untuk aplikasi Flexi Cashier.
 * Mengelola siklus hidup singleton basis data, repositori, kasus penggunaan, dan layanan jaringan.
 *
 * @param konteks Konteks aplikasi untuk inisialisasi Room dan sumber daya lainnya.
 */
class CashierDependencyContainer(
    konteks: Context,
) {

    /**
     * Instansi tunggal (singleton) untuk basis data Room.
     */
    private val basisData: FlexiCashierDatabase by lazy {
        Room.databaseBuilder(
            konteks.applicationContext,
            FlexiCashierDatabase::class.java,
            "kasir.db",
        )
            .addMigrations(
                CashierDatabaseMigration.DARI_1_KE_2,
                CashierDatabaseMigration.DARI_2_KE_3,
                CashierDatabaseMigration.DARI_3_KE_4,
                CashierDatabaseMigration.DARI_4_KE_5,
                CashierDatabaseMigration.DARI_5_KE_6,
                CashierDatabaseMigration.DARI_6_KE_7,
                CashierDatabaseMigration.DARI_7_KE_8,
                CashierDatabaseMigration.DARI_8_KE_9,
                CashierDatabaseMigration.DARI_9_KE_10,
                CashierDatabaseMigration.DARI_10_KE_11,
                CashierDatabaseMigration.DARI_11_KE_12,
                CashierDatabaseMigration.DARI_12_KE_13,
                CashierDatabaseMigration.DARI_13_KE_14,
                CashierDatabaseMigration.DARI_14_KE_15,
                CashierDatabaseMigration.DARI_15_KE_16,
                CashierDatabaseMigration.DARI_16_KE_17,
                CashierDatabaseMigration.DARI_17_KE_18,
                CashierDatabaseMigration.DARI_18_KE_19,
                CashierDatabaseMigration.DARI_19_KE_20,
                CashierDatabaseMigration.DARI_20_KE_21,
                CashierDatabaseMigration.DARI_21_KE_22,
            )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    /**
     * Repositori Transaction sebagai sumber data tunggal untuk Transaction lokal.
     *
     * Kontainer menyimpan implementasi konkret, sedangkan use case menerima kontrak
     * dari layer ranah.
     */
    val TransactionRepository: TransactionRepository by lazy {
        TransactionRepositoryLokal(basisData)
    }

    /**
     * Repositori produk yang menggabungkan sumber data lokal dan jaringan.
     */
    private val ProductRepository: ProductRepository by lazy {
        ProductRepositoryLokalRemote(
            basisData = basisData,
            layananJaringan = ProductNetworkService,
        )
    }

    /**
     * Repositori preferensi toko berbasis DataStore Preferences.
     */
    private val repositoriStorePreference: RepositoriStorePreference by lazy {
        RepositoriStorePreferenceDataStore(konteks.applicationContext)
    }

    private val repositoriStoreSetting: RepositoriStoreSetting by lazy {
        RepositoriStoreSettingDataStore(konteks.applicationContext)
    }

    private val TableRepository: TableRepository by lazy {
        TableRepositoryLokal(basisData)
    }

    val CashRepository: CashRepository by lazy {
        CashRepositoryLokal(basisData.LocalCashDao())
    }

    /**
     * Layanan API produk untuk komunikasi jaringan.
     *
     * Dipakai oleh repositori produk untuk sinkronisasi katalog secara local-first.
     * UI tetap membaca katalog dari Room melalui Flow.
     */
    val ProductNetworkService: ProductNetworkService by lazy {
        CashierNetworkProvider.buatProductNetworkService(
            alamatDasarApi = CashierNetworkConfig.alamatDasarApi,
            modeDebug = BuildConfig.DEBUG,
        )
    }

    /**
     * Kasus penggunaan untuk memuat katalog produk dengan skema local-first.
     */
    val LoadProductCatalog: LoadProductCatalog by lazy {
        LoadProductCatalog(ProductRepository)
    }

    /**
     * Kasus penggunaan untuk mengamati detail produk dari sumber lokal.
     */
    val ObserveProductById: ObserveProductById by lazy {
        ObserveProductById(ProductRepository)
    }

    /**
     * Kasus penggunaan untuk menyelesaikan proses checkout di tingkat lokal.
     */
    val CompleteLocalCheckout: CompleteLocalCheckout by lazy {
        CompleteLocalCheckout(TransactionRepository, TableRepository, bahanRepository, basisData)
    }

    /**
     * Kasus penggunaan untuk mengamati aliran data riwayat Transaction.
     */
    val ObserveTransactionHistory: ObserveTransactionHistory by lazy {
        ObserveTransactionHistory(TransactionRepository)
    }

    /**
     * Kasus penggunaan untuk mengamati detail Transaction tunggal berdasarkan identitas.
     */
    val ObserveTransactionById: ObserveTransactionById by lazy {
        ObserveTransactionById(TransactionRepository)
    }

    val batalkanTransaction: BatalkanTransaction by lazy {
        BatalkanTransaction(TransactionRepository)
    }

    /**
     * Kasus penggunaan untuk mengamati preferensi toko secara reaktif.
     */
    val amatiStorePreference: AmatiStorePreference by lazy {
        AmatiStorePreference(repositoriStorePreference)
    }

    /**
     * Kasus penggunaan untuk menyimpan preferensi toko.
     */
    val simpanStorePreference: SimpanStorePreference by lazy {
        SimpanStorePreference(repositoriStorePreference)
    }

    /**
     * Kasus penggunaan untuk menyimpan atau memperbarui produk.
     */
    val SaveProduct: SaveProduct by lazy {
        SaveProduct(ProductRepository)
    }

    /**
     * Kasus penggunaan untuk otomatis menandai produk paling laris sebagai favorit.
     */
    val UpdatePopularFavorites: UpdatePopularFavorites by lazy {
        UpdatePopularFavorites(TransactionRepository, ProductRepository)
    }

    /**
     * Kasus penggunaan untuk menghapus produk dari katalog.
     */
    val DeleteProduct: DeleteProduct by lazy {
        DeleteProduct(ProductRepository)
    }

    val ambilStoreSetting: AmbilStoreSetting by lazy {
        AmbilStoreSetting(repositoriStoreSetting)
    }

    val simpanStoreSetting: SimpanStoreSetting by lazy {
        SimpanStoreSetting(repositoriStoreSetting)
    }

    val ObservePendingOrders: ObservePendingOrders by lazy {
        ObservePendingOrders(TransactionRepository)
    }

    val ResumePendingOrder: ResumePendingOrder by lazy {
        ResumePendingOrder(TransactionRepository)
    }

    val PayPendingOrder: PayPendingOrder by lazy {
        PayPendingOrder(TransactionRepository)
    }

    val DeletePendingOrder: DeletePendingOrder by lazy {
        DeletePendingOrder(TransactionRepository, TableRepository)
    }

    val ObserveProcessingOrders: ObserveProcessingOrders by lazy {
        ObserveProcessingOrders(TransactionRepository)
    }

    val SelesaikanTransaction: SelesaikanTransaction by lazy {
        SelesaikanTransaction(TransactionRepository)
    }

    val GetTableList: GetTableList by lazy {
        GetTableList(TableRepository)
    }

    val SaveTable: SaveTable by lazy {
        SaveTable(TableRepository)
    }

    val DeleteTable: DeleteTable by lazy {
        DeleteTable(TableRepository)
    }

    val ThermalPrinterManager: ThermalPrinterManager by lazy {
        ThermalPrinterManager(konteks.applicationContext)
    }

    // ── Kas ──

    val bukaKas: BukaKas by lazy {
        BukaKas(CashRepository)
    }

    val tutupKas: TutupKas by lazy {
        TutupKas(CashRepository)
    }

    val amatiKasAktif: AmatiKasAktif by lazy {
        AmatiKasAktif(CashRepository)
    }

    val catatMutasiKas: CatatMutasiKas by lazy {
        CatatMutasiKas(CashRepository)
    }

    val amatiMutasiKas: AmatiMutasiKas by lazy {
        AmatiMutasiKas(CashRepository)
    }

    val hapusMutasiKas: HapusMutasiKas by lazy {
        HapusMutasiKas(CashRepository)
    }

    val amatiSemuaKas: AmatiSemuaKas by lazy {
        AmatiSemuaKas(CashRepository)
    }

    val seedDemoData: SeedDemoData by lazy {
        SeedDemoData(CashRepository, TransactionRepository)
    }

    // ── Setoran ──

    val catatSetoran: CatatSetoran by lazy {
        CatatSetoran(CashRepository)
    }

    val amatiSetoran: AmatiSetoran by lazy {
        AmatiSetoran(CashRepository)
    }

    val hapusSetoran: HapusSetoran by lazy {
        HapusSetoran(CashRepository)
    }

    val perbaruiSetoran: PerbaruiSetoran by lazy {
        PerbaruiSetoran(CashRepository)
    }

    // ── Bahan Baku ──

    val bahanRepository: BahanRepository by lazy {
        BahanRepositoryLokal(basisData.BahanDao())
    }

    val SimpanBahan: SimpanBahan by lazy {
        SimpanBahan(bahanRepository)
    }

    val HapusBahan: HapusBahan by lazy {
        HapusBahan(bahanRepository)
    }

    val LoadBahanCatalog: LoadBahanCatalog by lazy {
        LoadBahanCatalog(bahanRepository)
    }

    val ObserveBahanById: ObserveBahanById by lazy {
        ObserveBahanById(bahanRepository)
    }

    val CatatPembelianBahan: CatatPembelianBahan by lazy {
        CatatPembelianBahan(bahanRepository)
    }

    val HapusPembelianBahan: HapusPembelianBahan by lazy {
        HapusPembelianBahan(bahanRepository)
    }

    val AmatiPembelianBahan: AmatiPembelianBahan by lazy {
        AmatiPembelianBahan(bahanRepository)
    }

    val SimpanResep: SimpanResep by lazy {
        SimpanResep(bahanRepository)
    }

    val HapusResep: HapusResep by lazy {
        HapusResep(bahanRepository)
    }

    val AmatiResepByProduk: AmatiResepByProduk by lazy {
        AmatiResepByProduk(bahanRepository)
    }

    // ── Autentikasi (SaaS multi-tenant) ──

    /**
     * Penyimpanan token JWT terenkripsi (Keystore).
     */
    val TokenStore: TokenStore by lazy {
        TokenStore(konteks.applicationContext)
    }

    /**
     * Penyimpanan sesi login (data akun + daftar gerai) di DataStore.
     */
    val SesiStore: SesiStore by lazy {
        SesiStore(konteks.applicationContext)
    }

    /**
     * Layanan auth TANPA interceptor — dipakai untuk login/register dan penukaran
     * refresh token di AuthInterceptor.
     */
    val AuthNetworkService: AuthNetworkService by lazy {
        CashierNetworkProvider.buatAuthNetworkService(
            alamatDasarApi = CashierNetworkConfig.alamatDasarApi,
            modeDebug = BuildConfig.DEBUG,
        )
    }

    /**
     * Interceptor yang menyisipkan Bearer token dan menukar refresh token saat 401.
     */
    val AuthInterceptor: AuthInterceptor by lazy {
        AuthInterceptor(
            tokenStore = TokenStore,
            layananAuth = AuthNetworkService,
        )
    }

    /**
     * Klien HTTP terotentikasi. Endpoint terproteksi di phase berikutnya
     * (sync, laporan, pengaturan) memakai klien ini.
     */
    val KlienHttpOtentikasi: okhttp3.OkHttpClient by lazy {
        CashierNetworkProvider.buatKlienHttpOtentikasi(
            alamatDasarApi = CashierNetworkConfig.alamatDasarApi,
            modeDebug = BuildConfig.DEBUG,
            authInterceptor = AuthInterceptor,
        )
    }

    /**
     * Repositori autentikasi: jaringan + token terenkripsi + sesi DataStore.
     */
    val AuthRepository: AuthRepository by lazy {
        AuthRepositoryImpl(
            layananJaringan = AuthNetworkService,
            tokenStore = TokenStore,
            sesiStore = SesiStore,
        )
    }

    val loginUser: LoginUser by lazy {
        LoginUser(AuthRepository)
    }

    val registerAkun: RegisterAkun by lazy {
        RegisterAkun(AuthRepository)
    }

    val pilihGerai: PilihGerai by lazy {
        PilihGerai(AuthRepository)
    }

    val keluarAkun: KeluarAkun by lazy {
        KeluarAkun(AuthRepository)
    }

    val amatiSesi: AmatiSesi by lazy {
        AmatiSesi(AuthRepository)
    }
}
