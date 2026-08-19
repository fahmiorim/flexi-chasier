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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.flexi.kasir.domain.model.CatalogDisplay
import id.flexi.kasir.domain.model.LebarStruk
import id.flexi.kasir.domain.model.PrinterType
import id.flexi.kasir.domain.model.ReceiptPrintFormat
import id.flexi.kasir.ui.component.FlexiGradientButton
import id.flexi.kasir.ui.component.FlexiTopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune

/**
 * Compact section header dengan icon.
 */
@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.height(18.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Compact card container.
 */
@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            content()
        }
    }
}

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
    saatTestPrint: () -> Unit = {},
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
            saatBersihkanPesanSinkronisasi()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            // ── Top Bar ──
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                FlexiTopAppBar(
                    title = state.judulLayar,
                    saatKembali = saatKembali,
                    saatBukaSidebar = saatBukaSidebar,
                )
            }

            if (state.apakahSedangMemuat || state.apakahSedangMenyimpan) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // ── Konten utama — 2 kolom untuk tablet ──
            Row(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // ═══ KOLOM KIRI ═══
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Profil Bisnis
                    SettingsCard {
                        SectionHeader(icon = Icons.Default.Business, title = "Profil Bisnis")
                        BagianIdentitasUsaha(
                            logoUri = state.logoUri,
                            namaUsaha = state.namaUsaha,
                            alamat = state.alamat,
                            tagline = state.tagline,
                            perbaruiLogoUri = perbaruiLogoUri,
                            perbaruiNamaUsaha = perbaruiNamaUsaha,
                            perbaruiAlamat = perbaruiAlamat,
                            perbaruiTagline = perbaruiTagline,
                        )
                    }

                    // Pembayaran & Pajak
                    SettingsCard {
                        SectionHeader(icon = Icons.Default.CreditCard, title = "Pembayaran & Pajak")
                        BagianPayment(
                            PaymentMethodTunaiAktif = state.PaymentMethodTunaiAktif,
                            PaymentMethodQrisAktif = state.PaymentMethodQrisAktif,
                            perbaruiPaymentMethodTunai = perbaruiPaymentMethodTunai,
                            perbaruiPaymentMethodQris = perbaruiPaymentMethodQris,
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        BagianPajakDanBiaya(
                            basisPoinPajak = state.basisPoinPajak,
                            biayaLayanan = state.biayaLayanan,
                            perbaruiBasisPoinPajak = perbaruiBasisPoinPajak,
                            perbaruiBiayaLayanan = perbaruiBiayaLayanan,
                        )
                    }

                    // Tampilan & Operasional
                    SettingsCard {
                        SectionHeader(icon = Icons.Default.Tune, title = "Tampilan & Operasional")
                        BagianTampilan(
                            catalogDisplay = state.catalogDisplay,
                            perbaruiCatalogDisplay = perbaruiCatalogDisplay,
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        BagianKas(
                            manajemenKasAktif = state.manajemenKasAktif,
                            perbaruiManajemenKas = perbaruiManajemenKas,
                        )
                        BagianLainnya(
                            suaraNotifikasiAktif = state.suaraNotifikasiAktif,
                            satuanStokDefault = state.satuanStokDefault,
                            jumlahTopFavorit = state.jumlahTopFavorit,
                            perbaruiSuaraNotifikasi = perbaruiSuaraNotifikasi,
                            perbaruiSatuanStokDefault = perbaruiSatuanStokDefault,
                            perbaruiJumlahTopFavorit = perbaruiJumlahTopFavorit,
                        )
                    }
                }

                // ═══ KOLOM KANAN ═══
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Printer
                    SettingsCard {
                        SectionHeader(icon = Icons.Default.Print, title = "Printer")

                        Text(
                            text = "Printer Kasir",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        BagianPrinter(
                            printerType = state.printerType,
                            printerName = state.printerName,
                            printerAddress = state.printerAddress,
                            perbaruiPrinterType = perbaruiPrinterType,
                            perbaruiPrinter = perbaruiPrinter,
                            saatTestPrint = saatTestPrint,
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        Text(
                            text = "Printer Dapur",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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

                    // Pengaturan Struk
                    SettingsCard {
                        SectionHeader(icon = Icons.Default.Receipt, title = "Pengaturan Struk")
                        BagianStruk(
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

                    // Sinkronisasi
                    SettingsCard {
                        SectionHeader(icon = Icons.Default.Sync, title = "Sinkronisasi")
                        BagianSinkronisasi(
                            sinkronMesinStatus = state.sinkronMesinStatus,
                            saatSinkronkan = saatSinkronkan,
                        )
                    }
                }
            }

            // ── Bottom action bar ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
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
}
