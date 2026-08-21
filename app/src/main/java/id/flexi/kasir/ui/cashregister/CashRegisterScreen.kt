package id.flexi.kasir.ui.cashregister

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import id.flexi.kasir.domain.model.CashExpenseCategory
import id.flexi.kasir.domain.model.CashMutationType
import id.flexi.kasir.domain.model.CashKas
import id.flexi.kasir.domain.model.MutasiRekeningTipe
import id.flexi.kasir.domain.model.Setoran
import id.flexi.kasir.ui.history.StatusExportPdf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashRegisterScreen(
    state: CashRegisterUiState,
    tutupBerhasil: Boolean = false,
    saatKembali: () -> Unit,
    saatKembaliKeRekapKas: () -> Unit = saatKembali,
    saatBukaSidebar: (() -> Unit)? = null,
    bukaDialogBuka: () -> Unit,
    tutupDialogBuka: () -> Unit,
    perbaruiTanggalBuka: (Long) -> Unit,
    perbaruiJamBuka: (Int, Int) -> Unit,
    perbaruiNominalBuka: (String) -> Unit,
    perbaruiCatatanBuka: (String) -> Unit,
    bukaKas: () -> Unit,
    bukaDialogUangMasuk: () -> Unit,
    bukaDialogUangKeluar: () -> Unit,
    bukaDialogTutup: () -> Unit,
    tutupDialogMutasi: () -> Unit,
    perbaruiTipeMutasi: (CashMutationType) -> Unit,
    perbaruiKategoriMutasi: (CashExpenseCategory) -> Unit,
    perbaruiNominalMutasi: (String) -> Unit,
    perbaruiCatatanMutasi: (String) -> Unit,
    simpanMutasi: () -> Unit,
    hapusMutasi: (String) -> Unit,
    tutupDialogTutup: () -> Unit,
    perbaruiSaldoFisik: (String) -> Unit,
    tutupKas: () -> Unit,
    pilihKas: (CashKas) -> Unit = {},
    tutupDetailKas: () -> Unit = {},
    bukaDialogSetoran: () -> Unit = {},
    tutupDialogSetoran: () -> Unit = {},
    perbaruiNominalSetoran: (String) -> Unit = {},
    perbaruiCatatanSetoran: (String) -> Unit = {},
    simpanSetoran: () -> Unit = {},
    hapusSetoran: (String) -> Unit = {},
    bukaDialogEditSetoran: (Setoran) -> Unit = {},
    tutupDialogEditSetoran: () -> Unit = {},
    perbaruiCatatanEditSetoran: (String) -> Unit = {},
    simpanEditSetoran: () -> Unit = {},
    stateRekening: RekeningUiState = RekeningUiState(),
    bukaDialogSaldoAwalRekening: () -> Unit = {},
    tutupDialogSaldoAwalRekening: () -> Unit = {},
    perbaruiNominalSaldoAwalRekening: (String) -> Unit = {},
    perbaruiCatatanSaldoAwalRekening: (String) -> Unit = {},
    simpanSaldoAwalRekening: () -> Unit = {},
    bukaDialogMutasiRekening: (MutasiRekeningTipe) -> Unit = {},
    tutupDialogMutasiRekening: () -> Unit = {},
    perbaruiNominalMutasiRekening: (String) -> Unit = {},
    perbaruiCatatanMutasiRekening: (String) -> Unit = {},
    simpanMutasiRekening: () -> Unit = {},
    bersihkanPesanRekening: () -> Unit = {},
    statusExport: StatusExportPdf? = null,
    saatExportPdfDetail: (String, Uri) -> Unit = { _, _ -> },
    saatExportPdfRekap: (Uri, Long, Long) -> Unit = { _, _, _ -> },
    saatBersihkanStatusExport: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val snackbarState = remember { SnackbarHostState() }

    LaunchedEffect(statusExport) {
        when (statusExport) {
            is StatusExportPdf.Berhasil -> {
                snackbarState.showSnackbar("PDF berhasil diexport")
                saatBersihkanStatusExport()
            }
            is StatusExportPdf.Gagal -> {
                snackbarState.showSnackbar("Gagal export PDF: ${statusExport.pesan}")
                saatBersihkanStatusExport()
            }
            else -> {}
        }
    }

    LaunchedEffect(stateRekening.pesanSnackbar) {
        stateRekening.pesanSnackbar?.let {
            snackbarState.showSnackbar(it)
            bersihkanPesanRekening()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarState) },
    ) { padding ->
        if (tutupBerhasil) {
            TutupBerhasilContent(saatKembali = saatKembaliKeRekapKas, modifier = Modifier.padding(padding))
            return@Scaffold
        }

        when (state) {
            is CashRegisterUiState.Memuat -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            }

            is CashRegisterUiState.KasAktif -> {
                if (state.kasTerpilih != null) {
                    val isClosed = state.kasTerpilih.saldoAkhir != null
                    DetailKasPage(
                        shift = state.kasTerpilih,
                        ringkasan = state.ringkasanShift[state.kasTerpilih.id],
                        saldoSaatIni = if (isClosed) state.kasTerpilihSaldoSaatIni ?: state.saldoSaatIni else state.saldoSaatIni,
                        penjualanTunai = if (isClosed) state.kasTerpilihPenjualanTunai ?: state.penjualanTunai else state.penjualanTunai,
                        penjualanQRIS = if (isClosed) state.kasTerpilihPenjualanQRIS ?: state.penjualanQRIS else state.penjualanQRIS,
                        penjualanTotal = if (isClosed) state.kasTerpilihPenjualanTotal ?: state.penjualanTotal else state.penjualanTotal,
                        totalPemasukan = if (isClosed) state.kasTerpilihTotalPemasukan ?: state.totalPemasukan else state.totalPemasukan,
                        totalPengeluaran = if (isClosed) state.kasTerpilihTotalPengeluaran ?: state.totalPengeluaran else state.totalPengeluaran,
                        daftarMutasi = if (isClosed) state.kasTerpilihDaftarMutasi ?: state.daftarMutasi else state.daftarMutasi,
                        daftarTransaksi = if (isClosed) state.kasTerpilihDaftarTransaksi else null,
                        onTutupKas = if (!isClosed) {
                            { bukaDialogTutup() }
                        } else null,
                        saatKembali = tutupDetailKas,
                        saatExportPdf = { uri -> saatExportPdfDetail(state.kasTerpilih.id, uri) },
                        sedangExport = statusExport is StatusExportPdf.SedangMengexport,
                        modifier = Modifier.padding(padding),
                    )
                } else {
                    RekapKasContent(
                        state = state,
                        saatKembali = saatKembali,
                        saatBukaSidebar = saatBukaSidebar,
                        bukaDialogUangMasuk = bukaDialogUangMasuk,
                        bukaDialogUangKeluar = bukaDialogUangKeluar,
                        bukaDialogSetoran = bukaDialogSetoran,
                        hapusSetoran = hapusSetoran,
                        pilihKas = pilihKas,
                        hapusMutasi = hapusMutasi,
                        onEditSetoran = bukaDialogEditSetoran,
                        saatExportPdf = saatExportPdfRekap,
                        sedangExport = statusExport is StatusExportPdf.SedangMengexport,
                        stateRekening = stateRekening,
                        bukaDialogSaldoAwalRekening = bukaDialogSaldoAwalRekening,
                        tutupDialogSaldoAwalRekening = tutupDialogSaldoAwalRekening,
                        perbaruiNominalSaldoAwalRekening = perbaruiNominalSaldoAwalRekening,
                        perbaruiCatatanSaldoAwalRekening = perbaruiCatatanSaldoAwalRekening,
                        simpanSaldoAwalRekening = simpanSaldoAwalRekening,
                        bukaDialogMutasiRekening = bukaDialogMutasiRekening,
                        tutupDialogMutasiRekening = tutupDialogMutasiRekening,
                        perbaruiNominalMutasiRekening = perbaruiNominalMutasiRekening,
                        perbaruiCatatanMutasiRekening = perbaruiCatatanMutasiRekening,
                        simpanMutasiRekening = simpanMutasiRekening,
                        modifier = Modifier.padding(padding),
                    )
                }

                // Dialogs — rendered even when kasTerpilih is set
                if (state.apakahDialogMutasiTerbuka) {
                    DialogMutasi(
                        tipe = state.tipeMutasi, nominal = state.nominalMutasi, catatan = state.catatanMutasi,
                        apakahSedangMemproses = state.apakahSedangSimpanMutasi, pesanError = state.pesanErrorMutasi,
                        perbaruiTipe = perbaruiTipeMutasi, perbaruiNominal = perbaruiNominalMutasi,
                        perbaruiCatatan = perbaruiCatatanMutasi, simpan = simpanMutasi, tutup = tutupDialogMutasi,
                    )
                }
                if (state.apakahDialogTutupTerbuka) {
                    DialogTutupKas(state = state, perbaruiSaldoFisik = perbaruiSaldoFisik, tutupKas = tutupKas, tutup = tutupDialogTutup)
                }
                if (state.apakahDialogSetoranTerbuka) {
                    DialogSetoran(
                        nominal = state.nominalSetoran, catatan = state.catatanSetoran,
                        apakahSedangMemproses = state.apakahSedangSimpanSetoran, pesanError = state.pesanErrorSetoran,
                        perbaruiNominal = perbaruiNominalSetoran, perbaruiCatatan = perbaruiCatatanSetoran,
                        simpan = simpanSetoran, tutup = tutupDialogSetoran,
                    )
                }
                if (state.apakahDialogEditSetoranTerbuka && state.setoranYangDiedit != null) {
                    val setoranDiedit = state.setoranYangDiedit
                    DialogEditSetoran(
                        setoran = setoranDiedit, catatan = state.catatanEditSetoran,
                        apakahSedangMemproses = state.apakahSedangSimpanEditSetoran, pesanError = state.pesanErrorEditSetoran,
                        perbaruiCatatan = perbaruiCatatanEditSetoran, simpan = simpanEditSetoran,
                        hapus = { hapusSetoran(setoranDiedit.id) }, tutup = tutupDialogEditSetoran,
                    )
                }
            }

            is CashRegisterUiState.BelumBuka -> {
                if (state.kasTerpilih != null) {
                    DetailKasPage(
                        shift = state.kasTerpilih,
                        ringkasan = state.ringkasanShift[state.kasTerpilih.id],
                        saldoSaatIni = state.kasTerpilihSaldoSaatIni ?: state.penjualanTotalTerakhir,
                        penjualanTunai = state.kasTerpilihPenjualanTunai ?: state.penjualanTunaiTerakhir,
                        penjualanQRIS = state.kasTerpilihPenjualanQRIS ?: state.penjualanQRISTerakhir,
                        penjualanTotal = state.kasTerpilihPenjualanTotal ?: state.penjualanTotalTerakhir,
                        totalPemasukan = state.kasTerpilihTotalPemasukan ?: state.totalPemasukanTerakhir,
                        totalPengeluaran = state.kasTerpilihTotalPengeluaran ?: state.totalPengeluaranTerakhir,
                        daftarMutasi = state.kasTerpilihDaftarMutasi ?: state.daftarMutasiTerakhir,
                        daftarTransaksi = state.kasTerpilihDaftarTransaksi,
                        saatKembali = tutupDetailKas,
                        saatExportPdf = { uri -> saatExportPdfDetail(state.kasTerpilih.id, uri) },
                        sedangExport = statusExport is StatusExportPdf.SedangMengexport,
                        modifier = Modifier.padding(padding),
                    )
                } else {
                    RekapKasContent(
                        state = state,
                        saatKembali = saatKembali,
                        saatBukaSidebar = saatBukaSidebar,
                        bukaDialogBuka = bukaDialogBuka,
                        bukaDialogSetoran = bukaDialogSetoran,
                        hapusSetoran = hapusSetoran,
                        onEditSetoran = bukaDialogEditSetoran,
                        pilihKas = pilihKas,
                        saatExportPdf = saatExportPdfRekap,
                        sedangExport = statusExport is StatusExportPdf.SedangMengexport,
                        stateRekening = stateRekening,
                        bukaDialogSaldoAwalRekening = bukaDialogSaldoAwalRekening,
                        tutupDialogSaldoAwalRekening = tutupDialogSaldoAwalRekening,
                        perbaruiNominalSaldoAwalRekening = perbaruiNominalSaldoAwalRekening,
                        perbaruiCatatanSaldoAwalRekening = perbaruiCatatanSaldoAwalRekening,
                        simpanSaldoAwalRekening = simpanSaldoAwalRekening,
                        bukaDialogMutasiRekening = bukaDialogMutasiRekening,
                        tutupDialogMutasiRekening = tutupDialogMutasiRekening,
                        perbaruiNominalMutasiRekening = perbaruiNominalMutasiRekening,
                        perbaruiCatatanMutasiRekening = perbaruiCatatanMutasiRekening,
                        simpanMutasiRekening = simpanMutasiRekening,
                        modifier = Modifier.padding(padding),
                    )
                }
                if (state.apakahDialogSetoranTerbuka) {
                    DialogSetoran(
                        nominal = state.nominalSetoran, catatan = state.catatanSetoran,
                        apakahSedangMemproses = state.apakahSedangSimpanSetoran, pesanError = state.pesanErrorSetoran,
                        perbaruiNominal = perbaruiNominalSetoran, perbaruiCatatan = perbaruiCatatanSetoran,
                        simpan = simpanSetoran, tutup = tutupDialogSetoran,
                    )
                }
                if (state.apakahDialogBukaTerbuka) {
                    DialogBukaKas(
                        tanggalEpochMili = state.tanggalBukaEpochMili, jam = state.jamBukaJam, menit = state.jamBukaMenit,
                        nominal = state.nominalBuka, catatan = state.catatanBuka,
                        apakahSedangMemproses = state.apakahSedangMemproses, pesanError = state.pesanError,
                        perbaruiTanggal = perbaruiTanggalBuka, perbaruiJam = perbaruiJamBuka,
                        perbaruiNominal = perbaruiNominalBuka, perbaruiCatatan = perbaruiCatatanBuka,
                        bukaKas = bukaKas, tutup = tutupDialogBuka,
                    )
                }
                if (state.apakahDialogEditSetoranTerbuka && state.setoranYangDiedit != null) {
                    val setoranDiedit = state.setoranYangDiedit
                    DialogEditSetoran(
                        setoran = setoranDiedit, catatan = state.catatanEditSetoran,
                        apakahSedangMemproses = state.apakahSedangSimpanEditSetoran, pesanError = state.pesanErrorEditSetoran,
                        perbaruiCatatan = perbaruiCatatanEditSetoran, simpan = simpanEditSetoran,
                        hapus = { hapusSetoran(setoranDiedit.id) }, tutup = tutupDialogEditSetoran,
                    )
                }
            }
        }
    }
}
