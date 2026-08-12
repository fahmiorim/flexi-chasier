package id.flexi.kasir.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.flexi.kasir.ui.theme.FlexiGradients

/**
 * Header halaman modern dengan latar gradien brand.
 *
 * Konten (ikon, judul, badge sinkron) memakai warna [onPrimary] agar
 * kontras di mode terang; di mode gelap gradiennya lebih terang sehingga
 * otomatis memakai warna gelap yang kontras.
 */
@Composable
fun FlexiTopAppBar(
    title: String,
    saatKembali: () -> Unit = {},
    saatBukaSidebar: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val warnaKonten = MaterialTheme.colorScheme.onPrimary
    val shape = RoundedCornerShape(16.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, clip = false),
        shape = shape,
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(FlexiGradients.heroDalam()),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (saatBukaSidebar != null) {
                    IconButton(onClick = saatBukaSidebar) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Buka menu",
                            tint = warnaKonten,
                        )
                    }
                } else {
                    IconButton(onClick = saatKembali) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = warnaKonten,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp,
                    ),
                    color = warnaKonten,
                    modifier = Modifier.weight(1f),
                )

                // Indikator sinkronisasi premium
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = warnaKonten.copy(alpha = 0.16f),
                    contentColor = warnaKonten,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sinkron",
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = "Aktif",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }
                }
            }
        }
    }
}
