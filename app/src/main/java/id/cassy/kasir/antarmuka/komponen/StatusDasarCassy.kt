package id.cassy.kasir.antarmuka.komponen

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.cassy.kasir.antarmuka.tema.TemaCassyKasir

/**
 * Komponen untuk menampilkan status data kosong atau hasil pencarian nihil.
 * Membantu memberikan panduan visual kepada pengguna saat tidak ada data.
 *
 * @param judul Teks judul pesan kosong.
 * @param deskripsi Teks rincian pesan kosong.
 * @param modifier Modifikasi tata letak.
 */
@Composable
fun StatusKosongSederhana(
    judul: String,
    deskripsi: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = judul,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = deskripsi,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Komponen untuk menampilkan status gagal/error saat pengambilan data.
 * Dilengkapi dengan tombol aksi opsional untuk mencoba kembali (retry).
 *
 * @param pesan Teks rincian pesan kegagalan.
 * @param modifier Modifikasi tata letak.
 * @param saatCobaLagi Callback opsional saat tombol coba lagi ditekan.
 */
@Composable
fun StatusGagalSederhana(
    pesan: String,
    modifier: Modifier = Modifier,
    saatCobaLagi: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Gagal memuat data",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = pesan,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            if (saatCobaLagi != null) {
                OutlinedButton(
                    onClick = saatCobaLagi,
                    modifier = Modifier.padding(top = 8.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.onErrorContainer,
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Text(text = "Coba Lagi")
                }
            }
        }
    }
}

/**
 * Komponen penampung sementara (placeholder) saat data sedang dimuat.
 * Memberikan kesan responsif pada antarmuka pengguna.
 *
 * @param modifier Modifikasi tata letak.
 */
@Composable
fun PlaceholderMemuatSederhana(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// --- Area Pratinjau (Preview) ---

@Preview(
    name = "Status kosong terang",
    showBackground = true,
    widthDp = 360,
)
@Preview(
    name = "Status kosong gelap",
    showBackground = true,
    widthDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewStatusKosongSederhana() {
    TemaCassyKasir {
        StatusKosongSederhana(
            judul = "Belum ada produk",
            deskripsi = "Tambahkan produk pertama untuk mulai berjualan.",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    name = "Placeholder memuat",
    showBackground = true,
    widthDp = 360,
)
@Composable
private fun PreviewPlaceholderMemuatSederhana() {
    TemaCassyKasir {
        PlaceholderMemuatSederhana(
            modifier = Modifier.padding(16.dp),
        )
    }
}
