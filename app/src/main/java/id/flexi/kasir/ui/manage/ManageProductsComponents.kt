package id.flexi.kasir.ui.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import id.flexi.kasir.ui.component.FlexiCard
import id.flexi.kasir.ui.component.FlexiDialog
import id.flexi.kasir.ui.component.FlexiDialogActions
import id.flexi.kasir.ui.component.FlexiDialogHeader
import id.flexi.kasir.ui.component.FlexiDialogWarningPanel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.util.sebagaiRupiah

// ═══════════════════════════════════════
// PRODUCT TAB
// ═══════════════════════════════════════

@Composable
internal fun KontenProdukTab(
    state: ManageProductsUiState,
    viewModel: ManageProductsViewModel,
    navigasiKeTambahProduk: () -> Unit,
    navigasiKeUbahProduk: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        KotakPencarianProduk(
            kataKunci = state.kataKunciPencarian,
            onPerbaruiKataKunci = { viewModel.tanganiAksi(ManageProductsAction.PerbaruiKataKunciPencarian(it)) },
            onReset = { viewModel.tanganiAksi(ManageProductsAction.ResetPencarian) },
            daftarKategori = state.daftarKategori,
            kategoriFilter = state.kategoriFilter,
            onPerbaruiKategoriFilter = { viewModel.tanganiAksi(ManageProductsAction.PerbaruiKategoriFilter(it)) },
        )

        if (state.apakahSedangMemuat) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            DaftarProdukKelola(
                daftarProduk = state.daftarProduk,
                hppMap = state.hppMap,
                onUbah = navigasiKeUbahProduk,
            )
        }
    }
}

// ═══════════════════════════════════════
// CATEGORY TAB
// ═══════════════════════════════════════

@Composable
internal fun KontenKategoriTab(
    daftarKategori: List<String>,
    onTambahKategori: (String) -> Unit,
    onHapusKategori: (String) -> Unit,
) {
    var inputKategoriBaru by remember { mutableStateOf("") }
    var kategoriYangAkanDihapus by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = inputKategoriBaru,
                onValueChange = { inputKategoriBaru = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Nama kategori baru") },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
            )
            Button(
                onClick = {
                    if (inputKategoriBaru.isNotBlank()) {
                        onTambahKategori(inputKategoriBaru.trim())
                        inputKategoriBaru = ""
                    }
                },
                enabled = inputKategoriBaru.isNotBlank(),
                shape = MaterialTheme.shapes.small,
            ) {
                Text("Tambah")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (daftarKategori.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Belum ada kategori. Tambahkan kategori baru di atas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(daftarKategori, key = { it }) { kategori ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = kategori,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(onClick = { kategoriYangAkanDihapus = kategori }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus kategori",
                                    tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (kategoriYangAkanDihapus != null) {
        DialogHapusKategori(
            namaKategori = kategoriYangAkanDihapus ?: "",
            onDismiss = { kategoriYangAkanDihapus = null },
            onConfirm = {
                kategoriYangAkanDihapus?.let { onHapusKategori(it) }
                kategoriYangAkanDihapus = null
            },
        )
    }
}

@Composable
internal fun DialogHapusKategori(
    namaKategori: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    FlexiDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FlexiDialogHeader(
                icon = Icons.Default.Delete, title = "Hapus Kategori?",
                subtitle = "Tindakan ini tidak dapat dibatalkan",
                iconTint = MaterialTheme.colorScheme.error,
                onClose = onDismiss,
            )
            FlexiDialogWarningPanel(
                message = "Kategori '$namaKategori' akan dihapus dari semua produk. Lanjutkan?",
            )
            FlexiDialogActions(
                onBatal = onDismiss,
                onKonfirmasi = onConfirm,
                labelKonfirmasi = "Hapus",
                konfirmasiIcon = Icons.Default.Delete,
                konfirmasiColor = MaterialTheme.colorScheme.error,
            )
        }
    }
}

// ═══════════════════════════════════════
// SEARCH BOX
// ═══════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KotakPencarianProduk(
    kataKunci: String,
    onPerbaruiKataKunci: (String) -> Unit,
    onReset: () -> Unit,
    daftarKategori: List<String> = emptyList(),
    kategoriFilter: String = "",
    onPerbaruiKategoriFilter: (String) -> Unit = {},
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = kataKunci,
                onValueChange = onPerbaruiKataKunci,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Cari nama atau ID produk...") },
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

            if (daftarKategori.isNotEmpty()) {
                var ekspansiFilterKategori by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = ekspansiFilterKategori,
                    onExpandedChange = { ekspansiFilterKategori = it },
                ) {
                    OutlinedTextField(
                        value = if (kategoriFilter.isNotBlank()) kategoriFilter else "Semua",
                        onValueChange = {},
                        modifier = Modifier
                            .width(140.dp)
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
                        singleLine = true,
                        shape = MaterialTheme.shapes.small,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ekspansiFilterKategori) },
                        readOnly = true,
                    )
                    ExposedDropdownMenu(
                        expanded = ekspansiFilterKategori,
                        onDismissRequest = { ekspansiFilterKategori = false },
                    ) {
                        daftarKategori.forEach { kategori ->
                            DropdownMenuItem(
                                text = { Text(kategori) },
                                onClick = {
                                    onPerbaruiKategoriFilter(kategori)
                                    ekspansiFilterKategori = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════
// PRODUCT LIST
// ═══════════════════════════════════════

@Composable
internal fun DaftarProdukKelola(
    daftarProduk: List<Produk>,
    hppMap: Map<String, Long> = emptyMap(),
    onUbah: (String) -> Unit,
) {
    if (daftarProduk.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tidak ada produk ditemukan.", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(daftarProduk, key = { it.id }) { produk ->
                ItemProdukKelola(
                    produk = produk,
                    hpp = hppMap[produk.id],
                    onUbah = { onUbah(produk.id) },
                )
            }
        }
    }
}

// ═══════════════════════════════════════
// PRODUCT ITEM CARD
// ═══════════════════════════════════════

@Composable
internal fun ItemProdukKelola(
    produk: Produk,
    hpp: Long? = null,
    onUbah: () -> Unit,
) {
    val statusStokReady = !produk.apakahStokDiaktifkan || produk.stokTersedia > 5
    val statusStokMenipis = produk.apakahStokDiaktifkan && produk.stokTersedia <= 5
    val statusStokHabis = produk.apakahStokDiaktifkan && produk.stokTersedia <= 0

    val stokPillColor = when {
        statusStokHabis -> MaterialTheme.colorScheme.error
        statusStokMenipis -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.tertiary
    }

    val stokPillBg = stokPillColor.copy(alpha = 0.1f)
    val stokLabel = when {
        statusStokHabis -> "Habis"
        statusStokMenipis -> "Sisa ${produk.stokTersedia}"
        !produk.apakahStokDiaktifkan -> "Tanpa Batas"
        else -> "${produk.stokTersedia} Pcs"
    }

    FlexiCard(onClick = onUbah) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = produk.nama.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = produk.nama,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = stokPillBg,
                        contentColor = stokPillColor,
                    ) {
                        Text(
                            text = stokLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column {
                        Text(
                            text = produk.harga.sebagaiRupiah(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        if (hpp != null && hpp > 0) {
                            Text(
                                text = "HPP: ${hpp.sebagaiRupiah()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            )
                        }
                    }

                    if (produk.kategori.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ) {
                            Text(
                                text = produk.kategori,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════
// DELETE CONFIRMATION DIALOG
// ═══════════════════════════════════════

@Composable
internal fun DialogKonfirmasiHapus(
    status: StatusKonfirmasiDeleteProduct,
    onKonfirmasi: () -> Unit,
    onBatal: () -> Unit,
) {
    FlexiDialog(onDismissRequest = onBatal) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FlexiDialogHeader(
                icon = Icons.Default.Delete,
                title = status.judul,
                subtitle = "Tindakan ini tidak dapat dibatalkan",
                iconTint = MaterialTheme.colorScheme.error,
                onClose = onBatal,
            )
            FlexiDialogWarningPanel(message = status.deskripsi)
            FlexiDialogActions(
                onBatal = onBatal,
                onKonfirmasi = onKonfirmasi,
                labelKonfirmasi = "Hapus",
                konfirmasiIcon = Icons.Default.Delete,
                konfirmasiColor = MaterialTheme.colorScheme.error,
            )
        }
    }
}
