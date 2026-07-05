package id.cassy.kasir.antarmuka.utama

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.cassy.kasir.ranah.fungsi.sebagaiRupiah
import id.cassy.kasir.ranah.model.StatusTransaksi
import id.cassy.kasir.ranah.model.Transaksi
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun PanelPesananPending(
    daftarPesananPending: List<Transaksi>,
    apakahTampil: Boolean,
    saatTutup: () -> Unit,
    saatLanjutkan: (String) -> Unit,
    saatBayar: (String) -> Unit,
    saatHapus: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!apakahTampil) return

    ModalBottomSheet(
        onDismissRequest = saatTutup,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
        ) {
            Text(
                text = "Pesanan Pending",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp),
            )

            Text(
                text = "${daftarPesananPending.size} pesanan menunggu pembayaran",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            if (daftarPesananPending.isEmpty()) {
                Text(
                    text = "Belum ada pesanan pending.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp),
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = daftarPesananPending,
                        key = { it.id },
                    ) { transaksi ->
                        KartuPesananPending(
                            transaksi = transaksi,
                            saatLanjutkan = { saatLanjutkan(transaksi.id) },
                            saatBayar = { saatBayar(transaksi.id) },
                            saatHapus = { saatHapus(transaksi.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KartuPesananPending(
    transaksi: Transaksi,
    saatLanjutkan: () -> Unit,
    saatBayar: () -> Unit,
    saatHapus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val jumlahItem = transaksi.daftarItemKeranjang.sumOf { it.jumlah }
    val subtotal = transaksi.daftarItemKeranjang.sumOf { item ->
        item.produk.harga * item.jumlah
    }
    val total = subtotal +
        transaksi.biayaLayanan.nilaiRupiah +
        transaksi.pajak.nilaiRupiah -
        transaksi.potongan.nilaiRupiah

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$jumlahItem item",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = formatWaktuRelatif(transaksi.waktuTransaksiEpochMili),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.size(4.dp))

            if (!transaksi.catatan.isNullOrBlank()) {
                Text(
                    text = "Meja ${transaksi.catatan}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.size(2.dp))
            }

            transaksi.daftarItemKeranjang.take(3).forEach { item ->
                Text(
                    text = "${item.jumlah}x ${item.produk.nama}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (transaksi.daftarItemKeranjang.size > 3) {
                Text(
                    text = "+${transaksi.daftarItemKeranjang.size - 3} lainnya",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.size(8.dp))

            Text(
                text = "Rp${total}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalButton(
                    onClick = saatLanjutkan,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Lanjutkan", style = MaterialTheme.typography.labelMedium)
                }

                Button(
                    onClick = saatBayar,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCartCheckout,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bayar", style = MaterialTheme.typography.labelMedium)
                }

                IconButton(onClick = saatHapus) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus pesanan",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

private fun formatWaktuRelatif(waktuEpochMili: Long): String {
    val sekarang = Instant.now()
    val waktu = Instant.ofEpochMilli(waktuEpochMili)
    val selisihMenit = ChronoUnit.MINUTES.between(waktu, sekarang)

    return when {
        selisihMenit < 1 -> "Baru saja"
        selisihMenit < 60 -> "$selisihMenit menit lalu"
        else -> {
            val selisihJam = ChronoUnit.HOURS.between(waktu, sekarang)
            if (selisihJam < 24) "$selisihJam jam lalu"
            else waktu.atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd MMM HH:mm"))
        }
    }
}
