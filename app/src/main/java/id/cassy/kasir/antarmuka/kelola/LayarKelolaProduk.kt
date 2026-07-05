package id.cassy.kasir.antarmuka.kelola

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.cassy.kasir.ranah.model.Produk

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayarKelolaProduk(
    viewModel: ViewModelKelolaProduk,
    navigasiKembali: () -> Unit,
    navigasiKeTambahProduk: () -> Unit,
    navigasiKeUbahProduk: (String) -> Unit,
) {
    val state by viewModel.amatiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.judulLayar) },
                navigationIcon = {
                    IconButton(onClick = navigasiKembali) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = navigasiKeTambahProduk) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Produk")
            }
        }
    ) { bantalan ->
        Column(
            modifier = Modifier
                .padding(bantalan)
                .fillMaxSize()
        ) {
            KotakPencarianProduk(
                kataKunci = state.kataKunciPencarian,
                onPerbaruiKataKunci = { viewModel.tanganiAksi(AksiLayarKelolaProduk.PerbaruiKataKunciPencarian(it)) },
                onReset = { viewModel.tanganiAksi(AksiLayarKelolaProduk.ResetPencarian) }
            )

            if (state.apakahSedangMemuat) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                DaftarProdukKelola(
                    daftarProduk = state.daftarProduk,
                    onUbah = navigasiKeUbahProduk,
                    onHapus = { id, nama ->
                        viewModel.tanganiAksi(AksiLayarKelolaProduk.MintaHapusProduk(id, nama))
                    }
                )
            }
        }

        if (state.statusKonfirmasiHapus.apakahTampil) {
            DialogKonfirmasiHapus(
                status = state.statusKonfirmasiHapus,
                onKonfirmasi = { viewModel.tanganiAksi(AksiLayarKelolaProduk.KonfirmasiHapusProduk) },
                onBatal = { viewModel.tanganiAksi(AksiLayarKelolaProduk.BatalkanHapusProduk) }
            )
        }
    }
}

@Composable
fun KotakPencarianProduk(
    kataKunci: String,
    onPerbaruiKataKunci: (String) -> Unit,
    onReset: () -> Unit,
) {
    OutlinedTextField(
        value = kataKunci,
        onValueChange = onPerbaruiKataKunci,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text("Cari nama atau ID produk...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (kataKunci.isNotEmpty()) {
                IconButton(onClick = onReset) {
                    Icon(Icons.Default.Clear, contentDescription = "Bersihkan")
                }
            }
        },
        singleLine = true
    )
}

@Composable
fun DaftarProdukKelola(
    daftarProduk: List<Produk>,
    onUbah: (String) -> Unit,
    onHapus: (String, String) -> Unit,
) {
    if (daftarProduk.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tidak ada produk ditemukan.", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(daftarProduk, key = { it.id }) { produk ->
                ItemProdukKelola(
                    produk = produk,
                    onUbah = { onUbah(produk.id) },
                    onHapus = { onHapus(produk.id, produk.nama) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
fun ItemProdukKelola(
    produk: Produk,
    onUbah: () -> Unit,
    onHapus: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(produk.nama) },
        supportingContent = {
            Column {
                Text("Rp ${produk.harga} | Stok: ${produk.stokTersedia} | ${produk.kategori.ifBlank { "-" }}")
                Text("ID: ${produk.id}", style = MaterialTheme.typography.labelSmall)
            }
        },
        trailingContent = {
            Column {
                IconButton(onClick = onUbah) {
                    Icon(Icons.Default.Edit, contentDescription = "Ubah", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onHapus) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}

@Composable
fun DialogKonfirmasiHapus(
    status: StatusKonfirmasiHapusProduk,
    onKonfirmasi: () -> Unit,
    onBatal: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onBatal,
        title = { Text(status.judul) },
        text = { Text(status.deskripsi) },
        confirmButton = {
            TextButton(onClick = onKonfirmasi) {
                Text("Hapus", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onBatal) {
                Text("Batal")
            }
        }
    )
}
