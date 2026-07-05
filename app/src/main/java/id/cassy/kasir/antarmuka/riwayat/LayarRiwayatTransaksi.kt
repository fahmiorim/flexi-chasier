package id.cassy.kasir.antarmuka.riwayat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import id.cassy.kasir.antarmuka.komponen.StatusKosongSederhana

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayarRiwayatTransaksi(
    modelTampilan: ModelTampilanRiwayatTransaksi,
    saatKembali: () -> Unit,
    saatBukaDetailTransaksi: (String) -> Unit,
    saatCobaMuatUlang: () -> Unit,
    saatKataKunciPencarianBerubah: (String) -> Unit,
    saatResetPencarian: () -> Unit,
    saatFilterTanggalBerubah: (FilterTanggalRiwayat) -> Unit = {},
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingDalam)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BidangPencarianRiwayatTransaksi(
                kataKunciPencarian = modelTampilan.kataKunciPencarian,
                petunjukPencarian = modelTampilan.petunjukPencarian,
                saatKataKunciPencarianBerubah = saatKataKunciPencarianBerubah,
            )

            BarisFilterTanggal(
                filterAktif = modelTampilan.filterTanggal,
                saatFilterBerubah = saatFilterTanggalBerubah,
            )

            if (modelTampilan.tampilkanAksiResetPencarian) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = saatResetPencarian,
                    ) {
                        Text(
                            text = "Reset pencarian",
                        )
                    }
                }
            }

            when (val statusMuat = modelTampilan.statusMuat) {
                StatusMuatRiwayatTransaksi.Memuat -> {
                    KontenMemuatRiwayat(
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is StatusMuatRiwayatTransaksi.Berhasil -> {
                    KontenRiwayatBerhasil(
                        statusMuat = statusMuat,
                        saatBukaDetailTransaksi = saatBukaDetailTransaksi,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is StatusMuatRiwayatTransaksi.Kosong -> {
                    KontenRiwayatKosong(
                        judul = statusMuat.judul,
                        deskripsi = statusMuat.deskripsi,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is StatusMuatRiwayatTransaksi.Gagal -> {
                    KontenRiwayatGagal(
                        judul = statusMuat.judul,
                        deskripsi = statusMuat.deskripsi,
                        saatCobaMuatUlang = saatCobaMuatUlang,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun BidangPencarianRiwayatTransaksi(
    kataKunciPencarian: String,
    petunjukPencarian: String,
    saatKataKunciPencarianBerubah: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = kataKunciPencarian,
        onValueChange = saatKataKunciPencarianBerubah,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        label = {
            Text(
                text = "Cari riwayat transaksi",
            )
        },
        placeholder = {
            Text(
                text = petunjukPencarian,
            )
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search,
        ),
    )
}

@Composable
private fun KontenMemuatRiwayat(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Memuat riwayat transaksi...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun KontenRiwayatBerhasil(
    statusMuat: StatusMuatRiwayatTransaksi.Berhasil,
    saatBukaDetailTransaksi: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            KartuRingkasanHasilRiwayat(
                judulBagian = statusMuat.judulBagian,
                deskripsiBagian = statusMuat.deskripsiBagian,
                labelJumlahHasil = statusMuat.labelJumlahHasil,
                labelKataKunciAktif = statusMuat.labelKataKunciAktif,
            )
        }

        items(
            items = statusMuat.daftarRingkasanTransaksi,
            key = { ringkasan -> ringkasan.transaksiId },
        ) { ringkasan ->
            KartuRingkasanTransaksiRiwayat(
                ringkasan = ringkasan,
                saatBukaDetailTransaksi = {
                    saatBukaDetailTransaksi(ringkasan.transaksiId)
                },
            )
        }
    }
}

@Composable
private fun KartuRingkasanHasilRiwayat(
    judulBagian: String,
    deskripsiBagian: String,
    labelJumlahHasil: String,
    labelKataKunciAktif: String?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = judulBagian,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = deskripsiBagian,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = labelJumlahHasil,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            labelKataKunciAktif?.let { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun KartuRingkasanTransaksiRiwayat(
    ringkasan: RingkasanTransaksiRiwayat,
    saatBukaDetailTransaksi: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = saatBukaDetailTransaksi,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = ringkasan.labelIdentitasTransaksi,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = ringkasan.labelWaktu,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = ringkasan.ringkasanItem,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = ringkasan.labelJumlahItem,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = ringkasan.labelTotalAkhir,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            OutlinedButton(
                onClick = saatBukaDetailTransaksi,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text(
                    text = "Lihat Detail Transaksi",
                )
            }
        }
    }
}

@Composable
private fun KontenRiwayatKosong(
    judul: String,
    deskripsi: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        StatusKosongSederhana(
            judul = judul,
            deskripsi = deskripsi,
        )
    }
}

@Composable
private fun KontenRiwayatGagal(
    judul: String,
    deskripsi: String,
    saatCobaMuatUlang: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StatusKosongSederhana(
            judul = judul,
            deskripsi = deskripsi,
        )

        Button(
            onClick = saatCobaMuatUlang,
            modifier = Modifier.heightIn(min = 48.dp),
        ) {
            Text(
                text = "Coba Lagi",
            )
        }
    }
}

@Composable
private fun BarisFilterTanggal(
    filterAktif: FilterTanggalRiwayat,
    saatFilterBerubah: (FilterTanggalRiwayat) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterTanggalRiwayat.entries.forEach { filter ->
            FilterChip(
                selected = filterAktif == filter,
                onClick = { saatFilterBerubah(filter) },
                label = {
                    Text(
                        text = when (filter) {
                            FilterTanggalRiwayat.HariIni -> "Hari Ini"
                            FilterTanggalRiwayat.Kemarin -> "Kemarin"
                            FilterTanggalRiwayat.BulanIni -> "Bulan Ini"
                            FilterTanggalRiwayat.Semua -> "Semua"
                        },
                    )
                },
            )
        }
    }
}
