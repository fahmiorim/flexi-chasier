package id.flexi.kasir.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.flexi.kasir.ui.theme.FlexiGradients
import id.flexi.kasir.ui.theme.FlexiKasirTheme

/**
 * Komponen untuk menampilkan status data kosong atau hasil pencarian nihil.
 * Membantu memberikan panduan visual kepada pengguna saat tidak ada data.
 *
 * @param judul Teks judul pesan kosong.
 * @param deskripsi Teks rincian pesan kosong.
 * @param icon Ikon yang ditampilkan di tile gradien (opsional).
 * @param modifier Modifikasi tata letak.
 */
@Composable
fun SimpleEmptyStatus(
    judul: String,
    deskripsi: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Inbox,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(6.dp, CircleShape, clip = false)
                    .clip(CircleShape)
                    .background(FlexiGradients.tile(MaterialTheme.colorScheme.primary)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = judul,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = deskripsi,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
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
fun SimpleErrorStatus(
    pesan: String,
    modifier: Modifier = Modifier,
    saatCobaLagi: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
            Text(
                text = "Gagal memuat data",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = pesan,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center,
            )
            if (saatCobaLagi != null) {
                OutlinedButton(
                    onClick = saatCobaLagi,
                    modifier = Modifier.padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.5f),
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Text(text = "Coba Lagi", fontWeight = FontWeight.SemiBold)
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
fun SimpleLoadingPlaceholder(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Komponen untuk menampilkan status katalog sedang dimuat dari server saat
 * sinkronisasi pertama berjalan (katalog masih kosong). Menggantikan pesan
 * "produk tidak ditemukan" agar pengguna tahu produk sedang disiapkan.
 *
 * @param modifier Modifikasi tata letak.
 */
@Composable
fun KatalogMemuatStatus(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "Menyinkronkan katalog...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Produk sedang dimuat dari server.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
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
private fun PreviewSimpleEmptyStatus() {
    FlexiKasirTheme {
        SimpleEmptyStatus(
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
private fun PreviewSimpleLoadingPlaceholder() {
    FlexiKasirTheme {
        SimpleLoadingPlaceholder(
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    name = "Katalog memuat",
    showBackground = true,
    widthDp = 360,
)
@Composable
private fun PreviewKatalogMemuatStatus() {
    FlexiKasirTheme {
        KatalogMemuatStatus(
            modifier = Modifier.padding(16.dp),
        )
    }
}
