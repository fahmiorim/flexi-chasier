package id.flexi.kasir.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import id.flexi.kasir.ui.component.FlexiGradientButton
import id.flexi.kasir.ui.component.FlexiTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.flexi.kasir.domain.model.CatalogDisplay
import id.flexi.kasir.domain.model.LebarStruk
import id.flexi.kasir.domain.model.PrinterType
import id.flexi.kasir.domain.model.ReceiptPrintFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    saatKembali: () -> Unit,
    saatBukaSidebar: (() -> Unit)? = null,
    perbaruiLogoUri: (String) -> Unit,
    perbaruiNamaUsaha: (String) -> Unit,
    perbaruiAlamat: (String) -> Unit,
    perbaruiTagline: (String) -> Unit,
    perbaruiCatalogDisplay: (CatalogDisplay) -> Unit,
    perbaruiPaymentMethodTunai: (Boolean) -> Unit,
    perbaruiPaymentMethodQris: (Boolean) -> Unit,
    perbaruiReceiptPrintFormat: (ReceiptPrintFormat) -> Unit,
    perbaruiPrinterType: (PrinterType) -> Unit,
    perbaruiPrinter: (alamat: String, nama: String) -> Unit,
    perbaruiSuaraNotifikasi: (Boolean) -> Unit,
    perbaruiSatuanStokDefault: (String) -> Unit,
    perbaruiJumlahTopFavorit: (String) -> Unit,
    perbaruiManajemenKas: (Boolean) -> Unit,
    perbaruiStrukHeader: (String) -> Unit,
    perbaruiStrukFooter: (String) -> Unit,
    perbaruiLebarStruk: (LebarStruk) -> Unit,
    perbaruiJumlahCopyCetak: (String) -> Unit,
    perbaruiTampilkanLogoDiStruk: (Boolean) -> Unit,
    perbaruiTampilkanPajakDiStruk: (Boolean) -> Unit,
    perbaruiPrinterDapurAktif: (Boolean) -> Unit,
    perbaruiPrinterDapurType: (PrinterType) -> Unit,
    perbaruiPrinterDapur: (alamat: String, nama: String) -> Unit,
    perbaruiBasisPoinPajak: (String) -> Unit,
    perbaruiBiayaLayanan: (String) -> Unit,
    saatSimpan: () -> Unit,
    saatSinkronkan: () -> Unit = {},
    saatBersihkanPesanSinkronisasi: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.pesanBerhasil) {
        state.pesanBerhasil?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    LaunchedEffect(state.pesanSinkronisasi) {
        state.pesanSinkronisasi?.let {
            snackbarHostState.showSnackbar(it)
            // Bersihkan agar pesan yang sama bisa ditampilkan lagi nanti.
            saatBersihkanPesanSinkronisasi()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (state.apakahSedangMemuat) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FlexiTopAppBar(
                title = state.judulLayar,
                saatKembali = saatKembali,
                saatBukaSidebar = saatBukaSidebar,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            if (state.apakahSedangMenyimpan) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Baris pertama: Identitas Usaha (kiri) + Payment & Tampilan (kanan)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BagianIdentitasUsaha(
                    modifier = Modifier.weight(1.2f),
                    logoUri = state.logoUri,
                    namaUsaha = state.namaUsaha,
                    alamat = state.alamat,
                    tagline = state.tagline,
                    perbaruiLogoUri = perbaruiLogoUri,
                    perbaruiNamaUsaha = perbaruiNamaUsaha,
                    perbaruiAlamat = perbaruiAlamat,
                    perbaruiTagline = perbaruiTagline,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    BagianPayment(
                        modifier = Modifier.fillMaxWidth(),
                        PaymentMethodTunaiAktif = state.PaymentMethodTunaiAktif,
                        PaymentMethodQrisAktif = state.PaymentMethodQrisAktif,
                        perbaruiPaymentMethodTunai = perbaruiPaymentMethodTunai,
                        perbaruiPaymentMethodQris = perbaruiPaymentMethodQris,
                    )
                    BagianTampilan(
                        modifier = Modifier.fillMaxWidth(),
                        catalogDisplay = state.catalogDisplay,
                        perbaruiCatalogDisplay = perbaruiCatalogDisplay,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    BagianPrinter(
                        printerType = state.printerType,
                        printerName = state.printerName,
                        printerAddress = state.printerAddress,
                        perbaruiPrinterType = perbaruiPrinterType,
                        perbaruiPrinter = perbaruiPrinter,
                    )
                    BagianKas(
                        modifier = Modifier.fillMaxWidth(),
                        manajemenKasAktif = state.manajemenKasAktif,
                        perbaruiManajemenKas = perbaruiManajemenKas,
                    )
                    BagianPrinterDapur(
                        printerDapurAktif = state.printerDapurAktif,
                        printerDapurType = state.printerDapurType,
                        printerDapurName = state.printerDapurName,
                        printerDapurAddress = state.printerDapurAddress,
                        perbaruiPrinterDapurAktif = perbaruiPrinterDapurAktif,
                        perbaruiPrinterDapurType = perbaruiPrinterDapurType,
                        perbaruiPrinterDapur = perbaruiPrinterDapur,
                    )
                }
                BagianStruk(
                    modifier = Modifier.weight(1f),
                    namaUsaha = state.namaUsaha,
                    logoUri = state.logoUri,
                    alamat = state.alamat,
                    tagline = state.tagline,
                    strukHeader = state.strukHeader,
                    strukFooter = state.strukFooter,
                    lebarStruk = state.lebarStruk,
                    jumlahCopyCetak = state.jumlahCopyCetak,
                    tampilkanLogoDiStruk = state.tampilkanLogoDiStruk,
                    tampilkanPajakDiStruk = state.tampilkanPajakDiStruk,
                    receiptPrintFormat = state.receiptPrintFormat,
                    perbaruiStrukHeader = perbaruiStrukHeader,
                    perbaruiStrukFooter = perbaruiStrukFooter,
                    perbaruiLebarStruk = perbaruiLebarStruk,
                    perbaruiJumlahCopyCetak = perbaruiJumlahCopyCetak,
                    perbaruiTampilkanLogoDiStruk = perbaruiTampilkanLogoDiStruk,
                    perbaruiTampilkanPajakDiStruk = perbaruiTampilkanPajakDiStruk,
                    perbaruiReceiptPrintFormat = perbaruiReceiptPrintFormat,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BagianLainnya(
                    modifier = Modifier.weight(1f),
                    suaraNotifikasiAktif = state.suaraNotifikasiAktif,
                    satuanStokDefault = state.satuanStokDefault,
                    jumlahTopFavorit = state.jumlahTopFavorit,
                    perbaruiSuaraNotifikasi = perbaruiSuaraNotifikasi,
                    perbaruiSatuanStokDefault = perbaruiSatuanStokDefault,
                    perbaruiJumlahTopFavorit = perbaruiJumlahTopFavorit,
                )
                BagianPajakDanBiaya(
                    modifier = Modifier.weight(1f),
                    basisPoinPajak = state.basisPoinPajak,
                    biayaLayanan = state.biayaLayanan,
                    perbaruiBasisPoinPajak = perbaruiBasisPoinPajak,
                    perbaruiBiayaLayanan = perbaruiBiayaLayanan,
                )
            }

            BagianSinkronisasi(
                sinkronMesinStatus = state.sinkronMesinStatus,
                saatSinkronkan = saatSinkronkan,
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlexiGradientButton(
                onClick = saatSimpan,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                enabled = !state.apakahSedangMenyimpan,
                text = "Simpan Pengaturan",
            )
        }
    }
}
