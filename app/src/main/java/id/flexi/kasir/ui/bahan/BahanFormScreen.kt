package id.flexi.kasir.ui.bahan

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import id.flexi.kasir.ui.component.FlexiGradientButton
import id.flexi.kasir.ui.component.FlexiTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BahanFormScreen(
    viewModel: BahanFormViewModel,
    navigasiKembali: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.amatiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.pesanSukses) {
        state.pesanSukses?.let {
            snackbarHostState.showSnackbar(it)
            navigasiKembali()
        }
    }

    LaunchedEffect(state.pesanError) {
        state.pesanError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.tanganiAksi(BahanFormAction.BersihkanPesan)
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            FlexiTopAppBar(
                title = if (state.apakahModeEdit) "Ubah Bahan" else "Tambah Bahan",
                saatKembali = navigasiKembali,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            if (state.apakahSedangMemuat) {
                CircularProgressIndicator(modifier = Modifier.weight(1f).fillMaxWidth())
                return@Column
            }

            OutlinedTextField(
                value = state.nama,
                onValueChange = { viewModel.tanganiAksi(BahanFormAction.UbahNama(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nama Bahan") },
                placeholder = { Text("Contoh: Susu Kental Manis") },
                isError = state.errorNama != null,
                supportingText = state.errorNama?.let { { Text(it) } },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.satuan,
                onValueChange = { viewModel.tanganiAksi(BahanFormAction.UbahSatuan(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Satuan") },
                placeholder = { Text("Contoh: gram, ml, pcs, botol") },
                isError = state.errorSatuan != null,
                supportingText = state.errorSatuan?.let { { Text(it) } },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.stokTersedia,
                onValueChange = { viewModel.tanganiAksi(BahanFormAction.UbahStokTersedia(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Stok Tersedia") },
                placeholder = { Text("Contoh: 500") },
                // Stok hanya berubah lewat "Atur Stok" atau "Catat Pembelian"
                // (konsisten dengan web) — field dibuat read-only saat edit.
                readOnly = state.apakahModeEdit,
                supportingText = if (state.apakahModeEdit) {
                    { Text("Ubah lewat Atur Stok / Catat Pembelian.") }
                } else {
                    null
                },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.hargaPerSatuan,
                onValueChange = { viewModel.tanganiAksi(BahanFormAction.UbahHargaPerSatuan(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Harga Per Satuan (Rp)") },
                placeholder = { Text("Contoh: 94 (harga per gram)") },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
            )

            Spacer(modifier = Modifier.height(24.dp))

            FlexiGradientButton(
                onClick = { viewModel.tanganiAksi(BahanFormAction.Simpan) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                text = if (state.apakahModeEdit) "Simpan Perubahan" else "Tambah Bahan",
                icon = Icons.Default.Check,
            )
        }
    }
}
