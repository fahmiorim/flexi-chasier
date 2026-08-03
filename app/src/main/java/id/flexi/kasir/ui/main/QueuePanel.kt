package id.flexi.kasir.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.flexi.kasir.domain.model.Meja
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.model.OrderType
import java.time.Duration
import java.time.Instant

private fun formatDurasiAntrian(waktuDibuatEpochMili: Long): String {
    val durasi = Duration.between(
        Instant.ofEpochMilli(waktuDibuatEpochMili),
        Instant.now(),
    )
    return when {
        durasi.toMinutes() < 1 -> "Baru saja"
        durasi.toMinutes() < 60 -> "${durasi.toMinutes()} mnt"
        else -> "${durasi.toHours()} jam ${durasi.toMinutes() % 60} mnt"
    }
}

private fun hitungMenitTunggu(waktuDibuatEpochMili: Long): Long {
    return Duration.between(
        Instant.ofEpochMilli(waktuDibuatEpochMili),
        Instant.now(),
    ).toMinutes()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QueuePanel(
    daftarMeja: List<Meja>,
    daftarPesananDiproses: List<Transaction>,
    apakahTampil: Boolean,
    saatTutup: () -> Unit,
    saatSelesaikan: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!apakahTampil) return

    ModalBottomSheet(
        onDismissRequest = saatTutup,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                    Column {
                        Text(
                            text = "Antrian Pesanan",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "${daftarPesananDiproses.size} pesanan menunggu",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                IconButton(onClick = saatTutup) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (daftarPesananDiproses.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tidak ada pesanan dalam antrian.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(
                        items = daftarPesananDiproses,
                        key = { it.id },
                    ) { transaction ->
                        QueueCard(
                            daftarMeja = daftarMeja,
                            transaction = transaction,
                            saatSelesaikan = { saatSelesaikan(transaction.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueCard(
    daftarMeja: List<Meja>,
    transaction: Transaction,
    saatSelesaikan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val itemBelumSelesai = transaction.daftarCartItem.filter { !it.apakahSelesai }
    val jumlahItem = itemBelumSelesai.sumOf { it.jumlah }

    val sudahBayar = transaction.paymentStatus == id.flexi.kasir.domain.model.PaymentStatus.SudahDibayar

    val menitTunggu = hitungMenitTunggu(transaction.waktuTransactionEpochMili)
    val warnaKartu = when {
        menitTunggu >= 20 -> Color(0xFFD32F2F).copy(alpha = 0.08f)  // merah sangat transparan
        menitTunggu >= 10 -> Color(0xFFF57C00).copy(alpha = 0.08f)  // amber sangat transparan
        else -> MaterialTheme.colorScheme.surface
    }
    val durasiWarna = when {
        menitTunggu >= 20 -> Color(0xFFD32F2F)
        menitTunggu >= 10 -> Color(0xFFF57C00)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = warnaKartu,
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            // Baris 1: label + chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (transaction.orderType == OrderType.DineIn) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    val label = if (transaction.orderType == OrderType.DineIn) {
                        if (transaction.nomorAntrian != null) "Antrian #${transaction.nomorAntrian}" else "Dine In"
                    } else {
                        "Take Away"
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )

                    // Ikon warning untuk pesanan lama
                    if (menitTunggu >= 20) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Pesanan menunggu lama",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFD32F2F),
                        )
                    } else if (menitTunggu >= 10) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFF57C00),
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (sudahBayar) {
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = "Sudah bayar",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                            border = null,
                        )
                    } else {
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = "Belum bayar",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
                            border = null,
                        )
                    }
                    Text(
                        text = "${jumlahItem} item",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Baris 2: meja + durasi antrian
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (transaction.orderType == OrderType.DineIn &&
                    transaction.mejaId != null
                ) {
                    val nomorMeja = daftarMeja.firstOrNull { it.id == transaction.mejaId }?.nomor
                    Text(
                        text = if (nomorMeja != null) "Meja $nomorMeja" else "Meja ${transaction.mejaId}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Icon(
                    imageVector = if (menitTunggu >= 20) Icons.Default.Warning else Icons.Default.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = durasiWarna,
                )
                Text(
                    text = formatDurasiAntrian(transaction.waktuTransactionEpochMili),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (menitTunggu >= 10) FontWeight.Bold else FontWeight.Normal,
                    color = durasiWarna,
                )
            }

            Spacer(modifier = Modifier.size(4.dp))

            // Daftar item — hanya tampilkan yang belum selesai
            itemBelumSelesai.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${item.jumlah}x ${item.produk.nama}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (item.varian?.nama != null) {
                        Text(
                            text = "(${item.varian.nama})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (!item.catatan.isNullOrBlank()) {
                    Text(
                        text = "  · ${item.catatan}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (!transaction.catatan.isNullOrBlank()) {
                Spacer(modifier = Modifier.size(2.dp))
                Text(
                    text = "Catatan: ${transaction.catatan}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.size(6.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.size(6.dp))

            // Tombol selesai
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    onClick = saatSelesaikan,
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text("Selesai", modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    }
}
