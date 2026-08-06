package id.flexi.kasir.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.flexi.kasir.ui.component.FlexiCard
import id.flexi.kasir.ui.component.FlexiDialog
import id.flexi.kasir.ui.component.FlexiDialogHeader
import id.flexi.kasir.domain.util.sebagaiRupiah
import java.util.Calendar

// ═══════════════════════════════════════
// 7-DAY SALES CHART
// ═══════════════════════════════════════

@Composable
internal fun GrafikPenjualan7Hari(
    data: List<DataPointGrafik>,
    modifier: Modifier = Modifier,
) {
    val maxTotal = data.maxOfOrNull { it.total } ?: 0L

    FlexiCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Icons.Default.TrendingUp, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "7 Hari Terakhir",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().height(140.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                data.forEach { point ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        verticalArrangement = Arrangement.Bottom,
                    ) {
                        if (point.total > 0L) {
                            Text(
                                text = formatHargaSingkat(point.total),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))

                        val tinggiBar = if (maxTotal > 0) {
                            (point.total.toFloat() / maxTotal.toFloat()).coerceIn(0.04f, 1f)
                        } else 0.04f

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = 0.6f)
                                .fillMaxHeight(fraction = tinggiBar)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (point.total > 0L)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                                ),
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = point.label.take(3),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

internal fun formatHargaSingkat(total: Long): String {
    return when {
        total >= 1_000_000 -> "${total / 1_000_000}jt"
        total >= 1_000 -> "${total / 1_000}rb"
        else -> total.sebagaiRupiah()
    }
}

// ═══════════════════════════════════════
// TOTAL SALES CARD
// ═══════════════════════════════════════

@Composable
internal fun KartuTotalPenjualan(
    total: String,
    jumlahTransaksi: Int,
    rataRata: String,
    totalItem: Int,
    totalDiskon: String,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.TrendingUp, contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Column {
                    Text(
                        text = "Total Penjualan",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = total,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MetrikChip(label = "Transaksi", value = "$jumlahTransaksi", warnaTerang = true)
                MetrikChip(label = "Item terjual", value = "$totalItem", warnaTerang = true)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MetrikChip(label = "Rata-rata / Hari", value = rataRata, warnaTerang = true)
                if (totalDiskon != "Rp0") {
                    MetrikChip(label = "Diskon", value = totalDiskon, warnaTerang = true)
                }
            }
        }
    }
}

@Composable
internal fun MetrikChip(
    label: String,
    value: String,
    warnaTerang: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val textColor = if (warnaTerang) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = modifier, verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall,
            color = textColor.copy(alpha = 0.8f))
        Text(text = "·", style = MaterialTheme.typography.bodySmall,
            color = textColor.copy(alpha = 0.5f))
        Text(text = value, style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold, color = textColor)
    }
}

// ═══════════════════════════════════════
// PAYMENT METHOD CARD
// ═══════════════════════════════════════

@Composable
internal fun KartuMetodeBayar(
    label: String,
    total: String,
    jumlah: String,
    warna: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    FlexiCard(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(warna.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = warna,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold, color = warna)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = total, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Text(text = jumlah, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ═══════════════════════════════════════
// CATEGORY SALES ROW
// ═══════════════════════════════════════

@Composable
internal fun BarisKategori(
    nama: String,
    total: String,
    jumlah: Int,
    persentase: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = nama, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = persentase.coerceIn(0.05f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(text = total, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(text = "$jumlah terjual", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ═══════════════════════════════════════
// DATE RANGE DIALOG
// ═══════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DialogPilihTanggal(
    onDismiss: () -> Unit,
    onApply: (mulai: Long, selesai: Long) -> Unit,
) {
    val kal = Calendar.getInstance()
    kal.set(Calendar.HOUR_OF_DAY, 0); kal.set(Calendar.MINUTE, 0)
    kal.set(Calendar.SECOND, 0); kal.set(Calendar.MILLISECOND, 0)

    val stateMulai = rememberDatePickerState(
        initialSelectedDateMillis = kal.timeInMillis - 7 * 24 * 60 * 60 * 1000,
    )
    val stateSelesai = rememberDatePickerState(
        initialSelectedDateMillis = kal.timeInMillis,
    )
    var pilihMulai by remember { mutableStateOf(true) }

    FlexiDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FlexiDialogHeader(
                icon = Icons.Default.CalendarMonth,
                title = if (pilihMulai) "Pilih Tanggal Mulai" else "Pilih Tanggal Selesai",
                subtitle = if (pilihMulai) "Pilih tanggal awal laporan" else "Pilih tanggal akhir laporan",
                onClose = onDismiss,
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(modifier = Modifier.padding(8.dp)) {
                    if (pilihMulai) DatePicker(state = stateMulai)
                    else DatePicker(state = stateSelesai)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!pilihMulai) {
                    OutlinedButton(
                        onClick = { pilihMulai = true },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kembali", fontWeight = FontWeight.Medium)
                    }
                }
                Button(
                    onClick = {
                        if (pilihMulai) { pilihMulai = false }
                        else {
                            val mulai = stateMulai.selectedDateMillis ?: return@Button
                            val selesai = stateSelesai.selectedDateMillis ?: return@Button
                            if (mulai <= selesai) onApply(mulai, selesai)
                        }
                    },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    if (!pilihMulai) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(if (pilihMulai) "Lanjut" else "Terapkan", fontWeight = FontWeight.Bold)
                }
            }

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Batal", fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ═══════════════════════════════════════
// HELPERS
// ═══════════════════════════════════════

internal fun periodeLabel(periode: ReportPeriod): String = when (periode) {
    ReportPeriod.HariIni -> "Hari Ini"
    ReportPeriod.MingguIni -> "Minggu Ini"
    ReportPeriod.BulanIni -> "Bulan Ini"
    ReportPeriod.Kustom -> "Kustom"
}
