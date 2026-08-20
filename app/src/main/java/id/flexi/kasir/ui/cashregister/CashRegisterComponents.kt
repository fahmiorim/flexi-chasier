package id.flexi.kasir.ui.cashregister

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.outlined.Info
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.flexi.kasir.domain.model.CashMutationType
import id.flexi.kasir.domain.model.CashKas
import id.flexi.kasir.domain.model.MutasiRekening
import id.flexi.kasir.domain.model.MutasiRekeningTipe
import id.flexi.kasir.domain.model.Setoran
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.model.PaymentMethod
import id.flexi.kasir.domain.util.hitungTotalAkhirTransaction
import id.flexi.kasir.domain.util.sebagaiRupiah
import id.flexi.kasir.ui.component.FlexiBadge
import id.flexi.kasir.ui.component.FlexiCard
import id.flexi.kasir.ui.component.FlexiDialog
import id.flexi.kasir.ui.component.FlexiDialogActions
import id.flexi.kasir.ui.component.FlexiDialogHeader
import id.flexi.kasir.ui.component.FlexiDialogSingleAction
import id.flexi.kasir.ui.component.FlexiSectionLabel
import id.flexi.kasir.ui.component.FlexiTopAppBar
import id.flexi.kasir.ui.history.DialogRentangTanggalKustom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal val GreenAksen = Color(0xFF059669)
internal val BlueAksen = Color(0xFF2563EB)
internal val RedAksen = Color(0xFFE11D48)
internal val AmberAksen = Color(0xFFD97706)

// ═══════════════════════════════════════
// REKAP KAS — Unified content
// ═══════════════════════════════════════

@Composable
internal fun RekapKasContent(
    state: CashRegisterUiState,
    saatKembali: () -> Unit,
    saatBukaSidebar: (() -> Unit)? = null,
    bukaDialogBuka: () -> Unit = {},
    bukaDialogUangMasuk: () -> Unit = {},
    bukaDialogUangKeluar: () -> Unit = {},
    bukaDialogSetoran: () -> Unit = {},
    hapusSetoran: (String) -> Unit = {},
    onEditSetoran: (Setoran) -> Unit = {},
    pilihKas: (CashKas) -> Unit = {},
    hapusMutasi: (String) -> Unit = {},
    saatExportPdf: (Uri, Long, Long) -> Unit = { _, _, _ -> },
    sedangExport: Boolean = false,
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
    modifier: Modifier = Modifier,
) {
    val formatTanggal = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) }
    val pemformatTanggal = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }

    var tampilDialogRentang by remember { mutableStateOf(false) }
    var exportMulai by rememberSaveable { mutableStateOf(-1L) }
    var exportSelesai by rememberSaveable { mutableStateOf(-1L) }

    val pdfExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
        onResult = { uri ->
            if (uri != null) {
                saatExportPdf(
                    uri,
                    exportMulai.takeIf { it >= 0L } ?: 0L,
                    exportSelesai.takeIf { it >= 0L } ?: 0L,
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
                tampilDialogRentang = false
                val namaFile = "Rekap_Kas_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale("id", "ID")).format(Date())}.pdf"
                exportMulai = mulai
                exportSelesai = selesai + 86400000L
                pdfExportLauncher.launch(namaFile)
            },
        )
    }

    val shiftAktif = when (state) {
        is CashRegisterUiState.KasAktif -> state.kas
        else -> null
    }
    val isAktif = shiftAktif != null && state is CashRegisterUiState.KasAktif
    val daftarKasTertutup = when (state) {
        is CashRegisterUiState.KasAktif -> state.daftarKasTertutup
        is CashRegisterUiState.BelumBuka -> state.daftarKasTertutup
        else -> emptyList()
    }
    val daftarSetoran = when (state) {
        is CashRegisterUiState.KasAktif -> state.daftarSetoran
        is CashRegisterUiState.BelumBuka -> state.daftarSetoran
        else -> emptyList()
    }

    val riwayatGroup = remember(daftarKasTertutup, daftarSetoran, shiftAktif) {
        val items = mutableListOf<Pair<Long, Any>>()
        shiftAktif?.let { items.add(it.waktuBuka to it) }
        daftarKasTertutup.forEach { items.add(it.waktuBuka to it) }
        daftarSetoran.forEach { items.add(it.waktu to it) }
        items.sortByDescending { it.first }
        items.groupBy { pemformatTanggal.format(Date(it.first)) }
            .entries.toList()
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FlexiTopAppBar(
            title = "Rekap Kas",
            saatKembali = saatKembali,
            saatBukaSidebar = saatBukaSidebar,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ── Total Saldo Tunai (tampil di kedua state) ──
            item {
                // Saldo yang bermakna lintas platform: saldo shift yang sedang aktif,
                // atau saldo terakhir shift yang baru ditutup (bukan akumulasi semua shift).
                val saldoTampil = when (state) {
                    is CashRegisterUiState.KasAktif -> state.saldoSaatIni
                    is CashRegisterUiState.BelumBuka -> state.saldoSaatIniTerakhir
                    else -> "Rp0"
                }

                val isKasAktif = state is CashRegisterUiState.KasAktif
                val penjualanTunai = if (state is CashRegisterUiState.KasAktif) state.penjualanTunai else "Rp0"
                val penjualanQRIS = if (state is CashRegisterUiState.KasAktif) state.penjualanQRIS else "Rp0"
                val totalPemasukan = when (state) {
                    is CashRegisterUiState.KasAktif -> state.totalPemasukan
                    is CashRegisterUiState.BelumBuka -> state.totalPemasukanTerakhir
                    else -> "Rp0"
                }
                val totalPengeluaran = when (state) {
                    is CashRegisterUiState.KasAktif -> state.totalPengeluaran
                    is CashRegisterUiState.BelumBuka -> state.totalPengeluaranTerakhir
                    else -> "Rp0"
                }

                // Ambil saldo awal dari shift aktif
                val saldoAwalTampil = when (state) {
                    is CashRegisterUiState.KasAktif -> state.kas.saldoAwal.nilaiRupiah.sebagaiRupiah()
                    else -> "Rp0"
                }
                val totalSetoranTampil = when (state) {
                    is CashRegisterUiState.KasAktif -> state.totalSetoran
                    else -> "Rp0"
                }

                // Profit dari shift yang sudah ditutup (sudah dihitung di ViewModel)
                val profitDariShiftTertutup = when (state) {
                    is CashRegisterUiState.BelumBuka -> state.saldoSaatIniTerakhir.replace(Regex("[^\\d]"), "").toLongOrNull() ?: 0L
                    is CashRegisterUiState.KasAktif -> state.akumulasiProfitShiftTertutup
                    else -> 0L
                }
                // Profit hari ini (jika kas aktif)
                val profitHariIni = if (state is CashRegisterUiState.KasAktif) {
                    val penjualanTunaiAngka = state.penjualanTunai.replace(Regex("[^\\d]"), "").toLongOrNull() ?: 0L
                    val pemasukanAngka = state.totalPemasukan.replace(Regex("[^\\d]"), "").toLongOrNull() ?: 0L
                    val pengeluaranAngka = state.totalPengeluaran.replace(Regex("[^\\d]"), "").toLongOrNull() ?: 0L
                    penjualanTunaiAngka + pemasukanAngka - pengeluaranAngka
                } else 0L
                val totalProfitTerakumulasi = profitDariShiftTertutup + profitHariIni
                val totalSetoranAngka = daftarSetoran.filter { !it.dihapus }.sumOf { it.nominal.nilaiRupiah }
                // BelumBuka: cachedAkumulasiProfit SUDAH mengurangi setoran per shift, tidak perlu kurangi lagi.
                // KasAktif: profitHariIni belum dikurangi setoran, totalSetoranAngka hanya setoran shift aktif.
                val sisaBelumDisetor = if (state is CashRegisterUiState.BelumBuka) {
                    totalProfitTerakumulasi
                } else {
                    totalProfitTerakumulasi - totalSetoranAngka
                }

                SaldoKasBanner(
                    saldoAwal = saldoAwalTampil,
                    penjualanTunai = penjualanTunai,
                    totalPemasukan = totalPemasukan,
                    totalPengeluaran = totalPengeluaran,
                    totalProfitTerakumulasi = totalProfitTerakumulasi.sebagaiRupiah(),
                    totalSetoran = totalSetoranAngka.sebagaiRupiah(),
                    sisaBelumDisetor = sisaBelumDisetor.sebagaiRupiah(),
                    isAktif = isKasAktif,
                    bukaDialogSetoran = bukaDialogSetoran,
                    bukaDialogMutasi = bukaDialogUangKeluar,
                )
            }

            // ── Aksi Belum Buka Kas ──
            if (state is CashRegisterUiState.BelumBuka) {
                item {
                    BelumBukaBanner(bukaDialogBuka = bukaDialogBuka)
                }
            }

            // ── Riwayat Kas ──
            if (riwayatGroup.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 0.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        FlexiSectionLabel(
                            text = "Riwayat Kas",
                            modifier = Modifier.weight(1f).padding(vertical = 0.dp),
                        )
                        OutlinedButton(
                            onClick = { tampilDialogRentang = true },
                            enabled = !sedangExport,
                            modifier = Modifier.height(30.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        ) {
                            if (sedangExport) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Mengexport...", style = MaterialTheme.typography.labelMedium)
                            } else {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export PDF", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                riwayatGroup.forEach { (tanggal, entries) ->
                    item(key = "riwayat_$tanggal") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                            )
                            Text(
                                text = tanggal,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                            ) {
                                Text(
                                    text = "${entries.size}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    entries.forEach { (_, item) ->
                        when (item) {
                            is CashKas -> {
                                val shift = item
                                item(key = "shift_${shift.id}") {
                                    KasCard(
                                        shift = shift,
                                        formatTanggal = formatTanggal,
                                        onClick = { pilihKas(shift) },
                                    )
                                }
                            }
                            is Setoran -> {
                                val setoran = item
                                item(key = "setoran_${setoran.id}") {
                                    SetoranCard(
                                        setoran = setoran,
                                        formatTanggal = formatTanggal,
                                        onHapus = { hapusSetoran(setoran.id) },
                                        onEdit = { onEditSetoran(setoran) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════
// SALDO KAS BANNER — Premium hero card
// ═══════════════════════════════════════

@Composable
internal fun SaldoKasBanner(
    saldoAwal: String = "Rp0",
    penjualanTunai: String = "Rp0",
    totalPemasukan: String = "Rp0",
    totalPengeluaran: String = "Rp0",
    totalProfitTerakumulasi: String = "Rp0",
    totalSetoran: String = "Rp0",
    sisaBelumDisetor: String = "Rp0",
    isAktif: Boolean = true,
    bukaDialogSetoran: () -> Unit = {},
    bukaDialogMutasi: () -> Unit = {},
) {
    val heroColor = MaterialTheme.colorScheme.primary

    // Hitung uang di laci: saldo awal + tunai + pemasukan - pengeluaran - setoran
    val saldoAwalAngka = saldoAwal.replace(Regex("[^\\d]"), "").toLongOrNull() ?: 0L
    val penjualanTunaiAngka = penjualanTunai.replace(Regex("[^\\d]"), "").toLongOrNull() ?: 0L
    val pemasukanAngka = totalPemasukan.replace(Regex("[^\\d]"), "").toLongOrNull() ?: 0L
    val pengeluaranAngka = totalPengeluaran.replace(Regex("[^\\d]"), "").toLongOrNull() ?: 0L
    val setoranAngka = totalSetoran.replace(Regex("[^\\d]"), "").toLongOrNull() ?: 0L
    val uangDiLaci = saldoAwalAngka + penjualanTunaiAngka + pemasukanAngka - pengeluaranAngka - setoranAngka

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
            .border(1.dp, heroColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = heroColor),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Saldo Kas (uang di laci)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                )
                Button(
                    onClick = bukaDialogSetoran,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RedAksen,
                        contentColor = Color.White,
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    modifier = Modifier.height(34.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Setoran", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Uang di laci
            Text(
                text = uangDiLaci.sebagaiRupiah(),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp,
                ),
                color = MaterialTheme.colorScheme.onPrimary,
            )

            // Rincian uang di laci (saat kas aktif)
            if (isAktif) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.08f),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        InfoBarisKas("Saldo Awal", saldoAwal, MaterialTheme.colorScheme.onPrimary)
                        HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("+ Penjualan Tunai", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                                Text(penjualanTunai, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimary)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("+ Pemasukan", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                                Text(totalPemasukan, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 4.dp))
                        InfoBarisKas("- Pengeluaran", totalPengeluaran, MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }

            // Profit Belum Disetor
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.15f),
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Text(
                        text = "Profit Belum Disetor",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    InfoBarisKas("Total Profit", totalProfitTerakumulasi, MaterialTheme.colorScheme.onPrimary)
                    InfoBarisKas("Sudah Disetor", totalSetoran, MaterialTheme.colorScheme.onPrimary)
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Sisa", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimary)
                        Text(sisaBelumDisetor, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }

            // Action buttons (hanya saat kas aktif)
            if (isAktif) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = bukaDialogMutasi,
                    modifier = Modifier.fillMaxWidth().height(38.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenAksen,
                        contentColor = Color.White,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pengeluaran", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

// ═══════════════════════════════════════
// BELUM BUKA BANNER — Premium
// ═══════════════════════════════════════

@Composable
internal fun BelumBukaBanner(bukaDialogBuka: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
            .border(1.dp, primaryColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Icon with gradient background
                Box(
                    modifier = Modifier.size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(primaryColor, primaryColor.copy(alpha = 0.75f))
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.White,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Belum Buka Kas",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(
                        "Buka kas untuk mulai mencatat transaksi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = bukaDialogBuka,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buka Kas", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// ═══════════════════════════════════════
// KAS CARD — Premium riwayat
// ═══════════════════════════════════════

@Composable
internal fun KasCard(
    shift: CashKas,
    formatTanggal: SimpleDateFormat,
    onClick: () -> Unit,
) {
    val isSelesai = shift.saldoAkhir != null
    val cardColor = if (isSelesai) GreenAksen else BlueAksen
    val durasi = remember(shift) {
        val mulai = shift.waktuBuka
        val akhir = shift.waktuTutup ?: System.currentTimeMillis()
        val selisihMs = akhir - mulai
        val jam = selisihMs / 3_600_000
        val menit = (selisihMs % 3_600_000) / 60_000
        if (jam > 0) "${jam}j ${menit}m" else "${menit}m"
    }

    FlexiCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon
            Box(
                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp))
                    .background(cardColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (isSelesai) Icons.AutoMirrored.Filled.List else Icons.Default.Add,
                    contentDescription = null, modifier = Modifier.size(18.dp),
                    tint = cardColor,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))

            // Info column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Kas ${formatTanggal.format(Date(shift.waktuBuka))}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Saldo: ${shift.saldoAwal.nilaiRupiah.sebagaiRupiah()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    )
                    Text(
                        text = durasi,
                        style = MaterialTheme.typography.labelSmall,
                        color = cardColor.copy(alpha = 0.8f),
                    )
                }
            }

            // Badge
            FlexiBadge(
                text = if (isSelesai) "Selesai" else "Aktif",
                color = cardColor,
            )
        }
    }
}

// ═══════════════════════════════════════
// SETORAN CARD — Premium
// ═══════════════════════════════════════

@Composable
internal fun SetoranCard(
    setoran: Setoran,
    formatTanggal: SimpleDateFormat,
    onHapus: () -> Unit,
    onEdit: () -> Unit,
) {
    val isDibatalkan = setoran.dihapus
    val cardColor = if (isDibatalkan) RedAksen.copy(alpha = 0.5f) else AmberAksen
    var tampilDialogKonfirmasi by androidx.compose.runtime.remember { mutableStateOf(false) }

    FlexiCard(onClick = if (isDibatalkan) ({}) else onEdit) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp))
                    .background(if (isDibatalkan) RedAksen.copy(alpha = 0.08f) else AmberAksen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (isDibatalkan) Icons.Default.Delete else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = cardColor,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isDibatalkan) "Setoran Dibatalkan" else "Setoran ${setoran.nominal.nilaiRupiah.sebagaiRupiah()}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (isDibatalkan) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                    ),
                    color = if (isDibatalkan) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (setoran.catatan.isNotBlank()) {
                        Text(
                            text = setoran.catatan,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDibatalkan) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        )
                    }
                    Text(
                        text = formatTanggal.format(Date(setoran.waktu)),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDibatalkan) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                    if (isDibatalkan) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        )
                        id.flexi.kasir.ui.component.FlexiBadge(
                            text = "Dibatalkan",
                            color = RedAksen,
                        )
                    }
                }
            }
            if (!isDibatalkan) {
                IconButton(
                    onClick = { tampilDialogKonfirmasi = true },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Batalkan",
                        modifier = Modifier.size(16.dp),
                        tint = RedAksen.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }

    if (tampilDialogKonfirmasi) {
        id.flexi.kasir.ui.component.FlexiDialog(onDismissRequest = { tampilDialogKonfirmasi = false }) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                id.flexi.kasir.ui.component.FlexiDialogHeader(
                    icon = Icons.Default.Delete,
                    title = "Batalkan Setoran",
                    subtitle = "Setoran ${setoran.nominal.nilaiRupiah.sebagaiRupiah()} akan ditandai dibatalkan.",
                    onClose = { tampilDialogKonfirmasi = false },
                )
                id.flexi.kasir.ui.component.FlexiDialogWarningPanel(
                    message = "Setoran yang dibatalkan tidak akan dihitung dalam total. Tindakan ini tidak bisa dibatalkan.",
                )
                id.flexi.kasir.ui.component.FlexiDialogActions(
                    onBatal = { tampilDialogKonfirmasi = false },
                    onKonfirmasi = {
                        tampilDialogKonfirmasi = false
                        onHapus()
                    },
                    labelKonfirmasi = "Ya, Batalkan",
                )
            }
        }
    }
}

// ═══════════════════════════════════════
// UTILITY COMPOSABLES
// ═══════════════════════════════════════

@Composable
internal fun InfoItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
    }
}

@Composable
internal fun InfoBarisKas(label: String, value: String, tintColor: Color = MaterialTheme.colorScheme.onPrimary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = tintColor.copy(alpha = 0.8f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = tintColor,
        )
    }
}

@Composable
internal fun TutupBerhasilContent(
    saatKembali: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.AutoMirrored.Filled.List,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = GreenAksen,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Kas berhasil ditutup", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = saatKembali, shape = RoundedCornerShape(12.dp)) {
                Text("Kembali", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ═══════════════════════════════════════
// DETAIL KAS — Full screen page
// ═══════════════════════════════════════

@Composable
internal fun DetailKasPage(
    shift: CashKas,
    saldoSaatIni: String? = null,
    penjualanTunai: String? = null,
    penjualanQRIS: String? = null,
    penjualanTotal: String? = null,
    totalPemasukan: String? = null,
    totalPengeluaran: String? = null,
    daftarMutasi: List<id.flexi.kasir.domain.model.CashMutation>? = null,
    daftarTransaksi: List<id.flexi.kasir.domain.model.Transaction>? = null,
    onTutupKas: (() -> Unit)? = null,
    saatKembali: () -> Unit,
    saatExportPdf: (Uri) -> Unit = {},
    sedangExport: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val fmt = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) }
    val isAktif = shift.saldoAkhir == null
    val durasi = remember(shift) {
        val mulai = shift.waktuBuka
        val akhir = shift.waktuTutup ?: System.currentTimeMillis()
        val selisihMs = akhir - mulai
        val jam = selisihMs / 3_600_000
        val menit = (selisihMs % 3_600_000) / 60_000
        if (jam > 0) "${jam}j ${menit}m" else "${menit}m"
    }

    var tampilDialogMutasi by androidx.compose.runtime.remember { mutableStateOf(false) }
    var tampilDialogPenjualan by androidx.compose.runtime.remember { mutableStateOf(false) }

    val launcherExportPdf = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
        onResult = { uri ->
            if (uri != null) {
                saatExportPdf(uri)
            }
        },
    )

    val heroColor = if (isAktif) BlueAksen else GreenAksen

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        FlexiTopAppBar(
            title = "Detail Kas",
            saatKembali = saatKembali,
        )

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // ── Hero Card — compact ──
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = heroColor),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = if (isAktif) Icons.Default.Add else Icons.AutoMirrored.Filled.List,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = Color.White,
                                    )
                                }
                                Column {
                                    Text(
                                        text = if (isAktif) "Kas Aktif" else "Kas Selesai",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = Color.White,
                                    )
                                    Text(
                                        text = durasi,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.7f),
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.2f),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color.White),
                                    )
                                    Text(
                                        text = if (isAktif) "Aktif" else "Selesai",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = Color.White,
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isAktif) "Saldo Saat Ini" else "Saldo Akhir",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f),
                            )
                            Text(
                                text = if (isAktif) {
                                    saldoSaatIni ?: penjualanTotal ?: shift.saldoAwal.nilaiRupiah.sebagaiRupiah()
                                } else {
                                    shift.saldoAkhir.nilaiRupiah.sebagaiRupiah()
                                },
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = Color.White,
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            DateChip("Dibuka", fmt.format(Date(shift.waktuBuka)))
                            if (shift.waktuTutup != null) {
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(24.dp)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                )
                            }
                            if (shift.waktuTutup != null) {
                                DateChip("Ditutup", fmt.format(Date(shift.waktuTutup)))
                            }
                        }
                    }
                }
            }

            // ── Stat Cards ──
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        DetailStatBox(
                            label = "Penjualan Tunai",
                            value = penjualanTunai ?: "Rp0",
                            color = GreenAksen,
                            modifier = Modifier.weight(1f),
                        )
                        DetailStatBox(
                            label = "Penjualan QRIS",
                            value = penjualanQRIS ?: "Rp0",
                            color = BlueAksen,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (penjualanTotal != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                            tonalElevation = 0.dp,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "Total Penjualan",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                )
                                Text(
                                    penjualanTotal,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        DetailStatBox(
                            label = "Pemasukan",
                            value = totalPemasukan ?: "Rp0",
                            color = GreenAksen,
                            modifier = Modifier.weight(1f),
                        )
                        DetailStatBox(
                            label = "Pengeluaran",
                            value = totalPengeluaran ?: "Rp0",
                            color = RedAksen,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // ── Info Kas ──
            item {
                FlexiCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Info Kas",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        )
                        HorizontalDivider()
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            InfoItem("Saldo Awal", shift.saldoAwal.nilaiRupiah.sebagaiRupiah())
                            if (shift.saldoAkhir != null) {
                                InfoItem("Saldo Akhir", shift.saldoAkhir.nilaiRupiah.sebagaiRupiah())
                            }
                            if (shift.catatanBuka?.isNotBlank() == true) {
                                InfoItem("Catatan Buka", shift.catatanBuka)
                            }
                            if (shift.catatanTutup?.isNotBlank() == true) {
                                InfoItem("Catatan Tutup", shift.catatanTutup)
                            }
                        }
                    }
                }
            }

            // ── Actions ──
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OutlinedButton(
                            onClick = { tampilDialogPenjualan = true },
                            enabled = !daftarTransaksi.isNullOrEmpty(),
                            modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Riwayat Penjualan (${daftarTransaksi?.size ?: 0})", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium)
                        }
                        OutlinedButton(
                            onClick = { tampilDialogMutasi = true },
                            enabled = !daftarMutasi.isNullOrEmpty(),
                            modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pengeluaran (${daftarMutasi?.size ?: 0})", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    OutlinedButton(
                        onClick = { launcherExportPdf.launch("Detail_Kas_${fmt.format(Date(shift.waktuBuka)).replace(" ", "_").replace(",", "")}.pdf") },
                        enabled = !sedangExport,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        if (sedangExport) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mengexport...", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium)
                        } else {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export PDF", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // ── Tutup Kas ──
            if (isAktif && onTutupKas != null) {
                item {
                    Button(
                        onClick = { onTutupKas() },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RedAksen,
                            contentColor = Color.White,
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Tutup Kas",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(4.dp)) }
        }

        if (tampilDialogPenjualan && !daftarTransaksi.isNullOrEmpty()) {
            DialogRiwayatPenjualan(
                daftarTransaksi = daftarTransaksi,
                fmt = fmt,
                onTutup = { tampilDialogPenjualan = false },
            )
        }
        if (tampilDialogMutasi && !daftarMutasi.isNullOrEmpty()) {
            DialogRiwayatMutasi(
                daftarMutasi = daftarMutasi,
                fmt = fmt,
                onTutup = { tampilDialogMutasi = false },
            )
        }
    }
}

@Composable
private fun DateChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White,
        )
    }
}

// ═══════════════════════════════════════
// DETAIL STAT BOX
// ═══════════════════════════════════════

@Composable
private fun DialogRiwayatMutasi(
    daftarMutasi: List<id.flexi.kasir.domain.model.CashMutation>,
    fmt: java.text.SimpleDateFormat,
    onTutup: () -> Unit,
) {
    FlexiDialog(onDismissRequest = onTutup) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FlexiDialogHeader(
                icon = Icons.AutoMirrored.Filled.List, title = "Pengeluaran",
                subtitle = "${daftarMutasi.size} mutasi tercatat",
                onClose = onTutup,
            )

            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(daftarMutasi) { mutasi ->
                    val mutasiColor = if (mutasi.tipe == CashMutationType.Pemasukan) GreenAksen else RedAksen
                    FlexiCard {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                                    .background(mutasiColor.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    if (mutasi.tipe == CashMutationType.Pemasukan) Icons.Default.Add else Icons.Default.Delete,
                                    contentDescription = null, modifier = Modifier.size(16.dp),
                                    tint = mutasiColor,
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mutasi.catatan.ifBlank {
                                        if (mutasi.tipe == CashMutationType.Pemasukan) "Pemasukan" else "Pengeluaran"
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                )
                                Text(
                                    text = fmt.format(Date(mutasi.waktu)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                text = buildString {
                                    if (mutasi.tipe == CashMutationType.Pemasukan) append("+")
                                    else append("-")
                                    append(mutasi.nominal.nilaiRupiah.sebagaiRupiah())
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = mutasiColor,
                            )
                        }
                    }
                }
            }

            FlexiDialogSingleAction(
                label = "Tutup",
                onClick = onTutup,
            )
        }
    }
}

@Composable
private fun DialogRiwayatPenjualan(
    daftarTransaksi: List<Transaction>,
    fmt: java.text.SimpleDateFormat,
    onTutup: () -> Unit,
) {
    FlexiDialog(onDismissRequest = onTutup) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FlexiDialogHeader(
                icon = Icons.Default.ShoppingCart, title = "Riwayat Penjualan",
                subtitle = "${daftarTransaksi.size} transaksi tercatat",
                onClose = onTutup,
            )

            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(daftarTransaksi) { transaksi ->
                    val isCash = transaksi.paymentMethod == PaymentMethod.Cash
                    val metodeColor = if (isCash) GreenAksen else BlueAksen
                    FlexiCard {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                                    .background(metodeColor.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Default.ShoppingCart,
                                    contentDescription = null, modifier = Modifier.size(16.dp),
                                    tint = metodeColor,
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "#${transaksi.nomorAntrian ?: transaksi.id.take(6)}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(${transaksi.daftarCartItem.size} item)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(
                                    text = fmt.format(Date(transaksi.waktuTransactionEpochMili)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = if (transaksi.paymentMethod == PaymentMethod.Cash) "Tunai" else "QRIS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = metodeColor,
                                )
                            }
                            Text(
                                text = transaksi.hitungTotalAkhirTransaction().sebagaiRupiah(),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = if (transaksi.dibatalkan) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        if (transaksi.dibatalkan) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.error,
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Dibatalkan${transaksi.alasanPembatalan?.let { ": $it" } ?: ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }

            val totalTunai = remember(daftarTransaksi) {
                daftarTransaksi.filter { it.paymentMethod == PaymentMethod.Cash && !it.dibatalkan }
                    .sumOf { it.hitungTotalAkhirTransaction() }
            }
            val totalQRIS = remember(daftarTransaksi) {
                daftarTransaksi.filter { it.paymentMethod == PaymentMethod.Qris && !it.dibatalkan }
                    .sumOf { it.hitungTotalAkhirTransaction() }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = GreenAksen.copy(alpha = 0.06f),
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Tunai", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(totalTunai.sebagaiRupiah(), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = GreenAksen)
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = BlueAksen.copy(alpha = 0.06f),
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("QRIS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(totalQRIS.sebagaiRupiah(), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = BlueAksen)
                    }
                }
            }

            FlexiDialogSingleAction(
                label = "Tutup",
                onClick = onTutup,
            )
        }
    }
}

@Composable
private fun DetailStatBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.06f),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = color,
            )
        }
    }
}

// ═══════════════════════════════════════
// SEKSI REKENING — saldo & mutasi rekening
// ═══════════════════════════════════════

@Composable
internal fun SeksiRekening(
    state: RekeningUiState,
    bukaDialogSaldoAwal: () -> Unit,
    bukaDialogMutasi: (MutasiRekeningTipe) -> Unit,
    tutupDialogSaldoAwal: () -> Unit,
    perbaruiNominalSaldoAwal: (String) -> Unit,
    perbaruiCatatanSaldoAwal: (String) -> Unit,
    simpanSaldoAwal: () -> Unit,
    tutupDialogMutasi: () -> Unit,
    perbaruiNominalMutasi: (String) -> Unit,
    perbaruiCatatanMutasi: (String) -> Unit,
    simpanMutasi: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val formatTanggal = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) }

    Column(modifier = modifier.fillMaxWidth()) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Saldo Rekening",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    FilledTonalButton(
                        onClick = bukaDialogSaldoAwal,
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Saldo Awal", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = state.saldoAkhir.sebagaiRupiah(),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilledTonalButton(
                        onClick = { bukaDialogMutasi(MutasiRekeningTipe.Pemasukan) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pemasukan", fontWeight = FontWeight.Medium)
                    }
                    FilledTonalButton(
                        onClick = { bukaDialogMutasi(MutasiRekeningTipe.Penarikan) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Penarikan", fontWeight = FontWeight.Medium)
                    }
                }

                if (state.daftarMutasi.isNotEmpty()) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                    state.daftarMutasi.take(8).forEach { mutasi ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(
                                    text = labelMutasiRekening(mutasi.tipe),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                if (mutasi.catatan.isNotBlank()) {
                                    Text(
                                        text = mutasi.catatan,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Text(
                                    text = formatTanggal.format(Date(mutasi.waktu)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            val menambah = mutasi.tipe != MutasiRekeningTipe.Penarikan
                            Text(
                                text = if (menambah) "+${mutasi.nominal.nilaiRupiah.sebagaiRupiah()}" else "-${mutasi.nominal.nilaiRupiah.sebagaiRupiah()}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (menambah) GreenAksen else RedAksen,
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Belum ada mutasi rekening. Atur saldo awal untuk mulai mencatat.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }
        }
    }

    if (state.apakahDialogSaldoAwalTampil) {
        FlexiDialog(onDismissRequest = tutupDialogSaldoAwal) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FlexiDialogHeader(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "Atur Saldo Awal",
                    subtitle = "Menetapkan modal awal rekening",
                    onClose = tutupDialogSaldoAwal,
                )

                OutlinedTextField(
                    value = state.nominalSaldoAwal,
                    onValueChange = { perbaruiNominalSaldoAwal(it) },
                    label = { Text("Nominal") },
                    placeholder = { Text("Contoh: 1000000") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = state.catatanSaldoAwal,
                    onValueChange = { perbaruiCatatanSaldoAwal(it) },
                    label = { Text("Catatan (opsional)") },
                    placeholder = { Text("Contoh: Saldo awal bulan ini") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth(),
                )

                FlexiDialogActions(
                    onBatal = tutupDialogSaldoAwal,
                    labelBatal = "Batal",
                    onKonfirmasi = simpanSaldoAwal,
                    labelKonfirmasi = "Simpan",
                    konfirmasiIcon = Icons.Default.Add,
                    enabled = state.nominalSaldoAwal.isNotBlank() && !state.apakahSedangMenyimpan,
                )
            }
        }
    }

    if (state.apakahDialogMutasiTampil) {
        val isPemasukan = state.tipeMutasi == MutasiRekeningTipe.Pemasukan
        FlexiDialog(onDismissRequest = tutupDialogMutasi) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FlexiDialogHeader(
                    icon = if (isPemasukan) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    title = if (isPemasukan) "Pemasukan" else "Penarikan",
                    subtitle = "Catat mutasi rekening",
                    onClose = tutupDialogMutasi,
                    iconTint = if (isPemasukan) GreenAksen else RedAksen,
                )

                OutlinedTextField(
                    value = state.nominalMutasi,
                    onValueChange = { perbaruiNominalMutasi(it) },
                    label = { Text("Nominal") },
                    placeholder = { Text("Contoh: 50000") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = state.catatanMutasi,
                    onValueChange = { perbaruiCatatanMutasi(it) },
                    label = { Text("Catatan (opsional)") },
                    placeholder = { Text("Contoh: Transfer dari kas") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth(),
                )

                FlexiDialogActions(
                    onBatal = tutupDialogMutasi,
                    labelBatal = "Batal",
                    onKonfirmasi = simpanMutasi,
                    labelKonfirmasi = "Simpan",
                    konfirmasiIcon = Icons.Default.Add,
                    enabled = state.nominalMutasi.isNotBlank() && !state.apakahSedangMenyimpan,
                )
            }
        }
    }
}

private fun labelMutasiRekening(tipe: MutasiRekeningTipe): String = when (tipe) {
    MutasiRekeningTipe.SaldoAwal -> "Saldo Awal"
    MutasiRekeningTipe.Pemasukan -> "Pemasukan"
    MutasiRekeningTipe.Penarikan -> "Penarikan"
}
