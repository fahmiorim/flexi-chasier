package id.flexi.kasir.ui.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.flexi.kasir.CashierApp
import id.flexi.kasir.domain.model.PeranAkun
import id.flexi.kasir.domain.model.StoreSetting
import id.flexi.kasir.ui.CashierViewModelProvider
import id.flexi.kasir.ui.auth.AuthViewModel
import id.flexi.kasir.ui.auth.LoginScreen
import id.flexi.kasir.ui.auth.PilihGeraiScreen
import id.flexi.kasir.ui.dashboard.DashboardScreen
import id.flexi.kasir.ui.dashboard.DashboardViewModel
import id.flexi.kasir.ui.detail.ProductDetailEffect
import id.flexi.kasir.ui.detail.ProductDetailScreen
import id.flexi.kasir.ui.detail.ProductDetailViewModel
import id.flexi.kasir.ui.manage.ProductFormScreen
import id.flexi.kasir.ui.manage.ManageProductsScreen
import id.flexi.kasir.ui.manage.ProductFormViewModel
import id.flexi.kasir.ui.manage.ManageProductsViewModel
import id.flexi.kasir.ui.manage.ProductManagementMenuScreen
import id.flexi.kasir.ui.component.SidebarKasir
import id.flexi.kasir.ui.bahan.BahanManagementScreen
import id.flexi.kasir.ui.bahan.BahanFormScreen
import id.flexi.kasir.ui.bahan.BahanDetailScreen
import id.flexi.kasir.ui.resep.ResepScreen
import id.flexi.kasir.ui.table.SettingsScreenMeja
import id.flexi.kasir.ui.table.SettingsScreenMejaViewModel
import id.flexi.kasir.ui.cashregister.CashRegisterScreen
import id.flexi.kasir.ui.cashregister.CashRegisterViewModel
import id.flexi.kasir.ui.report.SalesReportScreen
import id.flexi.kasir.ui.report.SalesReportViewModel
import id.flexi.kasir.ui.settings.SettingsScreen
import id.flexi.kasir.ui.settings.SettingsViewModel
import id.flexi.kasir.ui.history.TransactionHistoryScreen
import id.flexi.kasir.ui.history.TransactionHistoryViewModel
import id.flexi.kasir.ui.transaction.TransactionDetailScreen
import id.flexi.kasir.ui.transaction.TransactionDetailViewModel
import id.flexi.kasir.ui.main.CashierMainAction
import id.flexi.kasir.ui.main.CashierMainScreen
import id.flexi.kasir.ui.main.CashierMainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

@Composable
fun NavigasiFlexiKasirApp() {
    val context = LocalContext.current
    val aplikasi = context.applicationContext as CashierApp
    val cakupanKorutin = rememberCoroutineScope()
    val sesi by aplikasi.kontainer.amatiSesi().collectAsState(initial = null)

    val akun = sesi
    when {
        akun == null -> {
            val viewModel: AuthViewModel = viewModel(
                factory = CashierViewModelProvider.Factory,
            )
            LoginScreen(viewModel = viewModel)
        }

        akun.geraiAktif == null && akun.daftarGerai.size > 1 -> {
            PilihGeraiScreen(
                akun = akun,
                onPilihGerai = { geraiId ->
                    cakupanKorutin.launch {
                        aplikasi.kontainer.pilihGerai(geraiId)
                    }
                },
                onKeluar = {
                    cakupanKorutin.launch {
                        aplikasi.kontainer.keluarAkun()
                    }
                },
            )
        }

        else -> {
            KasirAppUtama(
                aplikasi = aplikasi,
                peran = akun.peran,
                namaUser = akun.nama,
            )
        }
    }
}

@Composable
private fun KasirAppUtama(
    aplikasi: CashierApp,
    peran: PeranAkun,
    namaUser: String,
) {
    val context = LocalContext.current
    val pengendaliNavigasi = rememberNavController()
    val statusDrawer = rememberDrawerState(initialValue = DrawerValue.Closed)
    val cakupanKorutin = rememberCoroutineScope()

    val masukanBackStack by pengendaliNavigasi.currentBackStackEntryAsState()
    val ruteString = masukanBackStack?.destination?.route
    val ruteSaatIni: CashierNavigationDestination? = when (ruteString) {
        CashierNavigationDestination.Dashboard::class.qualifiedName -> CashierNavigationDestination.Dashboard
        CashierNavigationDestination.KasirUtama::class.qualifiedName -> CashierNavigationDestination.KasirUtama
        CashierNavigationDestination.RiwayatTransaction::class.qualifiedName -> CashierNavigationDestination.RiwayatTransaction
        CashierNavigationDestination.KelolaProduk::class.qualifiedName -> CashierNavigationDestination.KelolaProduk
        CashierNavigationDestination.DaftarProduk::class.qualifiedName -> CashierNavigationDestination.DaftarProduk
        CashierNavigationDestination.Pengaturan::class.qualifiedName -> CashierNavigationDestination.Pengaturan
        CashierNavigationDestination.PengaturanMeja::class.qualifiedName -> CashierNavigationDestination.PengaturanMeja
        CashierNavigationDestination.Kasir::class.qualifiedName -> CashierNavigationDestination.Kasir
        CashierNavigationDestination.Laporan::class.qualifiedName -> CashierNavigationDestination.Laporan
        CashierNavigationDestination.BahanBaku::class.qualifiedName -> CashierNavigationDestination.BahanBaku
        CashierNavigationDestination.AturResep::class.qualifiedName -> CashierNavigationDestination.AturResep
        else -> null
    }

    val CashierMainViewModel: CashierMainViewModel = viewModel(
        factory = CashierViewModelProvider.Factory,
    )

    val modelTampilanKasir = CashierMainViewModel.modelTampilan.collectAsStateWithLifecycle()

    // Observe store setting for sidebar conditional menu
    val pengaturanToko by aplikasi.kontainer.ambilStoreSetting().collectAsState(initial = StoreSetting())

    ModalNavigationDrawer(
        drawerState = statusDrawer,
        gesturesEnabled = true,
        drawerContent = {
            SidebarKasir(
                currentRoute = ruteSaatIni,
                onPilihMenu = { tujuan ->
                    cakupanKorutin.launch {
                        statusDrawer.close()
                    }
                    pengendaliNavigasi.navigate(tujuan) {
                        popUpTo(CashierNavigationDestination.Dashboard) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                apakahManajemenKasAktif = pengaturanToko.manajemenKasAktif,
                namaUsaha = pengaturanToko.namaUsaha,
                alamat = pengaturanToko.alamat,
                tagline = pengaturanToko.tagline,
                peran = peran,
                namaUser = namaUser,
                onKeluar = {
                    cakupanKorutin.launch {
                        aplikasi.kontainer.keluarAkun()
                    }
                },
            )
        },
    ) {
        NavHost(
            navController = pengendaliNavigasi,
            startDestination = CashierNavigationDestination.Dashboard,
        ) {
            composable<CashierNavigationDestination.KasirUtama> { entriBackStack ->
                LaunchedEffect(entriBackStack) {
                    entriBackStack.savedStateHandle
                        .ambilAlurPesanTambahProdukDariDetail()
                        .filterNotNull()
                        .collectLatest { pesan ->
                            CashierMainViewModel.tampilkanPesanOperasional(
                                pesan = pesan,
                            )

                            entriBackStack.savedStateHandle.konsumsiPesanTambahProdukDariDetail()
                        }
                }

                CashierMainScreen(
                    modelTampilan = modelTampilanKasir.value,
                    saatAksiDikirim = CashierMainViewModel::tanganiAksi,
                    alurEfek = CashierMainViewModel.efek,
                    saatBukaSidebar = {
                        cakupanKorutin.launch {
                            statusDrawer.open()
                        }
                    },
                    saatBukaDetailProduk = { identitasProduk ->
                        pengendaliNavigasi.bukaDetailProduk(
                            identitasProduk = identitasProduk,
                        )
                    },
                    saatBukaKasir = {
                        pengendaliNavigasi.bukaKasir()
                    },
                )
            }

            composable<CashierNavigationDestination.Dashboard> {
                val viewModel: DashboardViewModel = viewModel(
                    factory = CashierViewModelProvider.Factory,
                )
                val modelTampilan by viewModel.modelTampilan.collectAsStateWithLifecycle()

                DashboardScreen(
                    modelTampilan = modelTampilan,
                    saatKembali = {
                        pengendaliNavigasi.navigateUp()
                    },
                    saatBukaSidebar = {
                        cakupanKorutin.launch { statusDrawer.open() }
                    },
                    saatBukaTransaksi = {
                        pengendaliNavigasi.navigate(CashierNavigationDestination.KasirUtama)
                    },
                )
            }

            composable<CashierNavigationDestination.RiwayatTransaction> {
                val vm: TransactionHistoryViewModel = viewModel(
                    factory = CashierViewModelProvider.Factory,
                )

                val pagingData = vm.pagingData
                val filterTanggal by vm.filterTanggal.collectAsStateWithLifecycle()
                val labelRentang by vm.labelRentangKustom.collectAsStateWithLifecycle()

                TransactionHistoryScreen(
                    pagingData = pagingData,
                    filterTanggal = filterTanggal,
                    labelRentangKustom = labelRentang,
                    saatKembali = { pengendaliNavigasi.navigateUp() },
                    saatBukaSidebar = {
                        cakupanKorutin.launch { statusDrawer.open() }
                    },
                    saatBukaDetailTransaction = { id ->
                        pengendaliNavigasi.bukaDetailTransaction(id)
                    },
                    saatFilterTanggalBerubah = vm::perbaruiFilterTanggal,
                    saatFilterTanggalKustomBerubah = vm::perbaruiFilterTanggalKustom,
                    statusExport = vm.statusExport.collectAsStateWithLifecycle().value,
                    saatExportPdf = { uri, exportMulai, exportSelesai ->
                        vm.exportPdf(context, uri, pengaturanToko.namaUsaha, exportMulai, exportSelesai, pengaturanToko.alamat, pengaturanToko.tagline, pengaturanToko.logoUri)
                    },
                    saatBersihkanStatusExport = vm::bersihkanStatusExport,
                )
            }

            composable<CashierNavigationDestination.DetailProduk> {
                val ProductDetailViewModel: ProductDetailViewModel = viewModel(
                    factory = CashierViewModelProvider.Factory,
                )

                val modelTampilanDetail =
                    ProductDetailViewModel.modelTampilan.collectAsStateWithLifecycle()

                val stateAturStokDetail =
                    ProductDetailViewModel.stateAturStok.collectAsStateWithLifecycle()

                LaunchedEffect(ProductDetailViewModel) {
                    ProductDetailViewModel.efek.collectLatest { efek ->
                        when (efek) {
                            is ProductDetailEffect.MintaTambahKeKeranjang -> {
                                CashierMainViewModel.tanganiAksi(
                                    CashierMainAction.AddProductToCart(
                                        produkId = efek.produkId,
                                    ),
                                )

                                pengendaliNavigasi.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.simpanPesanTambahProdukDariDetail(
                                        pesan = "${efek.namaProduk} ditambahkan ke keranjang.",
                                    )

                                pengendaliNavigasi.navigateUp()
                            }
                        }
                    }
                }

                ProductDetailScreen(
                    modelTampilan = modelTampilanDetail.value,
                    stateAturStok = stateAturStokDetail.value,
                    saatKembali = {
                        pengendaliNavigasi.navigateUp()
                    },
                    saatAksiDikirim = ProductDetailViewModel::tanganiAksi,
                    bukaDialogAturStok = ProductDetailViewModel::bukaDialogAturStok,
                    tutupDialogAturStok = ProductDetailViewModel::tutupDialogAturStok,
                    perbaruiStokBaru = ProductDetailViewModel::perbaruiStokBaru,
                    perbaruiAlasanAturStok = ProductDetailViewModel::perbaruiAlasanAturStok,
                    simpanAturStok = ProductDetailViewModel::simpanAturStok,
                    bukaDialogRiwayat = ProductDetailViewModel::bukaDialogRiwayat,
                    tutupDialogRiwayat = ProductDetailViewModel::tutupDialogRiwayat,
                    bersihkanPesan = ProductDetailViewModel::bersihkanPesan,
                )
            }

            composable<CashierNavigationDestination.DetailTransaction> {
                val TransactionDetailViewModel: TransactionDetailViewModel = viewModel(
                    factory = CashierViewModelProvider.Factory,
                )

                val TransactionDetailUiState =
                    TransactionDetailViewModel.modelTampilan.collectAsStateWithLifecycle()

                TransactionDetailScreen(
                    modelTampilan = TransactionDetailUiState.value,
                    saatKembali = {
                        pengendaliNavigasi.navigateUp()
                    },
                    saatCobaMuatUlang = TransactionDetailViewModel::muatUlang,
                    bukaDialogBatalkan = TransactionDetailViewModel::bukaDialogBatalkan,
                    tutupDialogBatalkan = TransactionDetailViewModel::tutupDialogBatalkan,
                    perbaruiAlasanPembatalan = TransactionDetailViewModel::perbaruiAlasanPembatalan,
                    batalkan = TransactionDetailViewModel::batalkan,
                    alasanPembatalan = TransactionDetailUiState.value.alasanPembatalan,
                )
            }

            composable<CashierNavigationDestination.KelolaProduk> {
                ProductManagementMenuScreen(
                    navigasiKembali = { pengendaliNavigasi.navigateUp() },
                    saatBukaSidebar = {
                        cakupanKorutin.launch { statusDrawer.open() }
                    },
                    navigasiKeProduk = { pengendaliNavigasi.bukaDaftarProduk() },
                    navigasiKeBahanBaku = { pengendaliNavigasi.bukaBahanBaku() },
                    navigasiKeResep = { pengendaliNavigasi.bukaAturResep() },
                )
            }

            composable<CashierNavigationDestination.DaftarProduk> {
                val viewModel: ManageProductsViewModel = viewModel(
                    factory = CashierViewModelProvider.Factory,
                )
                ManageProductsScreen(
                    viewModel = viewModel,
                    navigasiKembali = { pengendaliNavigasi.navigateUp() },
                    navigasiKeTambahProduk = { pengendaliNavigasi.bukaFormProduk() },
                    navigasiKeUbahProduk = { id -> pengendaliNavigasi.bukaFormProduk(id) }
                )
            }

            composable<CashierNavigationDestination.FormProduk> {
                val viewModel: ProductFormViewModel = viewModel(
                    factory = CashierViewModelProvider.Factory,
                )
                ProductFormScreen(
                    viewModel = viewModel,
                    navigasiKembali = { pengendaliNavigasi.navigateUp() }
                )
            }

            composable<CashierNavigationDestination.Pengaturan> {
                val viewModel: SettingsViewModel = viewModel(
                    factory = CashierViewModelProvider.Factory,
                )
                val state by viewModel.state.collectAsStateWithLifecycle()

                SettingsScreen(
                    state = state,
                    saatKembali = { pengendaliNavigasi.navigateUp() },
                    saatBukaSidebar = {
                        cakupanKorutin.launch { statusDrawer.open() }
                    },
                    perbaruiLogoUri = viewModel::perbaruiLogoUri,
                    perbaruiNamaUsaha = viewModel::perbaruiNamaUsaha,
                    perbaruiAlamat = viewModel::perbaruiAlamat,
                    perbaruiTagline = viewModel::perbaruiTagline,
                    perbaruiCatalogDisplay = viewModel::perbaruiCatalogDisplay,
                    perbaruiPaymentMethodTunai = viewModel::perbaruiPaymentMethodTunai,
                    perbaruiPaymentMethodQris = viewModel::perbaruiPaymentMethodQris,
                    perbaruiReceiptPrintFormat = viewModel::perbaruiReceiptPrintFormat,
                    perbaruiPrinterType = viewModel::perbaruiPrinterType,
                    perbaruiPrinter = viewModel::perbaruiPrinter,
                    perbaruiSuaraNotifikasi = viewModel::perbaruiSuaraNotifikasi,
                    perbaruiSatuanStokDefault = viewModel::perbaruiSatuanStokDefault,
                    perbaruiJumlahTopFavorit = viewModel::perbaruiJumlahTopFavorit,
                    perbaruiManajemenKas = viewModel::perbaruiManajemenKas,
                    perbaruiStrukHeader = viewModel::perbaruiStrukHeader,
                    perbaruiStrukFooter = viewModel::perbaruiStrukFooter,
                    perbaruiLebarStruk = viewModel::perbaruiLebarStruk,
                    perbaruiJumlahCopyCetak = viewModel::perbaruiJumlahCopyCetak,
                    perbaruiTampilkanLogoDiStruk = viewModel::perbaruiTampilkanLogoDiStruk,
                    perbaruiTampilkanPajakDiStruk = viewModel::perbaruiTampilkanPajakDiStruk,
                    perbaruiBasisPoinPajak = viewModel::perbaruiBasisPoinPajak,
                    perbaruiBiayaLayanan = viewModel::perbaruiBiayaLayanan,
                    saatSimpan = viewModel::simpan,
                    saatSinkronkan = viewModel::sinkronkanSekarang,
                    saatBersihkanPesanSinkronisasi = viewModel::bersihkanPesanSinkronisasi,
                )
            }

            composable<CashierNavigationDestination.PengaturanMeja> {
                val viewModel: SettingsScreenMejaViewModel = viewModel(
                    factory = CashierViewModelProvider.Factory,
                )
                val state by viewModel.state.collectAsStateWithLifecycle()

                SettingsScreenMeja(
                    state = state,
                    saatKembali = { pengendaliNavigasi.navigateUp() },
                    saatBukaSidebar = {
                        cakupanKorutin.launch { statusDrawer.open() }
                    },
                    perbaruiJumlahBaris = viewModel::perbaruiJumlahBaris,
                    perbaruiJumlahKolom = viewModel::perbaruiJumlahKolom,
                    aturMeja = viewModel::aturMeja,
                    saatHapus = viewModel::hapusMeja,
                    saatBersihkanError = viewModel::bersihkanPesanError,
                    resetSemuaStatusMeja = viewModel::resetSemuaStatusMeja,
                    saatSimpanGrid = viewModel::simpanGrid,
                )
            }

            composable<CashierNavigationDestination.Laporan> {
                val viewModel: SalesReportViewModel = viewModel(
                    factory = CashierViewModelProvider.Factory,
                )
                val state by viewModel.state.collectAsStateWithLifecycle()

                SalesReportScreen(
                    state = state,
                    saatKembali = { pengendaliNavigasi.navigateUp() },
                    saatBukaSidebar = {
                        cakupanKorutin.launch { statusDrawer.open() }
                    },
                    perbaruiPeriode = viewModel::perbaruiPeriode,
                    perbaruiTanggalKustom = viewModel::perbaruiTanggalKustom,
                    exportCsv = { uri -> viewModel.exportCsv(aplikasi, uri, pengaturanToko.namaUsaha) },
                )
            }

            composable<CashierNavigationDestination.BahanBaku> {
                val viewModel: id.flexi.kasir.ui.bahan.BahanViewModel = viewModel(
                    factory = CashierViewModelProvider.Factory,
                )
                BahanManagementScreen(
                    viewModel = viewModel,
                    navigasiKembali = { pengendaliNavigasi.navigateUp() },
                    navigasiKeTambah = { pengendaliNavigasi.bukaFormBahan() },
                    navigasiKeDetail = { id -> pengendaliNavigasi.bukaDetailBahan(id) },
                )
            }

            composable<CashierNavigationDestination.DetailBahan> {
                val viewModel: id.flexi.kasir.ui.bahan.BahanDetailViewModel = viewModel(
                    factory = CashierViewModelProvider.Factory,
                )
                BahanDetailScreen(
                    viewModel = viewModel,
                    navigasiKembali = { pengendaliNavigasi.navigateUp() },
                    navigasiKeUbah = { id -> pengendaliNavigasi.bukaFormBahan(id) },
                )
            }

            composable<CashierNavigationDestination.FormBahan> {
                val viewModel: id.flexi.kasir.ui.bahan.BahanFormViewModel = viewModel(
                    factory = CashierViewModelProvider.Factory,
                )
                BahanFormScreen(
                    viewModel = viewModel,
                    navigasiKembali = { pengendaliNavigasi.navigateUp() },
                )
            }

            composable<CashierNavigationDestination.AturResep> {
                val viewModel: id.flexi.kasir.ui.resep.ResepViewModel = viewModel(
                    factory = CashierViewModelProvider.Factory,
                )
                ResepScreen(
                    viewModel = viewModel,
                    navigasiKembali = { pengendaliNavigasi.navigateUp() },
                )
            }

            composable<CashierNavigationDestination.Kasir> { entriBackStack ->
                val viewModel: CashRegisterViewModel = viewModel(
                    factory = CashierViewModelProvider.Factory,
                )
                val state by viewModel.state.collectAsStateWithLifecycle()
                val tutupBerhasil by viewModel.tutupBerhasil.collectAsStateWithLifecycle()
                val statusExport by viewModel.statusExport.collectAsStateWithLifecycle()
                val stateRekening by viewModel.stateRekening.collectAsStateWithLifecycle()

                CashRegisterScreen(
                    state = state,
                    tutupBerhasil = tutupBerhasil,
                    statusExport = statusExport,
                    stateRekening = stateRekening,
                    saatExportPdfDetail = { shiftId, uri ->
                        viewModel.exportPdfDetailKas(context, uri, shiftId, pengaturanToko.namaUsaha, pengaturanToko.alamat, pengaturanToko.tagline, pengaturanToko.logoUri)
                    },
                    saatExportPdfRekap = { uri, mulai, selesai ->
                        viewModel.exportPdfRekapKas(context, uri, mulai, selesai, pengaturanToko.namaUsaha, pengaturanToko.alamat, pengaturanToko.tagline, pengaturanToko.logoUri)
                    },
                    saatBersihkanStatusExport = viewModel::bersihkanStatusExport,
                    saatKembali = {
                        pengendaliNavigasi.navigateUp()
                    },
                    saatKembaliKeRekapKas = {
                        viewModel.clearTutupBerhasil()
                    },
                    saatBukaSidebar = {
                        cakupanKorutin.launch { statusDrawer.open() }
                    },
                    bukaDialogBuka = viewModel::bukaDialogBuka,
                    tutupDialogBuka = viewModel::tutupDialogBuka,
                    perbaruiTanggalBuka = viewModel::perbaruiTanggalBuka,
                    perbaruiJamBuka = viewModel::perbaruiJamBuka,
                    perbaruiNominalBuka = viewModel::perbaruiNominalBuka,
                    perbaruiCatatanBuka = viewModel::perbaruiCatatanBuka,
                    bukaKas = viewModel::bukaKas,
                    bukaDialogUangMasuk = viewModel::bukaDialogUangMasuk,
                    bukaDialogUangKeluar = viewModel::bukaDialogUangKeluar,
                    bukaDialogTutup = viewModel::bukaDialogTutup,
                    tutupDialogMutasi = viewModel::tutupDialogMutasi,
                    perbaruiTipeMutasi = viewModel::perbaruiTipeMutasi,
                    perbaruiKategoriMutasi = viewModel::perbaruiKategoriMutasi,
                    perbaruiNominalMutasi = viewModel::perbaruiNominalMutasi,
                    perbaruiCatatanMutasi = viewModel::perbaruiCatatanMutasi,
                    simpanMutasi = viewModel::simpanMutasi,
                    hapusMutasi = viewModel::hapusMutasi,
                    tutupDialogTutup = viewModel::tutupDialogTutup,
                    perbaruiSaldoFisik = viewModel::perbaruiSaldoFisik,
                    tutupKas = viewModel::tutupKas,
                    pilihKas = viewModel::pilihKas,
                    tutupDetailKas = viewModel::tutupDetailKas,
                    bukaDialogSetoran = viewModel::bukaDialogSetoran,
                    tutupDialogSetoran = viewModel::tutupDialogSetoran,
                    perbaruiNominalSetoran = viewModel::perbaruiNominalSetoran,
                    perbaruiCatatanSetoran = viewModel::perbaruiCatatanSetoran,
                    simpanSetoran = viewModel::simpanSetoran,
                    hapusSetoran = viewModel::hapusSetoran,
                    bukaDialogEditSetoran = viewModel::bukaDialogEditSetoran,
                    tutupDialogEditSetoran = viewModel::tutupDialogEditSetoran,
                    perbaruiCatatanEditSetoran = viewModel::perbaruiCatatanEditSetoran,
                    simpanEditSetoran = viewModel::simpanEditSetoran,
                    bukaDialogSaldoAwalRekening = viewModel::bukaDialogSaldoAwalRekening,
                    tutupDialogSaldoAwalRekening = viewModel::tutupDialogSaldoAwalRekening,
                    perbaruiNominalSaldoAwalRekening = viewModel::perbaruiNominalSaldoAwalRekening,
                    perbaruiCatatanSaldoAwalRekening = viewModel::perbaruiCatatanSaldoAwalRekening,
                    simpanSaldoAwalRekening = viewModel::simpanSaldoAwalRekening,
                    bukaDialogMutasiRekening = viewModel::bukaDialogMutasiRekening,
                    tutupDialogMutasiRekening = viewModel::tutupDialogMutasiRekening,
                    perbaruiNominalMutasiRekening = viewModel::perbaruiNominalMutasiRekening,
                    perbaruiCatatanMutasiRekening = viewModel::perbaruiCatatanMutasiRekening,
                    simpanMutasiRekening = viewModel::simpanMutasiRekening,
                    bersihkanPesanRekening = viewModel::bersihkanPesanRekening,
                )
            }
        }
    }
}
