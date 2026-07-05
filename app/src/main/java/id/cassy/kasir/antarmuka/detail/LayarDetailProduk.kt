package id.cassy.kasir.antarmuka.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.cassy.kasir.antarmuka.komponen.StatusKosongSederhana

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayarDetailProduk(
    modelTampilan: ModelTampilanDetailProduk,
    saatKembali: () -> Unit,
    saatAksiDikirim: (AksiLayarDetailProduk) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = modelTampilan.judulLayar,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = saatKembali,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke katalog",
                        )
                    }
                },
            )
        },
    ) { paddingDalam ->
        when (val statusMuat = modelTampilan.statusMuat) {
            StatusMuatDetailProduk.Memuat -> {
                KontenMemuatDetailProduk(
                    paddingDalam = paddingDalam,
                )
            }

            is StatusMuatDetailProduk.Berhasil -> {
                KontenDetailProduk(
                    paddingDalam = paddingDalam,
                    namaProduk = statusMuat.namaProduk,
                    hargaProduk = statusMuat.hargaProduk,
                    stokTersedia = statusMuat.stokTersedia,
                    deskripsiProduk = statusMuat.deskripsiProduk,
                    statusAksi = statusMuat.statusAksi,
                    saatTambahKeKeranjang = {
                        saatAksiDikirim(AksiLayarDetailProduk.CobaTambahKeKeranjang)
                    },
                )
            }

            is StatusMuatDetailProduk.Kosong -> {
                KontenProdukTidakDitemukan(
                    paddingDalam = paddingDalam,
                    judulStatusKosong = statusMuat.judul,
                    deskripsiStatusKosong = statusMuat.deskripsi,
                )
            }

            is StatusMuatDetailProduk.Gagal -> {
                KontenGagalMemuatDetailProduk(
                    paddingDalam = paddingDalam,
                    judulStatusGagal = statusMuat.judul,
                    deskripsiStatusGagal = statusMuat.deskripsi,
                    saatCobaMuatUlang = {
                        saatAksiDikirim(AksiLayarDetailProduk.CobaMuatUlang)
                    },
                )
            }
        }
    }
}

@Composable
private fun KontenMemuatDetailProduk(
    paddingDalam: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingDalam)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text(
            text = "Memuat detail produk...",
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

/**
 * Konten utama layar detail produk saat data berhasil dimuat.
 *
 * Menggunakan lebar konten maksimum (720dp) agar tampilan tablet tidak terlalu renggang
 * dan elemen CTA tetap fokus di area yang mudah dijangkau.
 *
 * @param paddingDalam Padding dari Scaffold induk.
 * @param namaProduk Nama produk yang ditampilkan.
 * @param hargaProduk Harga produk dalam format Rupiah.
 * @param stokTersedia Jumlah stok tersedia.
 * @param deskripsiProduk Deskripsi lengkap produk.
 * @param statusAksi Status tombol aksi (aktif/nonaktif, label, keterangan).
 * @param saatTambahKeKeranjang Callback saat tombol tambah diketuk.
 */
@Composable
private fun KontenDetailProduk(
    paddingDalam: PaddingValues,
    namaProduk: String,
    hargaProduk: String,
    stokTersedia: Int,
    deskripsiProduk: String,
    statusAksi: StatusAksiDetailProduk,
    saatTambahKeKeranjang: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingDalam),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = namaProduk,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                text = "Harga Jual: $hargaProduk",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                text = "Stok di Toko: $stokTersedia",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = deskripsiProduk,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )

            statusAksi.keterangan?.let { keterangan ->
                Text(
                    text = keterangan,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Button(
                onClick = saatTambahKeKeranjang,
                enabled = statusAksi.aktif,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text(
                    text = statusAksi.label,
                )
            }
        }
    }
}

@Composable
private fun KontenProdukTidakDitemukan(
    paddingDalam: PaddingValues,
    judulStatusKosong: String,
    deskripsiStatusKosong: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingDalam)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StatusKosongSederhana(
            judul = judulStatusKosong,
            deskripsi = deskripsiStatusKosong,
        )
    }
}

@Composable
private fun KontenGagalMemuatDetailProduk(
    paddingDalam: PaddingValues,
    judulStatusGagal: String,
    deskripsiStatusGagal: String,
    saatCobaMuatUlang: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingDalam)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StatusKosongSederhana(
            judul = judulStatusGagal,
            deskripsi = deskripsiStatusGagal,
        )

        Button(
            onClick = saatCobaMuatUlang,
        ) {
            Text(
                text = "Coba Lagi",
            )
        }
    }
}
