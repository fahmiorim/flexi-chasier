package id.flexi.kasir.ui.bahan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import id.flexi.kasir.ui.component.FlexiCard
import id.flexi.kasir.ui.component.FlexiDialog
import id.flexi.kasir.ui.component.FlexiDialogActions
import id.flexi.kasir.ui.component.FlexiDialogHeader
import id.flexi.kasir.ui.component.FlexiDialogWarningPanel
import id.flexi.kasir.ui.component.FlexiTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.flexi.kasir.domain.util.sebagaiRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BahanManagementScreen(
    viewModel: BahanViewModel,
    navigasiKembali: () -> Unit,
    navigasiKeTambah: () -> Unit,
    navigasiKeDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.amatiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        floatingActionButton = {
            FloatingActionButton(
                onClick = navigasiKeTambah,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Bahan")
            }
        },
    ) { bantalan ->
        Column(
            modifier = Modifier
                .padding(bantalan)
                .fillMaxSize(),
        ) {
            FlexiTopAppBar(
                title = "Bahan Baku",
                saatKembali = navigasiKembali,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )

            KotakPencarianBahan(
                kataKunci = state.kataKunciPencarian,
                onPerbaruiKataKunci = { viewModel.tanganiAksi(BahanAction.PerbaruiKataKunciPencarian(it)) },
                onReset = { viewModel.tanganiAksi(BahanAction.ResetPencarian) },
            )

            if (state.apakahSedangMemuat) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.daftarBahan.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Belum ada bahan baku. Ketuk + untuk menambah.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                DaftarBahanKelola(
                    daftarBahan = state.daftarBahan,
                    onBukaDetail = navigasiKeDetail,
                    onMintaHapus = { id, nama ->
                        viewModel.tanganiAksi(BahanAction.MintaHapus(id, nama))
                    },
                )
            }
        }

        if (state.statusKonfirmasiHapus.apakahTampil) {
            DialogKonfirmasiHapusBahan(
                status = state.statusKonfirmasiHapus,
                onKonfirmasi = { viewModel.tanganiAksi(BahanAction.KonfirmasiHapus) },
                onBatal = { viewModel.tanganiAksi(BahanAction.BatalkanHapus) },
            )
        }
    }
}

@Composable
fun KotakPencarianBahan(
    kataKunci: String,
    onPerbaruiKataKunci: (String) -> Unit,
    onReset: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = kataKunci,
            onValueChange = onPerbaruiKataKunci,
            modifier = Modifier.weight(1f).heightIn(max = 34.dp),
            placeholder = { Text("Cari bahan baku...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (kataKunci.isNotEmpty()) {
                    IconButton(onClick = onReset) {
                        Icon(Icons.Default.Clear, contentDescription = "Bersihkan")
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.small,
        )
    }
}

@Composable
fun DaftarBahanKelola(
    daftarBahan: List<id.flexi.kasir.domain.model.Bahan>,
    onBukaDetail: (String) -> Unit,
    onMintaHapus: (String, String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(daftarBahan, key = { it.id }) { bahan ->
            ItemBahanKelola(
                bahan = bahan,
                onBukaDetail = { onBukaDetail(bahan.id) },
                onMintaHapus = { onMintaHapus(bahan.id, bahan.nama) },
            )
        }
    }
}

@Composable
fun ItemBahanKelola(
    bahan: id.flexi.kasir.domain.model.Bahan,
    onBukaDetail: () -> Unit,
    onMintaHapus: () -> Unit,
) {
    FlexiCard(
        onClick = onBukaDetail,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bahan.nama,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.size(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Stok: ${bahan.stokTersedia} ${bahan.satuan}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (bahan.hargaPerSatuan > 0) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = bahan.hargaPerSatuan.sebagaiRupiah() + "/${bahan.satuan}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            IconButton(onClick = onMintaHapus) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Hapus bahan",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
fun DialogKonfirmasiHapusBahan(
    status: StatusKonfirmasiHapusBahan,
    onKonfirmasi: () -> Unit,
    onBatal: () -> Unit,
) {
    FlexiDialog(onDismissRequest = onBatal) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FlexiDialogHeader(
                icon = Icons.Default.Delete,
                title = "Hapus Bahan?",
                subtitle = "Tindakan ini tidak dapat dibatalkan",
                onClose = onBatal,
                iconTint = MaterialTheme.colorScheme.error,
            )

            FlexiDialogWarningPanel(
                message = "Apakah Anda yakin ingin menghapus '${status.namaBahan}'? Bahan yang sudah memiliki resep tidak bisa dihapus.",
            )

            FlexiDialogActions(
                onBatal = onBatal,
                labelBatal = "Batal",
                onKonfirmasi = onKonfirmasi,
                labelKonfirmasi = "Hapus",
                konfirmasiColor = MaterialTheme.colorScheme.error,
                konfirmasiIcon = Icons.Default.Delete,
            )
        }
    }
}
