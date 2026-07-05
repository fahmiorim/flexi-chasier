package id.cassy.kasir.antarmuka.kelola

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayarFormProduk(
    viewModel: ViewModelFormProduk,
    navigasiKembali: () -> Unit,
) {
    val state by viewModel.amatiState.collectAsState()

    LaunchedEffect(state.apakahBerhasilDisimpan) {
        if (state.apakahBerhasilDisimpan) {
            navigasiKembali()
        }
    }

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
        }
    ) { bantalan ->
        Column(
            modifier = Modifier
                .padding(bantalan)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            if (state.apakahSedangMenyimpan) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = state.nama,
                onValueChange = { viewModel.tanganiAksi(AksiLayarFormProduk.UbahNama(it)) },
                label = { Text("Nama Produk") },
                modifier = Modifier.fillMaxWidth(),
                isError = state.pesanKesalahanNama != null,
                supportingText = { state.pesanKesalahanNama?.let { Text(it) } },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.harga,
                onValueChange = { viewModel.tanganiAksi(AksiLayarFormProduk.UbahHarga(it)) },
                label = { Text("Harga (Rp)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.pesanKesalahanHarga != null,
                supportingText = { state.pesanKesalahanHarga?.let { Text(it) } },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.stok,
                onValueChange = { viewModel.tanganiAksi(AksiLayarFormProduk.UbahStok(it)) },
                label = { Text("Stok") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.pesanKesalahanStok != null,
                supportingText = { state.pesanKesalahanStok?.let { Text(it) } },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.deskripsi,
                onValueChange = { viewModel.tanganiAksi(AksiLayarFormProduk.UbahDeskripsi(it)) },
                label = { Text("Deskripsi (Opsional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.kategori,
                onValueChange = { viewModel.tanganiAksi(AksiLayarFormProduk.UbahKategori(it)) },
                label = { Text("Kategori (Opsional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.tanganiAksi(AksiLayarFormProduk.Simpan) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.apakahBisaSimpan && !state.apakahSedangMenyimpan
            ) {
                Text("Simpan Produk")
            }
        }
    }
}
