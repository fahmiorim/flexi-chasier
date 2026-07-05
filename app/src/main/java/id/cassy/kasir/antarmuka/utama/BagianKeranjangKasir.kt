package id.cassy.kasir.antarmuka.utama

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import id.cassy.kasir.antarmuka.komponen.StatusKosongSederhana
import id.cassy.kasir.ranah.fungsi.hitungSubTotal
import id.cassy.kasir.ranah.fungsi.sebagaiRupiah
import id.cassy.kasir.ranah.model.ItemKeranjang

@Composable
internal fun PanelKeranjangKasir(
    daftarItemKeranjang: List<ItemKeranjang>,
    statusKeranjang: StatusKeranjangKasir,
    saatTambahProduk: (String) -> Unit,
    saatKurangiProduk: (String) -> Unit,
    saatHapusProduk: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            JudulBagianKasir(judul = "Keranjang")

            Text(
                text = statusKeranjang.jumlahItem,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (daftarItemKeranjang.isEmpty()) {
            StatusKosongSederhana(
                judul = statusKeranjang.judul,
                deskripsi = statusKeranjang.deskripsi,
            )
        } else {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 4.dp),
                ) {
                    daftarItemKeranjang.forEachIndexed { indeks, itemKeranjang ->
                        BarisItemKeranjangKasir(
                            itemKeranjang = itemKeranjang,
                            saatTambahProduk = {
                                saatTambahProduk(itemKeranjang.produk.id)
                            },
                            saatKurangiProduk = {
                                saatKurangiProduk(itemKeranjang.produk.id)
                            },
                            saatHapusProduk = {
                                saatHapusProduk(itemKeranjang.produk.id)
                            },
                        )

                        if (indeks < daftarItemKeranjang.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun BarisItemKeranjangKasir(
    itemKeranjang: ItemKeranjang,
    saatTambahProduk: () -> Unit,
    saatKurangiProduk: () -> Unit,
    saatHapusProduk: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stokSudahPenuh = itemKeranjang.jumlah >= itemKeranjang.produk.stokTersedia

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = itemKeranjang.produk.nama,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${itemKeranjang.produk.harga.sebagaiRupiah()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = itemKeranjang.hitungSubTotal().sebagaiRupiah(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

            SpacerHeight(height = 8)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                FilledIconButton(
                    onClick = saatKurangiProduk,
                    enabled = itemKeranjang.jumlah > 1,
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Kurangi",
                        modifier = Modifier.size(16.dp),
                    )
                }

                Text(
                    text = "${itemKeranjang.jumlah}",
                    modifier = Modifier
                        .width(40.dp)
                        .padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                FilledIconButton(
                    onClick = saatTambahProduk,
                    enabled = !stokSudahPenuh,
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah",
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            IconButton(
                onClick = saatHapusProduk,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Hapus",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        if (stokSudahPenuh) {
            SpacerHeight(height = 4)
            Text(
                text = "Maksimum stok tercapai",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SpacerHeight(height: Int) {
    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(height.dp))
}
