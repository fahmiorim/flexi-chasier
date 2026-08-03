package id.flexi.kasir.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.flexi.kasir.ui.component.FlexiCard
import id.flexi.kasir.ui.component.FlexiDialog
import id.flexi.kasir.ui.component.FlexiDialogActions
import id.flexi.kasir.ui.component.FlexiDialogHeader
import id.flexi.kasir.ui.component.FlexiDialogWarningPanel
import id.flexi.kasir.domain.util.sebagaiRupiah
import id.flexi.kasir.domain.model.Meja
import id.flexi.kasir.domain.model.Transaction
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun PendingOrdersPanel(
    daftarMeja: List<Meja>,
    daftarPesananPending: List<Transaction>,
    apakahTampil: Boolean,
    saatTutup: () -> Unit,
    saatLanjutkan: (String) -> Unit,
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
                text = "${daftarPesananPending.size} pesanan menunggu Payment",
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
                    ) { Transaction ->
                        PendingOrderCard(
                            daftarMeja = daftarMeja,
                            Transaction = Transaction,
                            saatLanjutkan = { saatLanjutkan(Transaction.id) },
                            saatHapus = { saatHapus(Transaction.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingOrderCard(
    daftarMeja: List<Meja>,
    Transaction: Transaction,
    saatLanjutkan: () -> Unit,
    saatHapus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val jumlahItem = Transaction.daftarCartItem.sumOf { it.jumlah }
    val subtotal = Transaction.daftarCartItem.sumOf { item ->
        item.produk.harga * item.jumlah
    }
    val total = subtotal +
        Transaction.biayaLayanan.nilaiRupiah +
        Transaction.pajak.nilaiRupiah -
        Transaction.potongan.nilaiRupiah

    FlexiCard(
        modifier = modifier.fillMaxWidth(),
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
                    text = formatWaktuRelatif(Transaction.waktuTransactionEpochMili),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.size(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (Transaction.orderType == id.flexi.kasir.domain.model.OrderType.DineIn)
                        "Dine In" else "Take Away",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )

                if (Transaction.orderType == id.flexi.kasir.domain.model.OrderType.DineIn &&
                    Transaction.mejaId != null
                ) {
                    val nomorMeja = daftarMeja.firstOrNull { it.id == Transaction.mejaId }?.nomor
                    Text(
                        text = if (nomorMeja != null) "· Meja $nomorMeja" else "· Meja ${Transaction.mejaId}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (!Transaction.catatan.isNullOrBlank()) {
                Spacer(modifier = Modifier.size(2.dp))
                Text(
                    text = "Catatan: ${Transaction.catatan}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.size(6.dp))

            Transaction.daftarCartItem.take(3).forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "${item.jumlah}x ${item.produk.nama}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (item.apakahSelesai) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (item.apakahSelesai) {
                        Text(
                            text = "✓ Selesai",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            if (Transaction.daftarCartItem.size > 3) {
                Text(
                    text = "+${Transaction.daftarCartItem.size - 3} lainnya",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.size(8.dp))

            Text(
                text = total.sebagaiRupiah(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalButton(
                    onClick = saatLanjutkan,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Lanjutkan", style = MaterialTheme.typography.labelMedium)
                }

                var tampilKonfirmasiHapus by remember { mutableStateOf(false) }

                IconButton(onClick = { tampilKonfirmasiHapus = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus pesanan",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }

                if (tampilKonfirmasiHapus) {
                    FlexiDialog(
                        onDismissRequest = { tampilKonfirmasiHapus = false },
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            FlexiDialogHeader(
                                icon = Icons.Default.Delete,
                                title = "Hapus Pesanan?",
                                subtitle = "Tindakan ini tidak dapat dibatalkan",
                                onClose = { tampilKonfirmasiHapus = false },
                                iconTint = MaterialTheme.colorScheme.error,
                            )

                            FlexiDialogWarningPanel(
                                message = "Pesanan ini akan dihapus permanen. Tindakan ini tidak dapat dibatalkan.",
                            )

                            FlexiDialogActions(
                                onKonfirmasi = {
                                    tampilKonfirmasiHapus = false
                                    saatHapus()
                                },
                                labelKonfirmasi = "Hapus",
                                onBatal = { tampilKonfirmasiHapus = false },
                                labelBatal = "Batal",
                                konfirmasiColor = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
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
