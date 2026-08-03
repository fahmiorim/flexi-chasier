package id.flexi.kasir.ui.resep

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import id.flexi.kasir.ui.component.FlexiCard
import id.flexi.kasir.ui.component.FlexiDialog
import id.flexi.kasir.ui.component.FlexiDialogHeader
import id.flexi.kasir.ui.component.FlexiTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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
import id.flexi.kasir.domain.model.BahanResep
import id.flexi.kasir.domain.model.Produk
import id.flexi.kasir.domain.model.Resep
import id.flexi.kasir.domain.util.sebagaiRupiah
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResepScreen(
    viewModel: ResepViewModel,
    navigasiKembali: () -> Unit,
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
        Column(
            modifier = Modifier
                .padding(bantalan)
                .fillMaxSize(),
        ) {
            FlexiTopAppBar(
                title = "Atur Resep",
                saatKembali = navigasiKembali,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )

            // Search
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = state.kataKunciPencarian,
                    onValueChange = { viewModel.perbaruiKataKunci(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Cari produk...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (state.kataKunciPencarian.isNotEmpty()) {
                            IconButton(onClick = { viewModel.resetPencarian() }) {
                                Icon(Icons.Default.Clear, contentDescription = "Bersihkan")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )
            }

            if (state.apakahSedangMemuat) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.daftarProduk.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Tidak ada produk.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.daftarProduk, key = { it.produk.id }) { item ->
                        ItemProdukResep(
                            produk = item.produk,
                            apakahPunyaResep = item.apakahPunyaResep,
                            onClick = { viewModel.bukaEditResep(item.produk) },
                        )
                    }
                }
            }
        }

        val produkTerpilih = state.produkTerpilih
        if (state.apakahDialogEditTampil && produkTerpilih != null) {
            DialogEditResep(
                produk = produkTerpilih,
                resep = state.resepSaatIni,
                daftarBahan = state.daftarBahan,
                onDismiss = { viewModel.tutupDialogEdit() },
                onSimpan = { daftarBahan -> viewModel.simpanResep(daftarBahan) },
                onHapus = { viewModel.hapusResep() },
            )
        }
    }
}

@Composable
private fun ItemProdukResep(
    produk: Produk,
    apakahPunyaResep: Boolean,
    onClick: () -> Unit,
) {
    FlexiCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = produk.nama,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (produk.kategori.isNotBlank()) {
                    Text(
                        text = produk.kategori,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (apakahPunyaResep) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = "Resep",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = "Belum diatur",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

data class BahanResepInput(
    val bahanId: String,
    val namaBahan: String,
    val jumlah: String,
    val satuan: String,
)

@Composable
private fun DialogEditResep(
    produk: Produk,
    resep: Resep?,
    daftarBahan: List<Bahan>,
    onDismiss: () -> Unit,
    onSimpan: (List<BahanResep>) -> Unit,
    onHapus: () -> Unit,
) {
    val bahanResepList = remember(resep) {
        mutableStateListOf<BahanResepInput>().apply {
            if (resep != null) {
                resep.daftarBahan.forEach { br ->
                    val bahan = daftarBahan.firstOrNull { it.id == br.bahanId }
                    add(
                        BahanResepInput(
                            bahanId = br.bahanId,
                            namaBahan = bahan?.nama ?: br.bahanId,
                            jumlah = if (br.jumlah > 0) br.jumlah.toString() else "",
                            satuan = br.satuan,
                        ),
                    )
                }
            }
        }
    }

    // Hitung HPP
    // State dropdown per index — aman untuk dynamic list
    val dropdownState = remember { mutableStateMapOf<Int, Boolean>() }

    val hpp = bahanResepList.sumOf { input ->
        val jumlahVal = input.jumlah.toDoubleOrNull() ?: 0.0
        val bahan = daftarBahan.firstOrNull { it.id == input.bahanId }
        val hargaSatuan = bahan?.hargaPerSatuan ?: 0L
        (jumlahVal * hargaSatuan).roundToLong()
    }

    FlexiDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FlexiDialogHeader(
                icon = Icons.Default.MenuBook,
                title = "Resep: ${produk.nama}",
                subtitle = "HPP: ${hpp.sebagaiRupiah()}",
                onClose = onDismiss,
            )

                HorizontalDivider()

                // Daftar Bahan
                bahanResepList.forEachIndexed { index, input ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // Pilih bahan — DropdownMenu
                        val ekspansi = dropdownState[index] ?: false
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = input.namaBahan,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Bahan") },
                                placeholder = { Text("Pilih bahan...") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(onClick = { dropdownState[index] = !ekspansi }) {
                                        Icon(Icons.Default.Search, contentDescription = "Pilih")
                                    }
                                },
                            )
                            DropdownMenu(
                                expanded = ekspansi,
                                onDismissRequest = { dropdownState[index] = false },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                daftarBahan.forEach { bahan ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = bahan.nama,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                )
                                                Text(
                                                    text = "${bahan.hargaPerSatuan.sebagaiRupiah()}/${bahan.satuan}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        },
                                        onClick = {
                                            bahanResepList[index] = input.copy(
                                                bahanId = bahan.id,
                                                namaBahan = bahan.nama,
                                                satuan = bahan.satuan,
                                            )
                                            dropdownState[index] = false
                                        },
                                    )
                                }
                            }
                        }

                        // Jumlah
                        OutlinedTextField(
                            value = input.jumlah,
                            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) bahanResepList[index] = input.copy(jumlah = it) },
                            label = { Text("Jumlah") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.width(90.dp),
                        )

                        // Hapus
                        IconButton(onClick = { bahanResepList.removeAt(index) }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Tombol Tambah Bahan
                FilledTonalButton(
                    onClick = {
                        bahanResepList.add(
                            BahanResepInput(
                                bahanId = "",
                                namaBahan = "",
                                jumlah = "",
                                satuan = "",
                            ),
                        )
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambah Bahan", fontWeight = FontWeight.Medium)
                }

                HorizontalDivider()

                // Tombol Aksi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (resep != null) {
                        OutlinedButton(
                            onClick = onHapus,
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hapus", fontWeight = FontWeight.Medium)
                        }
                    }
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Batal", fontWeight = FontWeight.Medium)
                    }
                    Button(
                        onClick = {
                            val daftar = bahanResepList.mapNotNull { input ->
                                val jumlahVal = input.jumlah.toDoubleOrNull() ?: return@mapNotNull null
                                if (input.bahanId.isBlank() || jumlahVal <= 0) return@mapNotNull null
                                BahanResep(bahanId = input.bahanId, jumlah = jumlahVal, satuan = input.satuan)
                            }
                            onSimpan(daftar)
                        },
                        enabled = bahanResepList.any { it.jumlah.toDoubleOrNull() != null && (it.jumlah.toDoubleOrNull() ?: 0.0) > 0 && it.bahanId.isNotBlank() },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                }
        }
    }
}
