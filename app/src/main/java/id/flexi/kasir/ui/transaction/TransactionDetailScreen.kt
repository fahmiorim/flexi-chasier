package id.flexi.kasir.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.flexi.kasir.ui.component.FlexiCard
import id.flexi.kasir.ui.component.FlexiDialog
import id.flexi.kasir.ui.component.FlexiDialogActions
import id.flexi.kasir.ui.component.FlexiDialogHeader
import id.flexi.kasir.ui.component.FlexiTopAppBar
import id.flexi.kasir.ui.component.SimpleEmptyStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    modelTampilan: TransactionDetailUiState,
    saatKembali: () -> Unit,
    saatCobaMuatUlang: () -> Unit,
    bukaDialogBatalkan: () -> Unit = {},
    tutupDialogBatalkan: () -> Unit = {},
    perbaruiAlasanPembatalan: (String) -> Unit = {},
    batalkan: () -> Unit = {},
    alasanPembatalan: String = "",
    modifier: Modifier = Modifier,
) {
    if (modelTampilan.apakahDialogBatalkanTerbuka) {
        FlexiDialog(
            onDismissRequest = tutupDialogBatalkan,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                FlexiDialogHeader(
                    icon = Icons.Default.Close,
                    title = "Batalkan Transaksi",
                    subtitle = "Apakah Anda yakin ingin membatalkan transaksi ini?",
                    onClose = tutupDialogBatalkan,
                    iconTint = MaterialTheme.colorScheme.error,
                )

                OutlinedTextField(
                    value = alasanPembatalan,
                    onValueChange = perbaruiAlasanPembatalan,
                    label = { Text("Alasan pembatalan (opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                )

                FlexiDialogActions(
                    onBatal = tutupDialogBatalkan,
                    labelBatal = "Batal",
                    onKonfirmasi = batalkan,
                    labelKonfirmasi = "Batalkan",
                    konfirmasiColor = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { paddingDalam ->
        when (val statusMuat = modelTampilan.statusMuat) {
            StatusMuatDetailTransaction.Memuat -> {
                KontenMemuatDetailTransaction(
                    paddingDalam = paddingDalam,
                    saatKembali = saatKembali,
                )
            }

            is StatusMuatDetailTransaction.Berhasil -> {
    KontenDetailTransactionBerhasil(
        paddingDalam = paddingDalam,
        saatKembali = saatKembali,
        statusMuat = statusMuat,
        bukaDialogBatalkan = bukaDialogBatalkan,
    )
            }

            is StatusMuatDetailTransaction.Kosong -> {
                KontenTransactionKosong(
                    paddingDalam = paddingDalam,
                    saatKembali = saatKembali,
                    judul = statusMuat.judul,
                    deskripsi = statusMuat.deskripsi,
                )
            }

            is StatusMuatDetailTransaction.Gagal -> {
                KontenTransactionGagal(
                    paddingDalam = paddingDalam,
                    saatKembali = saatKembali,
                    judul = statusMuat.judul,
                    deskripsi = statusMuat.deskripsi,
                    saatCobaMuatUlang = saatCobaMuatUlang,
                )
            }
        }
    }
}

/**
 * Konten yang ditampilkan saat detail Transaction sedang dimuat.
 *
 * @param paddingDalam Padding dari Scaffold.
 * @param saatKembali Callback saat tombol kembali diketuk.
 * @param modifier Modifikasi tata letak.
 */
@Composable
private fun KontenMemuatDetailTransaction(
    paddingDalam: PaddingValues,
    saatKembali: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingDalam)
            .padding(16.dp),
    ) {
        FlexiTopAppBar(
            title = "Detail Transaksi",
            saatKembali = saatKembali,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Memuat detail Transaksi...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

/**
 * Konten yang ditampilkan saat detail Transaction berhasil dimuat.
 *
 * @param paddingDalam Padding dari Scaffold.
 * @param saatKembali Callback saat tombol kembali diketuk.
 * @param statusMuat Data detail Transaction yang berhasil dimuat.
 * @param modifier Modifikasi tata letak.
 */
@Composable
private fun KontenDetailTransactionBerhasil(
    paddingDalam: PaddingValues,
    saatKembali: () -> Unit,
    statusMuat: StatusMuatDetailTransaction.Berhasil,
    bukaDialogBatalkan: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingDalam)
            .padding(16.dp),
    ) {
        FlexiTopAppBar(
            title = "Detail Transaksi",
            saatKembali = saatKembali,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ── Header: ID + Waktu + Status + Pembayaran + Meja ──
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = statusMuat.labelIdentitasTransaction,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = statusMuat.labelWaktu,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = statusMuat.labelPembayaran,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (statusMuat.labelMeja != null) {
                            Text(
                                text = "• ${statusMuat.labelMeja}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (!statusMuat.catatan.isNullOrBlank()) {
                            Text(
                                text = "• ${statusMuat.catatan}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    if (statusMuat.dibatalkan) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            tonalElevation = 0.dp,
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            ) {
                                Text(
                                    text = "DIBATALKAN",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                )
                                if (!statusMuat.alasanPembatalan.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Alasan: ${statusMuat.alasanPembatalan}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Ringkasan Finansial ──
            item {
                KartuRingkasanFinansialTransaction(
                    statusMuat = statusMuat,
                )
            }

            // ── Daftar Item ──
            item {
                Text(
                    text = "Daftar Item (${statusMuat.daftarItem.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            items(
                items = statusMuat.daftarItem,
                key = { item -> item.namaProduk + item.labelJumlahKaliHarga },
            ) { item ->
                FlexiCard(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.namaProduk,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = item.labelJumlahKaliHarga,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = item.labelSubtotalItem,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            if (!statusMuat.dibatalkan) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = bukaDialogBatalkan,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text("Batalkan Transaksi", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onError)
                    }
                }
            }
        }
    }
}

/**
 * Komponen kartu yang merangkum rincian finansial Transaction (total, pajak, kembalian, dll).
 *
 * @param statusMuat Data Transaction yang berhasil dimuat.
 * @param modifier Modifikasi tata letak.
 */
@Composable
private fun KartuRingkasanFinansialTransaction(
    statusMuat: StatusMuatDetailTransaction.Berhasil,
    modifier: Modifier = Modifier,
) {
    FlexiCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BarisRingkasanTransaction(
                label = "Jumlah item",
                nilai = statusMuat.labelJumlahItem,
            )
            BarisRingkasanTransaction(
                label = "Subtotal",
                nilai = statusMuat.labelSubtotal,
            )
            BarisRingkasanTransaction(
                label = "Potongan",
                nilai = statusMuat.labelPotongan,
            )
            BarisRingkasanTransaction(
                label = "Biaya layanan",
                nilai = statusMuat.labelBiayaLayanan,
            )
            BarisRingkasanTransaction(
                label = "Pajak",
                nilai = statusMuat.labelPajak,
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            BarisRingkasanTransaction(
                label = "Total akhir",
                nilai = statusMuat.labelTotalAkhir,
                tonjolkan = true,
            )
            BarisRingkasanTransaction(
                label = "Uang dibayar",
                nilai = statusMuat.labelUangDibayar,
                tonjolkan = true,
            )
            BarisRingkasanTransaction(
                label = "Kembalian",
                nilai = statusMuat.labelKembalian,
                tonjolkan = true,
            )
        }
    }
}

/**
 * Baris sederhana untuk menampilkan satu entri informasi finansial (misal: Pajak: Rp1.000).
 *
 * @param label Teks label di sisi kiri.
 * @param nilai Teks nilai di sisi kanan.
 * @param modifier Modifikasi tata letak.
 * @param tonjolkan Apakah teks harus ditampilkan lebih tebal/menonjol.
 */
@Composable
private fun BarisRingkasanTransaction(
    label: String,
    nilai: String,
    modifier: Modifier = Modifier,
    tonjolkan: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = if (tonjolkan) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (tonjolkan) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = nilai,
            style = if (tonjolkan) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (tonjolkan) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (tonjolkan) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Konten yang ditampilkan saat detail Transaction tidak ditemukan atau kosong.
 *
 * @param paddingDalam Padding dari Scaffold.
 * @param saatKembali Callback saat tombol kembali diketuk.
 * @param judul Pesan judul kosong.
 * @param deskripsi Pesan deskripsi kosong.
 * @param modifier Modifikasi tata letak.
 */
@Composable
private fun KontenTransactionKosong(
    paddingDalam: PaddingValues,
    saatKembali: () -> Unit,
    judul: String,
    deskripsi: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingDalam)
            .padding(16.dp),
    ) {
        FlexiTopAppBar(
            title = "Detail Transaksi",
            saatKembali = saatKembali,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        SimpleEmptyStatus(
            judul = judul,
            deskripsi = deskripsi,
        )
    }
}

/**
 * Konten yang ditampilkan saat terjadi kegagalan dalam memuat detail Transaction.
 *
 * @param paddingDalam Padding dari Scaffold.
 * @param saatKembali Callback saat tombol kembali diketuk.
 * @param judul Pesan judul kegagalan.
 * @param deskripsi Pesan rincian kegagalan.
 * @param saatCobaMuatUlang Callback untuk mencoba memuat data kembali.
 * @param modifier Modifikasi tata letak.
 */
@Composable
private fun KontenTransactionGagal(
    paddingDalam: PaddingValues,
    saatKembali: () -> Unit,
    judul: String,
    deskripsi: String,
    saatCobaMuatUlang: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingDalam)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        FlexiTopAppBar(
            title = "Detail Transaksi",
            saatKembali = saatKembali,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        SimpleEmptyStatus(
            judul = judul,
            deskripsi = deskripsi,
        )

        Button(
            onClick = saatCobaMuatUlang,
            modifier = Modifier.heightIn(min = 48.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = "Coba Lagi",
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
