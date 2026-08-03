package id.flexi.kasir.ui.manage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import id.flexi.kasir.ui.component.FlexiTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    viewModel: ProductFormViewModel,
    navigasiKembali: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.amatiState.collectAsState()

    LaunchedEffect(state.apakahBerhasilDisimpan) {
        if (state.apakahBerhasilDisimpan) {
            navigasiKembali()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { bantalan ->
        Column(
            modifier = Modifier
                .padding(bantalan)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            FlexiTopAppBar(
                title = state.judulLayar,
                saatKembali = navigasiKembali,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            if (state.apakahSedangMenyimpan) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Foto + Nama + Harga + Kategori
            BagianFotoProduk(
                fotoUri = state.fotoUri,
                nama = state.nama,
                harga = state.harga,
                kategori = state.kategori,
                daftarKategori = state.daftarKategori,
                pesanKesalahanNama = state.pesanKesalahanNama,
                pesanKesalahanHarga = state.pesanKesalahanHarga,
                apakahTampilVarian = state.apakahTampilVarian,
                varianDraft = state.varianDraft,
                onUbahNama = { viewModel.tanganiAksi(ProductFormAction.UbahNama(it)) },
                onUbahHarga = { viewModel.tanganiAksi(ProductFormAction.UbahHarga(it)) },
                onUbahKategori = { viewModel.tanganiAksi(ProductFormAction.UbahKategori(it)) },
                onPilihFoto = { viewModel.tanganiAksi(ProductFormAction.PilihFoto(it)) },
                onHapusFoto = { viewModel.tanganiAksi(ProductFormAction.HapusFoto) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Favorit
            BagianFavorit(
                favorit = state.favorit,
                onToggle = { viewModel.tanganiAksi(ProductFormAction.ToggleFavorit) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Harga Modal
            BagianHargaModal(
                apakahTampil = state.apakahTampilHargaModal,
                hargaModal = state.hargaModal,
                onToggle = { viewModel.tanganiAksi(ProductFormAction.ToggleTampilHargaModal) },
                onUbahHargaModal = { viewModel.tanganiAksi(ProductFormAction.UbahHargaModal(it)) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Kelola Stok + Varian
            BagianKelolaStok(
                apakahTampil = state.apakahTampilKelolaStok,
                stok = state.stok,
                pesanKesalahanStok = state.pesanKesalahanStok,
                apakahTampilVarian = state.apakahTampilVarian,
                varianDraft = state.varianDraft,
                onToggle = { viewModel.tanganiAksi(ProductFormAction.ToggleTampilKelolaStok) },
                onUbahStok = { viewModel.tanganiAksi(ProductFormAction.UbahStok(it)) },
                onToggleVarian = { viewModel.tanganiAksi(ProductFormAction.ToggleTampilVarian) },
                onUbahNamaVarian = { indeks, nama ->
                    viewModel.tanganiAksi(ProductFormAction.UbahNamaVarian(indeks, nama))
                },
                onUbahHargaVarian = { indeks, harga ->
                    viewModel.tanganiAksi(ProductFormAction.UbahHargaVarian(indeks, harga))
                },
                onTambahVarian = { viewModel.tanganiAksi(ProductFormAction.TambahVarian("", "")) },
                onHapusVarianTerakhir = { viewModel.tanganiAksi(ProductFormAction.HapusVarianTerakhir) },
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Hapus Produk (hanya saat edit)
            if (viewModel.idProduk != null) {
                OutlinedButton(
                    onClick = { viewModel.tanganiAksi(ProductFormAction.MintaHapusProduk) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text("Hapus Produk", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Simpan
            Button(
                onClick = { viewModel.tanganiAksi(ProductFormAction.Simpan) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                enabled = state.apakahBisaSimpan && !state.apakahSedangMenyimpan,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Simpan Produk", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Dialog konfirmasi hapus produk
    if (state.apakahTampilDialogHapus && viewModel.idProduk != null) {
        DialogHapusProduk(
            namaProduk = state.nama,
            onDismiss = { viewModel.tanganiAksi(ProductFormAction.BatalkanHapusProduk) },
            onConfirmHapus = { viewModel.tanganiAksi(ProductFormAction.KonfirmasiHapusProduk) },
        )
    }
}
