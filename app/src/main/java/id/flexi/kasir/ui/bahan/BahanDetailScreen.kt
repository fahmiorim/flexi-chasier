package id.flexi.kasir.ui.bahan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import id.flexi.kasir.ui.component.FlexiCard
import id.flexi.kasir.ui.component.FlexiDialog
import id.flexi.kasir.ui.component.FlexiDialogActions
import id.flexi.kasir.ui.component.FlexiDialogHeader
import id.flexi.kasir.ui.component.FlexiDialogWarningPanel
import id.flexi.kasir.ui.component.FlexiTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.flexi.kasir.domain.model.Bahan
import id.flexi.kasir.domain.model.PembelianBahan
import id.flexi.kasir.domain.util.sebagaiRupiah
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BahanDetailScreen(
    viewModel: BahanDetailViewModel,
    navigasiKembali: () -> Unit,
    navigasiKeUbah: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.amatiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.pesanSnackbar) {
        state.pesanSnackbar?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.bersihkanPesan()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { bantalan ->
        if (state.apakahSedangMemuat || state.bahan == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bantalan),
                contentAlignment = Alignment.Center,
            ) {
                if (state.apakahSedangMemuat) {
                    CircularProgressIndicator()
                } else {
                    Text("Bahan tidak ditemukan.", style = MaterialTheme.typography.bodyLarge)
                }
            }            } else {
                val bahan = state.bahan!!
            Column(
                modifier = Modifier
                    .padding(bantalan)
                    .fillMaxSize(),
            ) {
                // Top bar
                FlexiTopAppBar(
                    title = bahan.nama,
                    saatKembali = navigasiKembali,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Info Bahan Card
                    KartuInfoBahan(bahan = bahan)

                    // Tombol Aksi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilledTonalButton(
                            onClick = { navigasiKeUbah(bahan.id) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ubah", fontWeight = FontWeight.Medium)
                        }
                        FilledTonalButton(
                            onClick = { viewModel.bukaDialogTambahPembelian() },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pembelian", fontWeight = FontWeight.Medium)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Riwayat Pembelian Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Riwayat Pembelian",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    if (state.daftarPembelian.isEmpty()) {
                        Text(
                            text = "Belum ada pembelian. Ketuk tombol 'Pembelian' untuk mencatat.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    } else {
                        state.daftarPembelian.forEach { pembelian ->
                            KartuPembelian(
                                pembelian = pembelian,
                                onHapus = { viewModel.mintaHapusPembelian(pembelian) },
                            )
                        }
                    }
                }
            }
        }

        // Dialog Tambah Pembelian
        if (state.apakahDialogTambahPembelianTampil) {
            DialogTambahPembelian(
                onDismiss = { viewModel.tutupDialogTambahPembelian() },
                onSimpan = { jumlah, satuan, total, catatan ->
                    viewModel.simpanPembelian(jumlah, satuan, total, catatan)
                },
            )
        }

        // Dialog Konfirmasi Hapus Pembelian
        if (state.statusHapusPembelian.apakahTampil) {
            FlexiDialog(
                onDismissRequest = { viewModel.batalkanHapusPembelian() },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    FlexiDialogHeader(
                        icon = Icons.Default.Delete,
                        title = "Hapus Pembelian?",
                        subtitle = "Stok bahan akan dikurangi",
                        onClose = { viewModel.batalkanHapusPembelian() },
                        iconTint = MaterialTheme.colorScheme.error,
                    )

                    FlexiDialogWarningPanel(
                        message = "Pembelian ${state.statusHapusPembelian.totalHarga.sebagaiRupiah()} (${state.statusHapusPembelian.jumlah} ${state.statusHapusPembelian.satuan}) akan dihapus dan stok bahan akan dikurangi.",
                    )

                    FlexiDialogActions(
                        onBatal = { viewModel.batalkanHapusPembelian() },
                        labelBatal = "Batal",
                        onKonfirmasi = { viewModel.konfirmasiHapusPembelian() },
                        labelKonfirmasi = "Hapus",
                        konfirmasiColor = MaterialTheme.colorScheme.error,
                        konfirmasiIcon = Icons.Default.Delete,
                    )
                }
            }
        }
    }
}

@Composable
private fun KartuInfoBahan(bahan: Bahan) {
    FlexiCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BarisInfo("Satuan", bahan.satuan)
            BarisInfo("Stok Tersedia", "${bahan.stokTersedia} ${bahan.satuan}")
            BarisInfo(
                "Harga Per Satuan",
                if (bahan.hargaPerSatuan > 0) "${bahan.hargaPerSatuan.sebagaiRupiah()}/${bahan.satuan}" else "Belum ditentukan",
            )
        }
    }
}

@Composable
private fun BarisInfo(label: String, nilai: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = nilai,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun KartuPembelian(
    pembelian: PembelianBahan,
    onHapus: () -> Unit,
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID")) }

    FlexiCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${pembelian.jumlah} ${pembelian.satuanBeli}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "@ ${pembelian.totalHarga.sebagaiRupiah()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateFormat.format(Date(pembelian.tanggalBeli)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!pembelian.catatan.isNullOrBlank()) {
                    Text(
                        text = pembelian.catatan,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            IconButton(onClick = onHapus, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Hapus pembelian",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun DialogTambahPembelian(
    onDismiss: () -> Unit,
    onSimpan: (jumlah: String, satuan: String, total: String, catatan: String) -> Unit,
) {
    var jumlah by remember { mutableStateOf("") }
    var satuanBeli by remember { mutableStateOf("") }
    var totalHarga by remember { mutableStateOf("") }
    var catatan by remember { mutableStateOf("") }

    FlexiDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FlexiDialogHeader(
                icon = Icons.Default.AddShoppingCart,
                title = "Tambah Pembelian",
                onClose = onDismiss,
            )

                OutlinedTextField(
                    value = jumlah,
                    onValueChange = { 
                        val bersih = it.fold("") { acc, c ->
                            when {
                                c.isDigit() -> acc + c
                                c == '.' && '.' !in acc -> acc + c
                                else -> acc
                            }
                        }
                        jumlah = bersih 
                    },
                    label = { Text("Jumlah") },
                    placeholder = { Text("Contoh: 1, 500, 24") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = satuanBeli,
                    onValueChange = { satuanBeli = it },
                    label = { Text("Satuan Beli") },
                    placeholder = { Text("Contoh: bungkus, dus, kg") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = totalHarga,
                    onValueChange = { if (it.all { c -> c.isDigit() }) totalHarga = it },
                    label = { Text("Total Harga (Rp)") },
                    placeholder = { Text("Contoh: 47000") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = catatan,
                    onValueChange = { catatan = it },
                    label = { Text("Catatan (opsional)") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth(),
                )

                FlexiDialogActions(
                    onBatal = onDismiss,
                    labelBatal = "Batal",
                    onKonfirmasi = {
                        if (jumlah.toDoubleOrNull() != null && totalHarga.toLongOrNull() != null) {
                            onSimpan(jumlah, satuanBeli, totalHarga, catatan)
                        }
                    },
                    labelKonfirmasi = "Simpan",
                    konfirmasiIcon = Icons.Default.Add,
                    enabled = jumlah.toDoubleOrNull() != null && totalHarga.toLongOrNull() != null,
                )
        }
    }
}
