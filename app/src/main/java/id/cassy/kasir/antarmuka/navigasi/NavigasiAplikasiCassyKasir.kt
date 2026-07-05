package id.cassy.kasir.antarmuka.navigasi

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.cassy.kasir.antarmuka.PenyediaViewModelKasir
import id.cassy.kasir.antarmuka.dashboard.LayarDashboard
import id.cassy.kasir.antarmuka.dashboard.LayarDashboardViewModel
import id.cassy.kasir.antarmuka.detail.EfekLayarDetailProduk
import id.cassy.kasir.antarmuka.detail.LayarDetailProduk
import id.cassy.kasir.antarmuka.detail.LayarDetailProdukViewModel
import id.cassy.kasir.antarmuka.kelola.LayarFormProduk
import id.cassy.kasir.antarmuka.kelola.LayarKelolaProduk
import id.cassy.kasir.antarmuka.kelola.ViewModelFormProduk
import id.cassy.kasir.antarmuka.kelola.ViewModelKelolaProduk
import id.cassy.kasir.antarmuka.komponen.SidebarKasir
import id.cassy.kasir.antarmuka.meja.LayarPengaturanMeja
import id.cassy.kasir.antarmuka.meja.LayarPengaturanMejaViewModel
import id.cassy.kasir.antarmuka.pengaturan.LayarPengaturan
import id.cassy.kasir.antarmuka.pengaturan.LayarPengaturanViewModel
import id.cassy.kasir.antarmuka.riwayat.LayarRiwayatTransaksi
import id.cassy.kasir.antarmuka.riwayat.LayarRiwayatTransaksiViewModel
import id.cassy.kasir.antarmuka.transaksi.LayarDetailTransaksi
import id.cassy.kasir.antarmuka.transaksi.LayarDetailTransaksiViewModel
import id.cassy.kasir.antarmuka.utama.AksiLayarUtamaKasir
import id.cassy.kasir.antarmuka.utama.LayarUtamaKasir
import id.cassy.kasir.antarmuka.utama.LayarUtamaKasirViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

@Composable
fun NavigasiAplikasiCassyKasir() {
    val pengendaliNavigasi = rememberNavController()
    val statusDrawer = rememberDrawerState(initialValue = DrawerValue.Closed)
    val cakupanKorutin = rememberCoroutineScope()

    val masukanBackStack by pengendaliNavigasi.currentBackStackEntryAsState()
    val ruteSaatIni: TujuanNavigasiKasir? = when (masukanBackStack?.destination?.route) {
        "Dashboard" -> TujuanNavigasiKasir.Dashboard
        "KasirUtama" -> TujuanNavigasiKasir.KasirUtama
        "RiwayatTransaksi" -> TujuanNavigasiKasir.RiwayatTransaksi
        "KelolaProduk" -> TujuanNavigasiKasir.KelolaProduk
        "Pengaturan" -> TujuanNavigasiKasir.Pengaturan
        "PengaturanMeja" -> TujuanNavigasiKasir.PengaturanMeja
        else -> null
    }

    val layarUtamaKasirViewModel: LayarUtamaKasirViewModel = viewModel(
        factory = PenyediaViewModelKasir.Factory,
    )

    val modelTampilanKasir = layarUtamaKasirViewModel.modelTampilan.collectAsStateWithLifecycle()

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
                        popUpTo(TujuanNavigasiKasir.KasirUtama) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
    ) {
        NavHost(
            navController = pengendaliNavigasi,
            startDestination = TujuanNavigasiKasir.KasirUtama,
        ) {
            composable<TujuanNavigasiKasir.KasirUtama> { entriBackStack ->
                LaunchedEffect(entriBackStack) {
                    entriBackStack.savedStateHandle
                        .ambilAlurPesanTambahProdukDariDetail()
                        .filterNotNull()
                        .collectLatest { pesan ->
                            layarUtamaKasirViewModel.tampilkanPesanOperasional(
                                pesan = pesan,
                            )

                            entriBackStack.savedStateHandle.konsumsiPesanTambahProdukDariDetail()
                        }
                }

                LayarUtamaKasir(
                    modelTampilan = modelTampilanKasir.value,
                    saatAksiDikirim = layarUtamaKasirViewModel::tanganiAksi,
                    alurEfek = layarUtamaKasirViewModel.efek,
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
                )
            }

            composable<TujuanNavigasiKasir.Dashboard> {
                val viewModel: LayarDashboardViewModel = viewModel(
                    factory = PenyediaViewModelKasir.Factory,
                )
                val modelTampilan by viewModel.modelTampilan.collectAsStateWithLifecycle()

                LayarDashboard(
                    modelTampilan = modelTampilan,
                    saatKembali = {
                        pengendaliNavigasi.navigateUp()
                    },
                )
            }

            composable<TujuanNavigasiKasir.RiwayatTransaksi> {
                val layarRiwayatTransaksiViewModel: LayarRiwayatTransaksiViewModel = viewModel(
                    factory = PenyediaViewModelKasir.Factory,
                )

                val modelTampilanRiwayat =
                    layarRiwayatTransaksiViewModel.modelTampilan.collectAsStateWithLifecycle()

                LayarRiwayatTransaksi(
                    modelTampilan = modelTampilanRiwayat.value,
                    saatKembali = {
                        pengendaliNavigasi.navigateUp()
                    },
                    saatBukaDetailTransaksi = { identitasTransaksi ->
                        pengendaliNavigasi.bukaDetailTransaksi(
                            identitasTransaksi = identitasTransaksi,
                        )
                    },
                    saatCobaMuatUlang = layarRiwayatTransaksiViewModel::muatUlang,
                    saatKataKunciPencarianBerubah =
                        layarRiwayatTransaksiViewModel::perbaruiKataKunciPencarian,
                    saatResetPencarian = layarRiwayatTransaksiViewModel::resetPencarian,
                    saatFilterTanggalBerubah = layarRiwayatTransaksiViewModel::perbaruiFilterTanggal,
                )
            }

            composable<TujuanNavigasiKasir.DetailProduk> {
                val layarDetailProdukViewModel: LayarDetailProdukViewModel = viewModel(
                    factory = PenyediaViewModelKasir.Factory,
                )

                val modelTampilanDetail =
                    layarDetailProdukViewModel.modelTampilan.collectAsStateWithLifecycle()

                LaunchedEffect(layarDetailProdukViewModel) {
                    layarDetailProdukViewModel.efek.collectLatest { efek ->
                        when (efek) {
                            is EfekLayarDetailProduk.MintaTambahKeKeranjang -> {
                                layarUtamaKasirViewModel.tanganiAksi(
                                    AksiLayarUtamaKasir.TambahProdukKeKeranjang(
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

                LayarDetailProduk(
                    modelTampilan = modelTampilanDetail.value,
                    saatKembali = {
                        pengendaliNavigasi.navigateUp()
                    },
                    saatAksiDikirim = layarDetailProdukViewModel::tanganiAksi,
                )
            }

            composable<TujuanNavigasiKasir.DetailTransaksi> {
                val layarDetailTransaksiViewModel: LayarDetailTransaksiViewModel = viewModel(
                    factory = PenyediaViewModelKasir.Factory,
                )

                val modelTampilanDetailTransaksi =
                    layarDetailTransaksiViewModel.modelTampilan.collectAsStateWithLifecycle()

                LayarDetailTransaksi(
                    modelTampilan = modelTampilanDetailTransaksi.value,
                    saatKembali = {
                        pengendaliNavigasi.navigateUp()
                    },
                    saatCobaMuatUlang = layarDetailTransaksiViewModel::muatUlang,
                )
            }

            composable<TujuanNavigasiKasir.KelolaProduk> {
                val viewModel: ViewModelKelolaProduk = viewModel(
                    factory = PenyediaViewModelKasir.Factory,
                )
                LayarKelolaProduk(
                    viewModel = viewModel,
                    navigasiKembali = { pengendaliNavigasi.navigateUp() },
                    navigasiKeTambahProduk = { pengendaliNavigasi.bukaFormProduk() },
                    navigasiKeUbahProduk = { id -> pengendaliNavigasi.bukaFormProduk(id) }
                )
            }

            composable<TujuanNavigasiKasir.FormProduk> {
                val viewModel: ViewModelFormProduk = viewModel(
                    factory = PenyediaViewModelKasir.Factory,
                )
                LayarFormProduk(
                    viewModel = viewModel,
                    navigasiKembali = { pengendaliNavigasi.navigateUp() }
                )
            }

            composable<TujuanNavigasiKasir.Pengaturan> {
                val viewModel: LayarPengaturanViewModel = viewModel(
                    factory = PenyediaViewModelKasir.Factory,
                )
                val state by viewModel.state.collectAsStateWithLifecycle()

                LayarPengaturan(
                    state = state,
                    saatKembali = { pengendaliNavigasi.navigateUp() },
                    perbaruiLogoUri = viewModel::perbaruiLogoUri,
                    perbaruiNamaUsaha = viewModel::perbaruiNamaUsaha,
                    perbaruiAlamat = viewModel::perbaruiAlamat,
                    perbaruiTampilanKatalog = viewModel::perbaruiTampilanKatalog,
                    perbaruiMetodeBayarTunai = viewModel::perbaruiMetodeBayarTunai,
                    perbaruiMetodeBayarQris = viewModel::perbaruiMetodeBayarQris,
                    perbaruiFormatCetakStruk = viewModel::perbaruiFormatCetakStruk,
                    perbaruiSuaraNotifikasi = viewModel::perbaruiSuaraNotifikasi,
                    perbaruiSatuanStokDefault = viewModel::perbaruiSatuanStokDefault,
                    saatSimpan = viewModel::simpan,
                )
            }

            composable<TujuanNavigasiKasir.PengaturanMeja> {
                val viewModel: LayarPengaturanMejaViewModel = viewModel(
                    factory = PenyediaViewModelKasir.Factory,
                )
                val state by viewModel.state.collectAsStateWithLifecycle()

                LayarPengaturanMeja(
                    state = state,
                    saatKembali = { pengendaliNavigasi.navigateUp() },
                    perbaruiNomorMejaBaru = viewModel::perbaruiNomorMejaBaru,
                    saatTambah = viewModel::tambahMeja,
                    saatHapus = viewModel::hapusMeja,
                    saatBersihkanError = viewModel::bersihkanPesanError,
                )
            }
        }
    }
}
