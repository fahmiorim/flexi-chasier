package id.flexi.kasir.ui.history

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.paging.compose.LazyPagingItems
import id.flexi.kasir.ui.component.FlexiCard
import id.flexi.kasir.ui.component.FlexiDialog
import id.flexi.kasir.ui.component.FlexiDialogHeader
import id.flexi.kasir.ui.component.SimpleEmptyStatus
import id.flexi.kasir.domain.util.sebagaiRupiah
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ═══════════════════════════════════════
// PAGED CONTENT — Dengan date header otomatis
// ═══════════════════════════════════════

@Composable
internal fun KontenRiwayatPaged(
    lazyItems: LazyPagingItems<RingkasanTransactionRiwayat>,
    saatBukaDetailTransaction: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pemformatTanggal = remember { SimpleDateFormat("EEEE, dd MMM yyyy", Locale("id", "ID")) }

    if (lazyItems.itemCount == 0 && lazyItems.loadState.refresh is androidx.paging.LoadState.Loading) {
        KontenMemuatRiwayat(modifier = modifier)
        return
    }

    if (lazyItems.itemCount == 0 && lazyItems.loadState.refresh is androidx.paging.LoadState.Error) {
        KontenRiwayatGagal(
            judul = "Gagal memuat riwayat transaksi",
            deskripsi = "Terjadi gangguan saat membaca data. Silakan coba lagi.",
            saatCobaMuatUlang = { lazyItems.refresh() },
            modifier = modifier,
        )
        return
    }

    if (lazyItems.itemCount == 0) {
        KontenRiwayatKosong(
            judul = "Belum ada riwayat transaksi",
            deskripsi = "Riwayat transaksi akan tampil di sini setelah data transaksi mulai disimpan.",
            modifier = modifier,
        )
        return
    }

    // Pre-compute total per tanggal dari SEMUA item yg sudah dimuat
    // Key = itemCount agar rekomputasi pas halaman baru dimuat
    val dateTotals = remember(lazyItems.itemCount) {
        val totals = mutableMapOf<String, Long>()
        for (i in 0 until lazyItems.itemCount) {
            val item = lazyItems[i] ?: continue
            if (!item.dibatalkan) {
                val tgl = pemformatTanggal.format(Date(item.waktuTransactionEpochMili))
                totals[tgl] = (totals[tgl] ?: 0L) + item.totalAkhir
            }
        }
        totals
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        var lastDate: String? = null

        items(lazyItems.itemCount) { index ->
            val item = lazyItems[index] ?: return@items
            val tgl = pemformatTanggal.format(Date(item.waktuTransactionEpochMili))

            // Date header — total PRE-COMPUTED dari semua item
            if (tgl != lastDate) {
                lastDate = tgl

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = tgl,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = (dateTotals[tgl] ?: 0L).sebagaiRupiah(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            KartuRingkasanTransactionRiwayat(
                ringkasan = item,
                saatBukaDetailTransaction = { saatBukaDetailTransaction(item.TransactionId) },
                modifier = Modifier.animateItem().fillMaxWidth(),
            )
        }

        // Loading indicator at bottom
        if (lazyItems.loadState.append is androidx.paging.LoadState.Loading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════
// LOADING STATE
// ═══════════════════════════════════════

@Composable
internal fun KontenMemuatRiwayat(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Memuat riwayat transaksi...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

// ═══════════════════════════════════════
// SUCCESS STATE
// ═══════════════════════════════════════

@Composable
internal fun KartuRingkasanTransactionRiwayat(
    ringkasan: RingkasanTransactionRiwayat,
    saatBukaDetailTransaction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlexiCard(
        onClick = saatBukaDetailTransaction,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = ringkasan.labelIdentitasTransaction,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Text(
                            text = ringkasan.paymentMethod.label,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                    if (ringkasan.dibatalkan) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                        ) {
                            Text(
                                text = "Dibatalkan",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ringkasan.labelWaktu,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ringkasan.ringkasanItem,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = ringkasan.labelTotalAkhir,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = ringkasan.labelJumlahItem,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ═══════════════════════════════════════
// EMPTY STATE
// ═══════════════════════════════════════

@Composable
internal fun KontenRiwayatKosong(
    judul: String,
    deskripsi: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SimpleEmptyStatus(judul = judul, deskripsi = deskripsi)
    }
}

// ═══════════════════════════════════════
// ERROR STATE
// ═══════════════════════════════════════

@Composable
internal fun KontenRiwayatGagal(
    judul: String,
    deskripsi: String,
    saatCobaMuatUlang: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SimpleEmptyStatus(judul = judul, deskripsi = deskripsi)
        Button(
            onClick = saatCobaMuatUlang,
            modifier = Modifier.heightIn(min = 48.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(text = "Coba Lagi", style = MaterialTheme.typography.labelLarge)
        }
    }
}

// ═══════════════════════════════════════
// DATE FILTER BAR
// ═══════════════════════════════════════

@Composable
internal fun BarisFilterTanggal(
    filterAktif: FilterTanggalRiwayat,
    labelRentangKustom: String,
    saatFilterBerubah: (FilterTanggalRiwayat) -> Unit,
    saatPilihKustom: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterTanggalRiwayat.entries.forEach { filter ->
            FilterChip(
                selected = filterAktif == filter,
                onClick = {
                    if (filter == FilterTanggalRiwayat.Kustom) saatPilihKustom()
                    else saatFilterBerubah(filter)
                },
                label = {
                    Text(
                        text = when (filter) {
                            FilterTanggalRiwayat.HariIni -> "Hari Ini"
                            FilterTanggalRiwayat.Kemarin -> "Kemarin"
                            FilterTanggalRiwayat.BulanIni -> "Bulan Ini"
                            FilterTanggalRiwayat.Semua -> "Semua"
                            FilterTanggalRiwayat.Kustom -> "Kustom"
                        },
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                modifier = Modifier.heightIn(min = 28.dp),
            )
        }

        if (filterAktif == FilterTanggalRiwayat.Kustom && labelRentangKustom.isNotBlank()) {
            Text(
                text = labelRentangKustom,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterVertically),
            )
        }
    }
}

// ═══════════════════════════════════════
// DATE RANGE DIALOG — 1 DateRangePicker
// ═══════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DialogRentangTanggalKustom(
    onDismiss: () -> Unit,
    onApply: (mulai: Long, selesai: Long) -> Unit,
) {
    val kalender = Calendar.getInstance()
    kalender.set(Calendar.HOUR_OF_DAY, 0)
    kalender.set(Calendar.MINUTE, 0)
    kalender.set(Calendar.SECOND, 0)
    kalender.set(Calendar.MILLISECOND, 0)

    val rangeState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = kalender.timeInMillis - 7 * 24 * 60 * 60 * 1000,
        initialSelectedEndDateMillis = kalender.timeInMillis,
    )
    val fmtTanggal = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }

    val mulaiDipilih = rangeState.selectedStartDateMillis
    val selesaiDipilih = rangeState.selectedEndDateMillis
    val tanggalValid = mulaiDipilih != null && selesaiDipilih != null && mulaiDipilih <= selesaiDipilih

    FlexiDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FlexiDialogHeader(
                icon = Icons.Default.CalendarMonth,
                title = "Pilih Rentang Tanggal",
                subtitle = "Pilih tanggal awal dan akhir",
                onClose = onDismiss,
            )

            // Ringkasan rentang yang dipilih
            if (mulaiDipilih != null || selesaiDipilih != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = buildString {
                                append(mulaiDipilih?.let { fmtTanggal.format(Date(it)) } ?: "?")
                                append(" → ")
                                append(selesaiDipilih?.let { fmtTanggal.format(Date(it)) } ?: "?")
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            // Satu DateRangePicker bawaan Material3
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(modifier = Modifier.padding(8.dp)) {
                    DateRangePicker(
                        state = rangeState,
                        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Batal", fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = {
                        val mulai = mulaiDipilih ?: return@Button
                        val selesai = selesaiDipilih ?: return@Button
                        if (mulai <= selesai) onApply(mulai, selesai)
                    },
                    enabled = tanggalValid,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Terapkan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
