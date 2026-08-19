package id.flexi.kasir.ui.history

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import id.flexi.kasir.ui.component.FlexiTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    pagingData: Flow<PagingData<RingkasanTransactionRiwayat>>,
    filterTanggal: FilterTanggalRiwayat,
    labelRentangKustom: String,
    saatKembali: () -> Unit,
    saatBukaSidebar: (() -> Unit)? = null,
    saatBukaDetailTransaction: (String) -> Unit,
    saatFilterTanggalBerubah: (FilterTanggalRiwayat) -> Unit = {},
    saatFilterTanggalKustomBerubah: (Long, Long) -> Unit = { _, _ -> },
    statusExport: StatusExportPdf? = null,
    saatExportPdf: (Uri, Long?, Long?) -> Unit = { _, _, _ -> },
    saatBersihkanStatusExport: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var tampilDialogRentang by remember { mutableStateOf(false) }
    var tampilDialogRentangExport by remember { mutableStateOf(false) }
    var exportMulai by rememberSaveable { mutableStateOf(-1L) }
    var exportSelesai by rememberSaveable { mutableStateOf(-1L) }

    val context = LocalContext.current
    val snackbarState = remember { SnackbarHostState() }

    val pdfExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
        onResult = { uri ->
            if (uri != null) {
                saatExportPdf(
                    uri,
                    exportMulai.takeIf { it >= 0L },
                    exportSelesai.takeIf { it >= 0L },
                )
            }
            exportMulai = -1L
            exportSelesai = -1L
        },
    )

    if (tampilDialogRentang) {
        DialogRentangTanggalKustom(
            onDismiss = { tampilDialogRentang = false },
            onApply = { mulai, selesai ->
                saatFilterTanggalKustomBerubah(mulai, selesai)
                tampilDialogRentang = false
            },
        )
    }

    if (tampilDialogRentangExport) {
        DialogRentangTanggalKustom(
            onDismiss = { tampilDialogRentangExport = false },
            onApply = { mulai, selesai ->
                tampilDialogRentangExport = false
                val namaFile = "Riwayat_Transaksi_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale("id", "ID")).format(Date())}.pdf"
                exportMulai = mulai
                exportSelesai = selesai + 86400000L
                pdfExportLauncher.launch(namaFile)
            },
        )
    }

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

    val lazyItems = pagingData.collectAsLazyPagingItems()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarState) },
    ) { paddingDalam ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingDalam)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FlexiTopAppBar(
                title = "Riwayat Transaksi",
                saatKembali = saatKembali,
                saatBukaSidebar = saatBukaSidebar,
                modifier = Modifier.padding(bottom = 4.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BarisFilterTanggal(
                    filterAktif = filterTanggal,
                    labelRentangKustom = labelRentangKustom,
                    saatFilterBerubah = saatFilterTanggalBerubah,
                    saatPilihKustom = { tampilDialogRentang = true },
                    modifier = Modifier.weight(1f),
                )

                if (lazyItems.itemCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { tampilDialogRentangExport = true },
                        enabled = statusExport !is StatusExportPdf.SedangMengexport,
                        modifier = Modifier.height(28.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    ) {
                        if (statusExport is StatusExportPdf.SedangMengexport) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mengexport...", style = MaterialTheme.typography.labelMedium)
                        } else {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Konten paging dengan date header otomatis
            KontenRiwayatPaged(
                lazyItems = lazyItems,
                saatBukaDetailTransaction = saatBukaDetailTransaction,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
