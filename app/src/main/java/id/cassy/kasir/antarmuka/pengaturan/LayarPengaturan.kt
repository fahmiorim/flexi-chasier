package id.cassy.kasir.antarmuka.pengaturan

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import id.cassy.kasir.ranah.model.FormatCetakStruk
import id.cassy.kasir.ranah.model.TampilanKatalog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayarPengaturan(
    state: ModelTampilanPengaturan,
    saatKembali: () -> Unit,
    perbaruiLogoUri: (String) -> Unit,
    perbaruiNamaUsaha: (String) -> Unit,
    perbaruiAlamat: (String) -> Unit,
    perbaruiTampilanKatalog: (TampilanKatalog) -> Unit,
    perbaruiMetodeBayarTunai: (Boolean) -> Unit,
    perbaruiMetodeBayarQris: (Boolean) -> Unit,
    perbaruiFormatCetakStruk: (FormatCetakStruk) -> Unit,
    perbaruiSuaraNotifikasi: (Boolean) -> Unit,
    perbaruiSatuanStokDefault: (String) -> Unit,
    saatSimpan: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.pesanBerhasil) {
        state.pesanBerhasil?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(state.judulLayar) },
                navigationIcon = {
                    IconButton(onClick = saatKembali) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
            )
        },
    ) { padding ->
        if (state.apakahSedangMemuat) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.apakahSedangMenyimpan) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            BagianIdentitasUsaha(
                logoUri = state.logoUri,
                namaUsaha = state.namaUsaha,
                alamat = state.alamat,
                perbaruiLogoUri = perbaruiLogoUri,
                perbaruiNamaUsaha = perbaruiNamaUsaha,
                perbaruiAlamat = perbaruiAlamat,
            )

            BagianTampilan(
                tampilanKatalog = state.tampilanKatalog,
                perbaruiTampilanKatalog = perbaruiTampilanKatalog,
            )

            BagianPembayaran(
                metodeBayarTunaiAktif = state.metodeBayarTunaiAktif,
                metodeBayarQrisAktif = state.metodeBayarQrisAktif,
                perbaruiMetodeBayarTunai = perbaruiMetodeBayarTunai,
                perbaruiMetodeBayarQris = perbaruiMetodeBayarQris,
            )

            BagianLainnya(
                formatCetakStruk = state.formatCetakStruk,
                suaraNotifikasiAktif = state.suaraNotifikasiAktif,
                satuanStokDefault = state.satuanStokDefault,
                perbaruiFormatCetakStruk = perbaruiFormatCetakStruk,
                perbaruiSuaraNotifikasi = perbaruiSuaraNotifikasi,
                perbaruiSatuanStokDefault = perbaruiSatuanStokDefault,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = saatSimpan,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                enabled = !state.apakahSedangMenyimpan,
            ) {
                Text("Simpan Pengaturan")
            }
        }
    }
}

@Composable
private fun BagianIdentitasUsaha(
    logoUri: String,
    namaUsaha: String,
    alamat: String,
    perbaruiLogoUri: (String) -> Unit,
    perbaruiNamaUsaha: (String) -> Unit,
    perbaruiAlamat: (String) -> Unit,
) {
    val context = LocalContext.current

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let { perbaruiLogoUri(it.toString()) }
    }

    KartuBagian("Identitas Usaha") {
        Text(
            text = "Logo Usaha",
            style = MaterialTheme.typography.bodyLarge,
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            if (logoUri.isNotBlank()) {
                val bitmap = remember(logoUri) {
                    try {
                        val uri = Uri.parse(logoUri)
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    } catch (_: Exception) { null }
                }

                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Logo usaha",
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }

        OutlinedButton(
            onClick = { pickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (logoUri.isBlank()) "Pilih Logo" else "Ganti Logo")
        }

        OutlinedTextField(
            value = namaUsaha,
            onValueChange = perbaruiNamaUsaha,
            label = { Text("Nama Usaha") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        OutlinedTextField(
            value = alamat,
            onValueChange = perbaruiAlamat,
            label = { Text("Alamat") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BagianTampilan(
    tampilanKatalog: TampilanKatalog,
    perbaruiTampilanKatalog: (TampilanKatalog) -> Unit,
) {
    KartuBagian("Tampilan Katalog") {
        var dropdownTerbuka by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = dropdownTerbuka,
            onExpandedChange = { dropdownTerbuka = it },
        ) {
            OutlinedTextField(
                value = when (tampilanKatalog) {
                    TampilanKatalog.List -> "Daftar (List)"
                    TampilanKatalog.Grid -> "Grid"
                },
                onValueChange = {},
                readOnly = true,
                label = { Text("Bentuk tampilan") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownTerbuka) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
            )

            ExposedDropdownMenu(
                expanded = dropdownTerbuka,
                onDismissRequest = { dropdownTerbuka = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Grid") },
                    onClick = {
                        perbaruiTampilanKatalog(TampilanKatalog.Grid)
                        dropdownTerbuka = false
                    },
                )
                DropdownMenuItem(
                    text = { Text("Daftar (List)") },
                    onClick = {
                        perbaruiTampilanKatalog(TampilanKatalog.List)
                        dropdownTerbuka = false
                    },
                )
            }
        }
    }
}

@Composable
private fun BagianPembayaran(
    metodeBayarTunaiAktif: Boolean,
    metodeBayarQrisAktif: Boolean,
    perbaruiMetodeBayarTunai: (Boolean) -> Unit,
    perbaruiMetodeBayarQris: (Boolean) -> Unit,
) {
    KartuBagian("Metode Pembayaran") {
        BarisSwitch(
            label = "Tunai",
            deskripsi = "Pembayaran menggunakan uang tunai",
            checked = metodeBayarTunaiAktif,
            onCheckedChange = perbaruiMetodeBayarTunai,
        )

        BarisSwitch(
            label = "QRIS",
            deskripsi = "Pembayaran scan QRIS",
            checked = metodeBayarQrisAktif,
            onCheckedChange = perbaruiMetodeBayarQris,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BagianLainnya(
    formatCetakStruk: FormatCetakStruk,
    suaraNotifikasiAktif: Boolean,
    satuanStokDefault: String,
    perbaruiFormatCetakStruk: (FormatCetakStruk) -> Unit,
    perbaruiSuaraNotifikasi: (Boolean) -> Unit,
    perbaruiSatuanStokDefault: (String) -> Unit,
) {
    KartuBagian("Lainnya") {
        var dropdownTerbuka by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = dropdownTerbuka,
            onExpandedChange = { dropdownTerbuka = it },
        ) {
            OutlinedTextField(
                value = when (formatCetakStruk) {
                    FormatCetakStruk.Otomatis -> "Otomatis setelah bayar"
                    FormatCetakStruk.Manual -> "Cetak manual"
                },
                onValueChange = {},
                readOnly = true,
                label = { Text("Cetak struk") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownTerbuka) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
            )

            ExposedDropdownMenu(
                expanded = dropdownTerbuka,
                onDismissRequest = { dropdownTerbuka = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Otomatis setelah bayar") },
                    onClick = {
                        perbaruiFormatCetakStruk(FormatCetakStruk.Otomatis)
                        dropdownTerbuka = false
                    },
                )
                DropdownMenuItem(
                    text = { Text("Cetak manual") },
                    onClick = {
                        perbaruiFormatCetakStruk(FormatCetakStruk.Manual)
                        dropdownTerbuka = false
                    },
                )
            }
        }

        BarisSwitch(
            label = "Suara notifikasi",
            deskripsi = "Bunyi setiap scan / tambah item",
            checked = suaraNotifikasiAktif,
            onCheckedChange = perbaruiSuaraNotifikasi,
        )

        OutlinedTextField(
            value = satuanStokDefault,
            onValueChange = perbaruiSatuanStokDefault,
            label = { Text("Satuan stok default") },
            placeholder = { Text("pcs, cup, gelas, gram") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
}

@Composable
private fun KartuBagian(
    judul: String,
    konten: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = judul,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            konten()
        }
    }
}

@Composable
private fun BarisSwitch(
    label: String,
    deskripsi: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = deskripsi,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
