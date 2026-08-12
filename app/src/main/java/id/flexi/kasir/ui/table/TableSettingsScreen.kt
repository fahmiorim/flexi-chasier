package id.flexi.kasir.ui.table

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import id.flexi.kasir.ui.component.FlexiCard
import id.flexi.kasir.ui.component.FlexiDialog
import id.flexi.kasir.ui.component.FlexiDialogActions
import id.flexi.kasir.ui.component.FlexiDialogHeader
import id.flexi.kasir.ui.component.FlexiDialogWarningPanel
import id.flexi.kasir.ui.component.FlexiGradientButton
import id.flexi.kasir.ui.component.FlexiTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenMeja(
    state: SettingsUiStateMeja,
    saatKembali: () -> Unit,
    saatBukaSidebar: (() -> Unit)? = null,
    perbaruiJumlahBaris: (String) -> Unit,
    perbaruiJumlahKolom: (String) -> Unit,
    aturMeja: (Int, Int, String?) -> Unit,
    saatHapus: (String) -> Unit,
    saatBersihkanError: () -> Unit,
    resetSemuaStatusMeja: () -> Unit = {},
    saatSimpanGrid: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.pesanError) {
        state.pesanError?.let {
            snackbarHostState.showSnackbar(it)
            saatBersihkanError()
        }
    }

    LaunchedEffect(state.pesanSnackbar) {
        state.pesanSnackbar?.let {
            snackbarHostState.showSnackbar(it)
            saatBersihkanError()
        }
    }

    var selBaris by remember { mutableStateOf<Int?>(null) }
    var selKolom by remember { mutableStateOf<Int?>(null) }
    var nomorDialog by remember { mutableStateOf("") }

    var draftBaris by remember(state.barisStr) { mutableStateOf(state.barisStr) }
    var draftKolom by remember(state.kolomStr) { mutableStateOf(state.kolomStr) }

    val grid = remember(state.daftarMeja, state.jumlahBaris, state.jumlahKolom) {
        state.gridNomorMeja()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            FlexiTopAppBar(
                title = "Pengaturan Meja",
                saatKembali = saatKembali,
                saatBukaSidebar = saatBukaSidebar,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Layout Meja",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "Atur jumlah baris dan kolom, lalu tap sel untuk menetapkan nomor meja.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = draftBaris,
                    onValueChange = { draftBaris = it.filter { c -> c.isDigit() }.take(2) },
                    label = { Text("Baris") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = draftKolom,
                    onValueChange = { draftKolom = it.filter { c -> c.isDigit() }.take(2) },
                    label = { Text("Kolom") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                FlexiGradientButton(
                    onClick = {
                        perbaruiJumlahBaris(draftBaris)
                        perbaruiJumlahKolom(draftKolom)
                        saatSimpanGrid()
                    },
                    text = "Simpan",
                    fillWidth = false,
                    modifier = Modifier.heightIn(min = 56.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Grid peta meja
    FlexiCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    for (baris in 0..<state.jumlahBaris) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            for (kolom in 0..<state.jumlahKolom) {
                                val nomorMeja = grid[baris][kolom]
                                KotakMeja(
                                    nomor = nomorMeja,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        selBaris = baris
                                        selKolom = kolom
                                        nomorDialog = nomorMeja ?: ""
                                    },
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            var tampilKonfirmasiReset by remember { mutableStateOf(false) }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.05f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Zona Berbahaya",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Text(
                        text = "Tindakan di bawah ini bersifat merusak status transaksi meja aktif. Harap berhati-hati.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = { tampilKonfirmasiReset = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Reset Status Semua Meja",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (tampilKonfirmasiReset) {
                FlexiDialog(
                    onDismissRequest = { tampilKonfirmasiReset = false },
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        FlexiDialogHeader(
                            icon = Icons.Default.TableRestaurant,
                            title = "Reset Meja",
                            subtitle = "Tindakan ini tidak dapat dibatalkan",
                            onClose = { tampilKonfirmasiReset = false },
                            iconTint = MaterialTheme.colorScheme.error,
                        )

                        FlexiDialogWarningPanel(
                            message = "Semua meja akan direset ke status Available. Lanjutkan?",
                        )

                        FlexiDialogActions(
                            onBatal = { tampilKonfirmasiReset = false },
                            labelBatal = "Batal",
                            onKonfirmasi = {
                                resetSemuaStatusMeja()
                                tampilKonfirmasiReset = false
                            },
                            labelKonfirmasi = "Reset",
                            konfirmasiColor = MaterialTheme.colorScheme.error,
                            konfirmasiIcon = Icons.Default.Delete,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialog input nomor meja
    val barisTerpilih = selBaris
    val kolomTerpilih = selKolom
    if (barisTerpilih != null && kolomTerpilih != null) {
        val b = barisTerpilih
        val k = kolomTerpilih
        val nomorLama = grid[b][k]
        FlexiDialog(
            onDismissRequest = { selBaris = null; selKolom = null },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                FlexiDialogHeader(
                    icon = Icons.Default.TableRestaurant,
                    title = if (nomorLama != null) "Ubah Nomor Meja" else "Tambah Meja",
                    subtitle = if (nomorLama != null) "Masukkan nomor baru untuk meja ini" else "Masukkan nomor untuk meja baru",
                    onClose = { selBaris = null; selKolom = null },
                )

                OutlinedTextField(
                    value = nomorDialog,
                    onValueChange = { nomorDialog = it.filter { c -> c.isDigit() } },
                    label = { Text("Nomor Meja") },
                    placeholder = { Text("Contoh: 1, 2, 3") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )

                // Tombol
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (nomorLama != null) {
                        OutlinedButton(
                            onClick = {
                                aturMeja(b, k, null)
                                selBaris = null; selKolom = null
                            },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hapus Meja", fontWeight = FontWeight.Medium)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = { selBaris = null; selKolom = null },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text("Batal", fontWeight = FontWeight.Medium)
                        }
                        FlexiGradientButton(
                            onClick = {
                                aturMeja(b, k, nomorDialog)
                                selBaris = null; selKolom = null
                            },
                            enabled = nomorDialog.isNotBlank(),
                            text = "Simpan",
                            icon = Icons.Default.CheckCircle,
                            fillWidth = false,
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        )
                    }
                }
            }
        }
    }
}@Composable
private fun KotakMeja(
    nomor: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    if (nomor != null) {
        ElevatedCard(
            onClick = onClick,
            modifier = modifier
                .height(100.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            ),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.TableRestaurant,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Meja $nomor",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    } else {
        ElevatedCard(
            onClick = onClick,
            modifier = modifier
                .height(100.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.5.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah meja",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}
