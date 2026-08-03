package id.flexi.kasir.ui.report

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.flexi.kasir.ui.component.FlexiTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesReportScreen(
    state: SalesReportUiState,
    saatKembali: () -> Unit,
    saatBukaSidebar: (() -> Unit)? = null,
    perbaruiPeriode: (ReportPeriod) -> Unit,
    perbaruiTanggalKustom: (Long, Long) -> Unit,
    exportCsv: (android.net.Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var tampilDialogTanggal by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri ->
        uri?.let { exportCsv(it) }
    }

    if (tampilDialogTanggal) {
        DialogPilihTanggal(
            onDismiss = { tampilDialogTanggal = false },
            onApply = { mulai, selesai ->
                perbaruiTanggalKustom(mulai, selesai)
                tampilDialogTanggal = false
            },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        if (state.apakahSedangMemuat) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    FlexiTopAppBar(
                        title = "Laporan Penjualan",
                        saatKembali = saatKembali,
                        saatBukaSidebar = saatBukaSidebar,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }

                // Period filter chips
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ReportPeriod.entries.forEach { periode ->
                            FilterChip(
                                selected = state.periode == periode,
                                onClick = {
                                    if (periode == ReportPeriod.Kustom) {
                                        tampilDialogTanggal = true
                                    } else {
                                        perbaruiPeriode(periode)
                                    }
                                },
                                label = { Text(periodeLabel(periode)) },
                            )
                        }
                    }
                }

                // Custom range label
                if (state.periode == ReportPeriod.Kustom && state.labelRentangKustom.isNotBlank()) {
                    item {
                        Text(
                            text = state.labelRentangKustom,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                // Total sales card
                item {
                    KartuTotalPenjualan(
                        total = state.totalPenjualan,
                        jumlahTransaksi = state.jumlahTransaksi,
                        rataRata = state.rataRataPerTransaksi,
                        totalItem = state.totalItemTerjual,
                        totalDiskon = state.totalDiskon,
                    )
                }

                // Chart
                if (state.grafik7Hari.isNotEmpty()) {
                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                    item {
                        Text(
                            text = "Grafik Penjualan 7 Hari",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    item {
                        GrafikPenjualan7Hari(data = state.grafik7Hari)
                    }
                }

                // Payment methods
                item {
                    Text(
                        text = "Metode Pembayaran",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        KartuMetodeBayar(
                            label = "Tunai",
                            total = state.totalTunai,
                            jumlah = "${state.jumlahTransaksiTunai} transaksi",
                            warna = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                        )
                        KartuMetodeBayar(
                            label = "QRIS",
                            total = state.totalQris,
                            jumlah = "${state.jumlahTransaksiQris} transaksi",
                            warna = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // Category sales
                if (state.penjualanPerKategori.isNotEmpty()) {
                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                    item {
                        Text(
                            text = "Penjualan per Kategori",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    items(state.penjualanPerKategori) { kategori ->
                        BarisKategori(
                            nama = kategori.nama,
                            total = kategori.total,
                            jumlah = kategori.jumlah,
                            persentase = kategori.persentase,
                        )
                    }
                }

                // Export CSV
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Button(
                            onClick = {
                                val fileName = "Laporan_Penjualan_${System.currentTimeMillis()}.csv"
                                exportLauncher.launch(fileName)
                            },
                            enabled = !state.apakahSedangExport && state.jumlahTransaksi > 0,
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            if (state.apakahSedangExport) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp,
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Menyimpan...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export CSV", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    if (state.pesanError != null) {
                        Text(
                            text = state.pesanError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }

                // Empty state
                if (state.jumlahTransaksi == 0 && !state.apakahSedangMemuat) {
                    item {
                        Text(
                            text = "Belum ada transaksi untuk periode ini.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}
