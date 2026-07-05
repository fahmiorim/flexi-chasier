package id.cassy.kasir.antarmuka.utama

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import id.cassy.kasir.ranah.fungsi.sebagaiRupiah
import id.cassy.kasir.ranah.model.Produk

private val WarnaAksenProduk = listOf(
    Color(0xFF5D4037),
    Color(0xFF6D4C41),
    Color(0xFF8D6E63),
    Color(0xFFA1887F),
    Color(0xFFBCAAA4),
    Color(0xFFD4A373),
    Color(0xFFC4956A),
    Color(0xFFB5835A),
)

@Composable
internal fun BagianPencarianProdukKasir(
    nilaiPencarian: String,
    saatNilaiPencarianBerubah: (String) -> Unit,
    jumlahHasil: Int,
    tampilkanAksiResetPencarian: Boolean,
    saatResetPencarian: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = nilaiPencarian,
        onValueChange = saatNilaiPencarianBerubah,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingIcon = {
            if (tampilkanAksiResetPencarian) {
                IconButton(onClick = saatResetPencarian) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Reset pencarian",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        placeholder = {
            Text(
                text = "Cari produk...",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        },
        supportingText = {
            Text(
                text = "$jumlahHasil produk ditemukan",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { /* no-op */ }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        ),
    )
}

@Composable
internal fun JudulBagianKasir(
    judul: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = judul,
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
internal fun KartuProdukKasir(
    produk: Produk,
    saatTambahProduk: () -> Unit,
    saatBukaDetailProduk: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val produkBisaDipilih = produk.aktif && produk.stokTersedia > 0

    Card(
        onClick = saatTambahProduk,
        enabled = produkBisaDipilih,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column {
            BagianGambarProduk(
                namaProduk = produk.nama,
                stokTersedia = produk.stokTersedia,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            ) {
                Text(
                    text = produk.nama,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = produk.harga.sebagaiRupiah(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun BagianGambarProduk(
    namaProduk: String,
    stokTersedia: Int,
    modifier: Modifier = Modifier,
) {
    val indeksWarna = namaProduk.hashCode().let { (it and Int.MAX_VALUE) % WarnaAksenProduk.size }
    val warnaAksen = WarnaAksenProduk[indeksWarna]
    val inisial = namaProduk.take(1).uppercase()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(warnaAksen.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = inisial,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = warnaAksen,
        )

        if (stokTersedia <= 5) {
            val labelStok = when {
                stokTersedia <= 0 -> "Habis"
                else -> "$stokTersedia"
            }
            val warnaLabel = when {
                stokTersedia <= 0 -> MaterialTheme.colorScheme.error
                else -> Color(0xFFE65100)
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp),
                shape = RoundedCornerShape(4.dp),
                color = when {
                    stokTersedia <= 0 -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.tertiaryContainer
                },
            ) {
                Text(
                    text = labelStok,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = warnaLabel,
                )
            }
        }
    }
}

@Composable
internal fun LencanaStokKasir(
    stokTersedia: Int,
    modifier: Modifier = Modifier,
) {
    val (warna, teks) = when {
        stokTersedia <= 0 -> MaterialTheme.colorScheme.errorContainer to "Habis"
        stokTersedia <= 5 -> MaterialTheme.colorScheme.tertiaryContainer to "Sisa $stokTersedia"
        else -> MaterialTheme.colorScheme.secondaryContainer to "Stok $stokTersedia"
    }

    Surface(
        modifier = modifier,
        color = warna,
        shape = MaterialTheme.shapes.small,
        tonalElevation = 1.dp,
    ) {
        Text(
            text = teks,
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 6.dp,
            ),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}
