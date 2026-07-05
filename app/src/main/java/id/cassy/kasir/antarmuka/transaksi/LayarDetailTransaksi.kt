package id.cassy.kasir.antarmuka.transaksi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.cassy.kasir.antarmuka.komponen.StatusKosongSederhana

/**
 * Layar detail transaksi yang menampilkan rincian lengkap dari sebuah nota penjualan.
 *
 * Layar ini bersifat stateless, menerima data via [modelTampilan] dan mengirimkan
 * aksi pengguna melalui callback.
 *
 * @param modelTampilan Status data yang akan ditampilkan di layar.
 * @param saatKembali Callback untuk navigasi balik.
 * @param saatCobaMuatUlang Callback untuk memicu pemuatan ulang data jika terjadi kegagalan.
 * @param modifier Modifikasi tata letak opsional.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayarDetailTransaksi(
    modelTampilan: ModelTampilanDetailTransaksi,
    saatKembali: () -> Unit,
    saatCobaMuatUlang: () -> Unit,
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
                    TextButton(
                        onClick = saatKembali,
                    ) {
                        Text(
                            text = "Kembali",
                        )
                    }
                },
            )
        },
    ) { paddingDalam ->
        when (val statusMuat = modelTampilan.statusMuat) {
            StatusMuatDetailTransaksi.Memuat -> {
                KontenMemuatDetailTransaksi(
                    paddingDalam = paddingDalam,
                )
            }

            is StatusMuatDetailTransaksi.Berhasil -> {
                KontenDetailTransaksiBerhasil(
                    paddingDalam = paddingDalam,
                    statusMuat = statusMuat,
                )
            }

            is StatusMuatDetailTransaksi.Kosong -> {
                KontenTransaksiKosong(
                    paddingDalam = paddingDalam,
                    judul = statusMuat.judul,
                    deskripsi = statusMuat.deskripsi,
                )
            }

            is StatusMuatDetailTransaksi.Gagal -> {
                KontenTransaksiGagal(
                    paddingDalam = paddingDalam,
                    judul = statusMuat.judul,
                    deskripsi = statusMuat.deskripsi,
                    saatCobaMuatUlang = saatCobaMuatUlang,
                )
            }
        }
    }
}

/**
 * Konten yang ditampilkan saat detail transaksi sedang dimuat.
 *
 * @param paddingDalam Padding dari Scaffold.
 * @param modifier Modifikasi tata letak.
 */
@Composable
private fun KontenMemuatDetailTransaksi(
    paddingDalam: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingDalam)
            .padding(16.dp),
    ) {
        Text(
            text = "Memuat detail transaksi...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

/**
 * Konten yang ditampilkan saat detail transaksi berhasil dimuat.
 *
 * @param paddingDalam Padding dari Scaffold.
 * @param statusMuat Data detail transaksi yang berhasil dimuat.
 * @param modifier Modifikasi tata letak.
 */
@Composable
private fun KontenDetailTransaksiBerhasil(
    paddingDalam: PaddingValues,
    statusMuat: StatusMuatDetailTransaksi.Berhasil,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingDalam),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = statusMuat.labelIdentitasTransaksi,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = statusMuat.labelWaktu,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            KartuRingkasanFinansialTransaksi(
                statusMuat = statusMuat,
            )
        }

        item {
            Text(
                text = "Daftar item",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        items(
            items = statusMuat.daftarItem,
            key = { item -> item.namaProduk + item.labelJumlahKaliHarga },
        ) { item ->
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = item.namaProduk,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = item.labelSubtotalItem,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    Text(
                        text = item.labelJumlahKaliHarga,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        statusMuat.catatan?.takeIf { it.isNotBlank() }?.let { catatan ->
            item {
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Catatan transaksi",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = catatan,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Komponen kartu yang merangkum rincian finansial transaksi (total, pajak, kembalian, dll).
 *
 * @param statusMuat Data transaksi yang berhasil dimuat.
 * @param modifier Modifikasi tata letak.
 */
@Composable
private fun KartuRingkasanFinansialTransaksi(
    statusMuat: StatusMuatDetailTransaksi.Berhasil,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BarisRingkasanTransaksi(
                label = "Jumlah item",
                nilai = statusMuat.labelJumlahItem,
            )
            BarisRingkasanTransaksi(
                label = "Subtotal",
                nilai = statusMuat.labelSubtotal,
            )
            BarisRingkasanTransaksi(
                label = "Potongan",
                nilai = statusMuat.labelPotongan,
            )
            BarisRingkasanTransaksi(
                label = "Biaya layanan",
                nilai = statusMuat.labelBiayaLayanan,
            )
            BarisRingkasanTransaksi(
                label = "Pajak",
                nilai = statusMuat.labelPajak,
            )

            HorizontalDivider()

            BarisRingkasanTransaksi(
                label = "Total akhir",
                nilai = statusMuat.labelTotalAkhir,
                tonjolkan = true,
            )
            BarisRingkasanTransaksi(
                label = "Uang dibayar",
                nilai = statusMuat.labelUangDibayar,
                tonjolkan = true,
            )
            BarisRingkasanTransaksi(
                label = "Kembalian",
                nilai = statusMuat.labelKembalian,
                tonjolkan = true,
            )
        }
    }
}

/**
 * Baris sederhana untuk menampilkan satu entri informasi finansial (misal: Pajak: Rp1.000).
 *
 * @param label Teks label di sisi kiri.
 * @param nilai Teks nilai di sisi kanan.
 * @param modifier Modifikasi tata letak.
 * @param tonjolkan Apakah teks harus ditampilkan lebih tebal/menonjol.
 */
@Composable
private fun BarisRingkasanTransaksi(
    label: String,
    nilai: String,
    modifier: Modifier = Modifier,
    tonjolkan: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = if (tonjolkan) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyLarge
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = nilai,
            style = if (tonjolkan) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyLarge
            },
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Konten yang ditampilkan saat detail transaksi tidak ditemukan atau kosong.
 *
 * @param paddingDalam Padding dari Scaffold.
 * @param judul Pesan judul kosong.
 * @param deskripsi Pesan deskripsi kosong.
 * @param modifier Modifikasi tata letak.
 */
@Composable
private fun KontenTransaksiKosong(
    paddingDalam: PaddingValues,
    judul: String,
    deskripsi: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingDalam)
            .padding(16.dp),
    ) {
        StatusKosongSederhana(
            judul = judul,
            deskripsi = deskripsi,
        )
    }
}

/**
 * Konten yang ditampilkan saat terjadi kegagalan dalam memuat detail transaksi.
 *
 * @param paddingDalam Padding dari Scaffold.
 * @param judul Pesan judul kegagalan.
 * @param deskripsi Pesan rincian kegagalan.
 * @param saatCobaMuatUlang Callback untuk mencoba memuat data kembali.
 * @param modifier Modifikasi tata letak.
 */
@Composable
private fun KontenTransaksiGagal(
    paddingDalam: PaddingValues,
    judul: String,
    deskripsi: String,
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
            judul = judul,
            deskripsi = deskripsi,
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
