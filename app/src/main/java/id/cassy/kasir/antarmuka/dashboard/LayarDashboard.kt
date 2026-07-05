package id.cassy.kasir.antarmuka.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayarDashboard(
    modelTampilan: ModelTampilanDashboard,
    saatKembali: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Dashboard") },
                navigationIcon = {
                    TextButton(onClick = saatKembali) {
                        Text(text = "Kembali")
                    }
                },
            )
        },
    ) { paddingDalam ->
        if (modelTampilan.apakahSedangMemuat) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingDalam),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingDalam)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                KartuPenjualan(
                    judul = "Penjualan Hari Ini",
                    total = modelTampilan.totalPenjualanHariIni,
                    sub = "${modelTampilan.jumlahTransaksiHariIni} · ${modelTampilan.totalProdukTerjualHariIni} item terjual",
                )

                KartuPenjualan(
                    judul = "Penjualan Minggu Ini",
                    total = modelTampilan.totalPenjualanMingguIni,
                    sub = "${modelTampilan.jumlahTransaksiMingguIni} · ${modelTampilan.totalProdukTerjualMingguIni} item terjual",
                )

                KartuPenjualan(
                    judul = "Penjualan Bulan Ini",
                    total = modelTampilan.totalPenjualanBulanIni,
                    sub = "${modelTampilan.jumlahTransaksiBulanIni} · ${modelTampilan.totalProdukTerjualBulanIni} item terjual",
                )

                HorizontalDivider()

                Text(
                    text = "Ringkasan",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )

                BarisStatistik(
                    label = "Total transaksi hari ini",
                    nilai = modelTampilan.jumlahTransaksiHariIni,
                )
                BarisStatistik(
                    label = "Total transaksi minggu ini",
                    nilai = modelTampilan.jumlahTransaksiMingguIni,
                )
                BarisStatistik(
                    label = "Total transaksi bulan ini",
                    nilai = modelTampilan.jumlahTransaksiBulanIni,
                )
            }
        }
    }
}

@Composable
private fun KartuPenjualan(
    judul: String,
    total: String,
    sub: String = "",
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = judul,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = total,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            if (sub.isNotBlank()) {
                Text(
                    text = sub,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun BarisStatistik(
    label: String,
    nilai: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = nilai,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
