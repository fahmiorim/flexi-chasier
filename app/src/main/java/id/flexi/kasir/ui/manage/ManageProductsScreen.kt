package id.flexi.kasir.ui.manage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import id.flexi.kasir.ui.component.FlexiTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageProductsScreen(
    viewModel: ManageProductsViewModel,
    navigasiKembali: () -> Unit,
    navigasiKeTambahProduk: () -> Unit,
    navigasiKeUbahProduk: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.amatiState.collectAsState()
    var tabTerpilih by remember { mutableStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        floatingActionButton = {
            if (tabTerpilih == 0) {
                FloatingActionButton(
                    onClick = navigasiKeTambahProduk,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Produk")
                }
            }
        },
    ) { bantalan ->
        Column(modifier = Modifier.padding(bantalan).fillMaxSize()) {
            FlexiTopAppBar(
                title = state.judulLayar,
                saatKembali = navigasiKembali,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )

            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column {
                    PrimaryTabRow(
                        selectedTabIndex = tabTerpilih,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        divider = {},
                    ) {
                        Tab(
                            selected = tabTerpilih == 0,
                            onClick = { tabTerpilih = 0 },
                            text = { Text("Produk", fontWeight = if (tabTerpilih == 0) FontWeight.Bold else FontWeight.Normal) },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Tab(
                            selected = tabTerpilih == 1,
                            onClick = { tabTerpilih = 1 },
                            text = { Text("Kategori", fontWeight = if (tabTerpilih == 1) FontWeight.Bold else FontWeight.Normal) },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                }
            }

            when (tabTerpilih) {
                0 -> KontenProdukTab(
                    state = state,
                    viewModel = viewModel,
                    navigasiKeTambahProduk = navigasiKeTambahProduk,
                    navigasiKeUbahProduk = navigasiKeUbahProduk,
                )
                1 -> KontenKategoriTab(
                    daftarKategori = state.daftarKategori,
                    onTambahKategori = { viewModel.tanganiAksi(ManageProductsAction.TambahKategori(it)) },
                    onHapusKategori = { viewModel.tanganiAksi(ManageProductsAction.HapusKategori(it)) },
                )
            }
        }

        if (state.statusKonfirmasiHapus.apakahTampil) {
            DialogKonfirmasiHapus(
                status = state.statusKonfirmasiHapus,
                onKonfirmasi = { viewModel.tanganiAksi(ManageProductsAction.KonfirmasiDeleteProduct) },
                onBatal = { viewModel.tanganiAksi(ManageProductsAction.BatalkanDeleteProduct) },
            )
        }
    }
}
