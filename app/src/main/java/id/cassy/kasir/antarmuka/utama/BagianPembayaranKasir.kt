package id.cassy.kasir.antarmuka.utama

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.cassy.kasir.ranah.model.MetodeBayar
import id.cassy.kasir.ranah.model.TipeOrder

@Composable
internal fun BagianRingkasanPembayaranKasir(
    ringkasanPembayaran: RingkasanPembayaranKasir,
    apakahRingkasanPembayaranTampil: Boolean,
    saatUbahVisibilitasRingkasanPembayaran: () -> Unit,
    saatSimpanPesanan: () -> Unit,
    saatCheckout: () -> Unit,
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
            JudulBagianKasir(judul = "Pembayaran")

            TextButton(onClick = saatUbahVisibilitasRingkasanPembayaran) {
                Text(
                    text = if (apakahRingkasanPembayaranTampil) "Sembunyikan" else "Tampilkan",
                )
            }
        }

        if (apakahRingkasanPembayaranTampil) {
            PanelRingkasanPembayaranKasir(
                ringkasanPembayaran = ringkasanPembayaran,
                saatSimpanPesanan = saatSimpanPesanan,
                saatCheckout = saatCheckout,
            )
        }
    }
}

@Composable
internal fun PanelRingkasanPembayaranKasir(
    ringkasanPembayaran: RingkasanPembayaranKasir,
    saatSimpanPesanan: () -> Unit,
    saatCheckout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BarisRingkasanPembayaranKasir(
                label = "Subtotal",
                nilai = ringkasanPembayaran.subtotal,
            )
            BarisRingkasanPembayaranKasir(
                label = "Potongan",
                nilai = ringkasanPembayaran.potongan,
            )
            BarisRingkasanPembayaranKasir(
                label = "Pajak",
                nilai = ringkasanPembayaran.pajak,
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )

            BarisRingkasanPembayaranKasir(
                label = "Total",
                nilai = ringkasanPembayaran.totalAkhir,
                tonjolkan = true,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(
                    onClick = saatSimpanPesanan,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp),
                    enabled = ringkasanPembayaran.aksiUtamaAktif,
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = "Simpan",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                Button(
                    onClick = saatCheckout,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp),
                    enabled = ringkasanPembayaran.aksiUtamaAktif,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(
                        text = "Bayar",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

@Composable
internal fun BarisRingkasanPembayaranKasir(
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
            style = if (tonjolkan) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyLarge
            },
            color = if (tonjolkan) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )

        Text(
            text = nilai,
            style = if (tonjolkan) {
                MaterialTheme.typography.headlineSmall
            } else {
                MaterialTheme.typography.titleMedium
            },
            fontWeight = if (tonjolkan) FontWeight.Bold else FontWeight.Normal,
            color = if (tonjolkan) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
    }
}

@Composable
internal fun DialogKonfirmasiCheckoutKasir(
    statusKonfirmasiCheckout: StatusKonfirmasiCheckoutKasir,
    saatBatalkan: () -> Unit,
    saatKonfirmasi: () -> Unit,
    saatSimpanPesanan: () -> Unit,
    saatCatatanBerubah: (String) -> Unit,
    saatMetodeBayarBerubah: (MetodeBayar) -> Unit = {},
    saatTipeOrderBerubah: (TipeOrder) -> Unit = {},
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = saatBatalkan,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = statusKonfirmasiCheckout.judul,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column {
                Text(
                    text = statusKonfirmasiCheckout.deskripsi,
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (statusKonfirmasiCheckout.metodeBayarTunaiTersedia ||
                    statusKonfirmasiCheckout.metodeBayarQrisTersedia
                ) {
                    Text(
                        text = "Metode pembayaran",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (statusKonfirmasiCheckout.metodeBayarTunaiTersedia) {
                            FilterChip(
                                selected = statusKonfirmasiCheckout.metodeBayar == MetodeBayar.Tunai,
                                onClick = { saatMetodeBayarBerubah(MetodeBayar.Tunai) },
                                label = { Text("Tunai") },
                            )
                        }

                        if (statusKonfirmasiCheckout.metodeBayarQrisTersedia) {
                            FilterChip(
                                selected = statusKonfirmasiCheckout.metodeBayar == MetodeBayar.Qris,
                                onClick = { saatMetodeBayarBerubah(MetodeBayar.Qris) },
                                label = { Text("QRIS") },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = "Tipe pesanan",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = statusKonfirmasiCheckout.tipeOrder == TipeOrder.DineIn,
                        onClick = { saatTipeOrderBerubah(TipeOrder.DineIn) },
                        label = { Text("Dine In") },
                    )
                    FilterChip(
                        selected = statusKonfirmasiCheckout.tipeOrder == TipeOrder.TakeAway,
                        onClick = { saatTipeOrderBerubah(TipeOrder.TakeAway) },
                        label = { Text("Take Away") },
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (statusKonfirmasiCheckout.tipeOrder == TipeOrder.DineIn) {
                    Text(
                        text = "Nomor meja",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (statusKonfirmasiCheckout.daftarMeja.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            statusKonfirmasiCheckout.daftarMeja.forEach { meja ->
                                FilterChip(
                                    selected = statusKonfirmasiCheckout.catatan == meja.nomor,
                                    onClick = {
                                        saatCatatanBerubah(
                                            if (statusKonfirmasiCheckout.catatan == meja.nomor) "" else meja.nomor,
                                        )
                                    },
                                    label = { Text("Meja ${meja.nomor}") },
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = statusKonfirmasiCheckout.catatan,
                        onValueChange = saatCatatanBerubah,
                        label = { Text("Atau ketik nomor meja") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { /* close keyboard */ }),
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(
                    onClick = saatSimpanPesanan,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(text = statusKonfirmasiCheckout.labelSimpanPesanan)
                }

                Button(
                    onClick = saatKonfirmasi,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(text = statusKonfirmasiCheckout.labelKonfirmasi)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = saatBatalkan) {
                Text(text = "Batal")
            }
        },
    )
}
