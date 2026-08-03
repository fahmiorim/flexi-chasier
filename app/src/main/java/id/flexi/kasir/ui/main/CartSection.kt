package id.flexi.kasir.ui.main

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.flexi.kasir.ui.component.FlexiCard
import id.flexi.kasir.ui.component.FlexiDialog
import id.flexi.kasir.ui.component.FlexiDialogActions
import id.flexi.kasir.ui.component.FlexiDialogHeader
import id.flexi.kasir.ui.component.FlexiDialogWarningPanel
import id.flexi.kasir.ui.component.SimpleEmptyStatus
import id.flexi.kasir.domain.util.hitungSubTotal
import id.flexi.kasir.domain.util.sebagaiRupiah
import id.flexi.kasir.domain.model.CartItem

@Composable
internal fun CartPanel(
    daftarCartItem: List<CartItem>,
    statusKeranjang: CartStatus,
    saatTambahProduk: (String, String?) -> Unit,
    saatKurangiProduk: (String, String?) -> Unit,
    saatDeleteProduct: (String, String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionTitle(judul = "Keranjang")
            Text(
                text = statusKeranjang.jumlahItem,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (daftarCartItem.isEmpty()) {
            SimpleEmptyStatus(
                judul = statusKeranjang.judul,
                deskripsi = statusKeranjang.deskripsi,
            )
        } else {
            FlexiCard {
                Column {
                    daftarCartItem.forEachIndexed { indeks, item ->
                        CartItemRow(
                            CartItem = item,
                            saatTambahProduk = {
                                saatTambahProduk(item.produk.id, item.varian?.nama)
                            },
                            saatKurangiProduk = {
                                saatKurangiProduk(item.produk.id, item.varian?.nama)
                            },
                            saatDeleteProduct = {
                                saatDeleteProduct(item.produk.id, item.varian?.nama)
                            },
                        )

                        if (indeks < daftarCartItem.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun CartItemRow(
    CartItem: CartItem,
    saatTambahProduk: () -> Unit,
    saatKurangiProduk: () -> Unit,
    saatDeleteProduct: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var tampilKonfirmasiHapus by remember { mutableStateOf(false) }
    val itemSelesai = CartItem.apakahSelesai

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !itemSelesai) {
                if (!itemSelesai) tampilKonfirmasiHapus = true
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (CartItem.produk.fotoUri != null) {
                    val ctx = LocalContext.current
                    val bitmap = remember(CartItem.produk.fotoUri) {
                        try {
                            val uri = Uri.parse(CartItem.produk.fotoUri)
                            ctx.contentResolver.openInputStream(uri)?.use { stream ->
                                BitmapFactory.decodeStream(stream)
                            }
                        } catch (_: Exception) { null }
                    }
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = CartItem.produk.nama,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = CartItem.produk.nama,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (itemSelesai) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (itemSelesai) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selesai",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = "Selesai",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    if (CartItem.varian != null) {
                        Text(
                            text = "- ${CartItem.varian.nama}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    val hargaItem = CartItem.varian?.harga ?: CartItem.produk.harga
                    Text(
                        text = "${hargaItem.sebagaiRupiah()} x ${CartItem.jumlah}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    )
                }
            }

            Text(
                text = CartItem.hitungSubTotal().sebagaiRupiah(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (itemSelesai) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.primary,
            )
        }
    }

    if (tampilKonfirmasiHapus && !itemSelesai) {
        FlexiDialog(
            onDismissRequest = { tampilKonfirmasiHapus = false },
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                FlexiDialogHeader(
                    icon = Icons.Default.Delete,
                    title = "Hapus ${CartItem.produk.nama}?",
                    subtitle = "Item akan dihapus dari keranjang",
                    iconTint = MaterialTheme.colorScheme.error,
                    onClose = { tampilKonfirmasiHapus = false },
                )

                FlexiDialogWarningPanel(
                    message = if (CartItem.varian != null) {
                        "Varian: ${CartItem.varian.nama} — ${CartItem.jumlah} item"
                    } else {
                        "Item ini akan dihapus dari keranjang."
                    }
                )

                FlexiDialogActions(
                    onBatal = { tampilKonfirmasiHapus = false },
                    onKonfirmasi = {
                        tampilKonfirmasiHapus = false
                        saatDeleteProduct()
                    },
                    labelKonfirmasi = "Hapus",
                    konfirmasiIcon = Icons.Default.Delete,
                    konfirmasiColor = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
