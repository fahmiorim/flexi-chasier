package id.flexi.kasir.ui.main

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import id.flexi.kasir.ui.component.FlexiCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import id.flexi.kasir.domain.util.sebagaiRupiah
import id.flexi.kasir.domain.model.Produk

private val WarnaAksenProduk = listOf(
    Color(0xFFF04B32),
    Color(0xFF2697C8),
    Color(0xFFF59E0B),
    Color(0xFF10B981),
    Color(0xFF8B5CF6),
    Color(0xFFEC4899),
    Color(0xFF14B8A6),
    Color(0xFF6366F1),
)

@Composable
internal fun ProductSearchSection(
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
        shape = MaterialTheme.shapes.small,
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
                text = "Cari nama produk atau SKU...",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        supportingText = {
            Text(
                text = "$jumlahHasil produk ditemukan",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            )
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        ),
    )
}

@Composable
internal fun SectionTitle(
    judul: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = judul,
        modifier = modifier,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
internal fun ProductCard(
    produk: Produk,
    saatTambahProduk: () -> Unit,
    saatBukaDetailProduk: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val produkBisaDipilih = produk.aktif && (!produk.apakahStokDiaktifkan || produk.stokTersedia > 0)

    FlexiCard(
        onClick = if (produkBisaDipilih) saatTambahProduk else null,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {
            BagianGambarProduk(
                namaProduk = produk.nama,
                stokTersedia = produk.stokTersedia,
                apakahStokDiaktifkan = produk.apakahStokDiaktifkan,
                fotoUri = produk.fotoUri,
                favorit = produk.favorit,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = produk.nama,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )

                val hargaTampil = if (produk.varian.isNotEmpty()) {
                    val hargaMin = produk.varian.minOfOrNull { it.harga } ?: produk.harga
                    if (hargaMin > 0) {
                        "Mulai ${hargaMin.sebagaiRupiah()}"
                    } else {
                        produk.harga.sebagaiRupiah()
                    }
                } else {
                    produk.harga.sebagaiRupiah()
                }

                Text(
                    text = hargaTampil,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                if (produk.varian.isNotEmpty()) {
                    Text(
                        text = "${produk.varian.size} varian",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun BagianGambarProduk(
    namaProduk: String,
    stokTersedia: Int,
    apakahStokDiaktifkan: Boolean = false,
    fotoUri: String? = null,
    favorit: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val indeksWarna = namaProduk.hashCode().let { (it and Int.MAX_VALUE) % WarnaAksenProduk.size }
    val warnaAksen = WarnaAksenProduk[indeksWarna]
    val inisial = namaProduk.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

    val punyaFoto = fotoUri != null
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(if (punyaFoto) 80.dp else 64.dp),
        color = if (punyaFoto) warnaAksen.copy(alpha = 0.06f) else warnaAksen.copy(alpha = 0.04f),
        shape = MaterialTheme.shapes.medium,
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (punyaFoto) {
                val ctx = LocalContext.current
                val bitmap = remember(fotoUri) {
                    try {
                        val uri = Uri.parse(fotoUri)
                        ctx.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    } catch (_: Exception) { null }
                }
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = namaProduk,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop,
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = warnaAksen.copy(alpha = 0.3f),
                    )
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = MaterialTheme.shapes.extraSmall,
                        color = warnaAksen.copy(alpha = 0.15f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = inisial,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = warnaAksen,
                            )
                        }
                    }
                }
            }

            // Indikator favorit: icon hati di pojok kiri atas
            if (favorit) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Produk favorit",
                        modifier = Modifier.padding(4.dp).size(16.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            if (apakahStokDiaktifkan && stokTersedia <= 5) {
                val labelStok = when {
                    stokTersedia <= 0 -> "Habis"
                    else -> "Sisa $stokTersedia"
                }
                val warnaLabel = when {
                    stokTersedia <= 0 -> MaterialTheme.colorScheme.error
                    else -> Color(0xFFD97706)
                }
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    shape = MaterialTheme.shapes.extraSmall,
                    color = when {
                        stokTersedia <= 0 -> MaterialTheme.colorScheme.errorContainer
                        else -> Color(0xFFFEF3C7)
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
}

@Composable
internal fun StockBadge(
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
