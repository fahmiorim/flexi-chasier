package id.flexi.kasir.ui.cashregister

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import id.flexi.kasir.domain.model.CashMutationType
import id.flexi.kasir.domain.model.Setoran
import id.flexi.kasir.domain.util.sebagaiRupiah
import id.flexi.kasir.ui.component.FlexiDialog
import id.flexi.kasir.ui.component.FlexiDialogActions
import id.flexi.kasir.ui.component.FlexiDialogHeader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ═══════════════════════════════════════
// DIALOG BUKA KAS
// ═══════════════════════════════════════

@Composable
internal fun DialogBukaKas(
    tanggalEpochMili: Long,
    jam: Int, menit: Int,
    nominal: String, catatan: String,
    apakahSedangMemproses: Boolean, pesanError: String?,
    perbaruiTanggal: (Long) -> Unit, perbaruiJam: (Int, Int) -> Unit,
    perbaruiNominal: (String) -> Unit, perbaruiCatatan: (String) -> Unit,
    bukaKas: () -> Unit, tutup: () -> Unit,
) {
    FlexiDialog(onDismissRequest = tutup) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FlexiDialogHeader(
                icon = Icons.Default.Add, title = "Buka Kas",
                subtitle = "Isi saldo awal untuk memulai shift",
                onClose = tutup,
            )

            // Date & Time — disabled with visible colors
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(tanggalEpochMili)),
                    onValueChange = {}, readOnly = true, enabled = false,
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    label = { Text("Tanggal") },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    ),
                )
                OutlinedTextField(
                    value = String.format("%02d:%02d", jam, menit),
                    onValueChange = {}, readOnly = true, enabled = false,
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    label = { Text("Jam") },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    ),
                )
            }

            // Nominal
            OutlinedTextField(
                value = nominal, onValueChange = { perbaruiNominal(it.filter { c -> c.isDigit() }) },
                label = { Text("Saldo Awal (Rp)") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = RoundedCornerShape(12.dp),
                visualTransformation = RupiahTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
            )

            // Catatan
            OutlinedTextField(
                value = catatan, onValueChange = perbaruiCatatan,
                label = { Text("Catatan (opsional)") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = RoundedCornerShape(12.dp),
            )

            if (pesanError != null) {
                Text(pesanError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            FlexiDialogActions(
                onBatal = tutup, onKonfirmasi = bukaKas,
                labelKonfirmasi = "Buka Kas", konfirmasiIcon = Icons.Default.Add,
                enabled = !apakahSedangMemproses && nominal.isNotBlank(),
            )
        }
    }
}

// ═══════════════════════════════════════
// DIALOG MUTASI
// ═══════════════════════════════════════

@Composable
internal fun DialogMutasi(
    tipe: CashMutationType, nominal: String, catatan: String,
    apakahSedangMemproses: Boolean, pesanError: String?,
    perbaruiTipe: (CashMutationType) -> Unit, perbaruiNominal: (String) -> Unit,
    perbaruiCatatan: (String) -> Unit, simpan: () -> Unit, tutup: () -> Unit,
) {
    FlexiDialog(onDismissRequest = tutup) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FlexiDialogHeader(
                icon = Icons.AutoMirrored.Filled.List, title = "Catat Mutasi",
                subtitle = "Pemasukan atau pengeluaran kas",
                onClose = tutup,
            )

            // Tipe toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = tipe == CashMutationType.Pemasukan,
                        onClick = { perbaruiTipe(CashMutationType.Pemasukan) },
                        shape = RoundedCornerShape(12.dp),
                    ) { Text("Pemasukan", fontWeight = FontWeight.Medium) }
                    SegmentedButton(
                        selected = tipe == CashMutationType.Pengeluaran,
                        onClick = { perbaruiTipe(CashMutationType.Pengeluaran) },
                        shape = RoundedCornerShape(12.dp),
                    ) { Text("Pengeluaran", fontWeight = FontWeight.Medium) }
                }
            }

            OutlinedTextField(
                value = nominal, onValueChange = { perbaruiNominal(it.filter { c -> c.isDigit() }) },
                label = { Text("Nominal (Rp)") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = RoundedCornerShape(12.dp),
                visualTransformation = RupiahTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
            )

            OutlinedTextField(
                value = catatan, onValueChange = perbaruiCatatan,
                label = { Text("Catatan") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = RoundedCornerShape(12.dp),
            )

            if (pesanError != null) {
                Text(pesanError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            FlexiDialogActions(
                onBatal = tutup, onKonfirmasi = simpan,
                labelKonfirmasi = "Simpan", konfirmasiIcon = Icons.AutoMirrored.Filled.List,
                enabled = !apakahSedangMemproses && nominal.isNotBlank(),
            )
        }
    }
}

// ═══════════════════════════════════════
// DIALOG TUTUP KAS
// ═══════════════════════════════════════

@Composable
internal fun DialogTutupKas(
    state: CashRegisterUiState.KasAktif,
    perbaruiSaldoFisik: (String) -> Unit,
    tutupKas: () -> Unit,
    tutup: () -> Unit,
) {
    // saldoSaatIni SUDAH termasuk saldoAwal + tunai + pemasukan - pengeluaran - setoran.
    // Cukup parse langsung — tidak perlu tambah saldoAwal lagi.
    val expectedFisikAngka = remember(state.saldoSaatIni) {
        state.saldoSaatIni
            .replace(Regex("[^0-9-]"), "")
            .toLongOrNull() ?: 0L
    }
    val saldoFisikAngka = remember(state.saldoFisikInput) {
        state.saldoFisikInput.toLongOrNull()
    }
    val selisih = remember(expectedFisikAngka, saldoFisikAngka) {
        if (saldoFisikAngka != null) saldoFisikAngka - expectedFisikAngka else null
    }

    FlexiDialog(onDismissRequest = tutup) {
        Column(
            modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FlexiDialogHeader(
                icon = Icons.AutoMirrored.Filled.Logout, title = "Tutup Kas",
                subtitle = "Hitung dan verifikasi saldo fisik",
                onClose = tutup,
            )

            // Perhitungan saldo
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Perhitungan Saldo",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Saldo Awal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(state.kas.saldoAwal.nilaiRupiah.sebagaiRupiah(), style = MaterialTheme.typography.bodySmall)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("+ Penjualan Tunai", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(state.penjualanTunai, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("+ Pemasukan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(state.totalPemasukan, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("- Pengeluaran", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val pengeluaranAngka = state.totalPengeluaran.filter { it.isDigit() }.toLongOrNull() ?: 0L
                        Text("(${pengeluaranAngka.sebagaiRupiah()})",
                            style = MaterialTheme.typography.bodySmall, color = RedAksen)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("- Setoran", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val setoranAngka = state.totalSetoran.filter { it.isDigit() }.toLongOrNull() ?: 0L
                        Text("(${setoranAngka.sebagaiRupiah()})",
                            style = MaterialTheme.typography.bodySmall, color = RedAksen)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Saldo Saat Ini", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        Text(expectedFisikAngka.sebagaiRupiah(), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Penjualan QRIS", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(state.penjualanQRIS, style = MaterialTheme.typography.bodySmall, color = BlueAksen)
                    }
                }
            }

            // Input saldo fisik
            OutlinedTextField(
                value = state.saldoFisikInput, onValueChange = { perbaruiSaldoFisik(it.filter { c -> c.isDigit() }) },
                label = { Text("Saldo Fisik (Rp)") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = RoundedCornerShape(12.dp),
                visualTransformation = RupiahTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
            )

            // Selisih
            if (selisih != null && state.saldoFisikInput.isNotBlank()) {
                val isLebih = selisih > 0
                val isKurang = selisih < 0
                val warnaSelisih = when {
                    isLebih -> GreenAksen
                    isKurang -> RedAksen
                    else -> MaterialTheme.colorScheme.onSurface
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = warnaSelisih.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Selisih",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = warnaSelisih,
                        )
                        Text(
                            text = buildString {
                                if (isLebih) append("+")
                                append(kotlin.math.abs(selisih).sebagaiRupiah())
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = warnaSelisih,
                        )
                    }
                }
            }

            if (state.pesanErrorTutup != null) {
                Text(state.pesanErrorTutup, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            FlexiDialogActions(
                onBatal = tutup, onKonfirmasi = tutupKas,
                labelKonfirmasi = "Tutup Kas", konfirmasiIcon = Icons.AutoMirrored.Filled.Logout,
                enabled = !state.apakahSedangTutup && state.saldoFisikInput.isNotBlank(),
            )
        }
    }
}

// ═══════════════════════════════════════
// DIALOG SETORAN
// ═══════════════════════════════════════

@Composable
internal fun DialogSetoran(
    nominal: String, catatan: String,
    apakahSedangMemproses: Boolean, pesanError: String?,
    perbaruiNominal: (String) -> Unit, perbaruiCatatan: (String) -> Unit,
    simpan: () -> Unit, tutup: () -> Unit,
) {
    FlexiDialog(onDismissRequest = tutup) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FlexiDialogHeader(
                icon = Icons.Default.Add, title = "Catat Setoran",
                subtitle = "Setorkan uang kas ke bank atau keperluan lain",
                onClose = tutup,
            )

            OutlinedTextField(
                value = nominal, onValueChange = { perbaruiNominal(it.filter { c -> c.isDigit() }) },
                label = { Text("Nominal Setoran (Rp)") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = RoundedCornerShape(12.dp),
                visualTransformation = RupiahTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
            )

            OutlinedTextField(
                value = catatan, onValueChange = perbaruiCatatan,
                label = { Text("Catatan (opsional)") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = RoundedCornerShape(12.dp),
            )

            if (pesanError != null) {
                Text(pesanError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            FlexiDialogActions(
                onBatal = tutup, onKonfirmasi = simpan,
                labelKonfirmasi = "Simpan", konfirmasiIcon = Icons.Default.Add,
                enabled = !apakahSedangMemproses && nominal.isNotBlank(),
            )
        }
    }
}

// ═══════════════════════════════════════
// DIALOG EDIT SETORAN
// ═══════════════════════════════════════

@Composable
internal fun DialogEditSetoran(
    setoran: Setoran,
    catatan: String,
    apakahSedangMemproses: Boolean,
    pesanError: String?,
    perbaruiCatatan: (String) -> Unit,
    simpan: () -> Unit,
    hapus: () -> Unit,
    tutup: () -> Unit,
) {
    FlexiDialog(onDismissRequest = tutup) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FlexiDialogHeader(
                icon = Icons.AutoMirrored.Filled.List, title = "Edit Setoran",
                subtitle = "Nominal: ${setoran.nominal.nilaiRupiah.sebagaiRupiah()}",
                onClose = tutup,
            )

            OutlinedTextField(
                value = catatan, onValueChange = perbaruiCatatan,
                label = { Text("Catatan") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true, shape = RoundedCornerShape(12.dp),
            )

            if (pesanError != null) {
                Text(pesanError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = hapus,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hapus", fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = simpan,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    enabled = !apakahSedangMemproses,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Simpan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
// ═══════════════════════════════════════
// VISUAL TRANSFORMATION: Rupiah
// ═══════════════════════════════════════

private object RupiahTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val bersih = text.text.filter { it.isDigit() }
        if (bersih.isEmpty()) return TransformedText(AnnotatedString(""), OffsetMapping.Identity)

        val formatted = bersih.reversed().chunked(3).joinToString(".").reversed()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                if (offset >= bersih.length) return formatted.length
                val prefix = bersih.take(offset)
                return prefix.reversed().chunked(3).joinToString(".").reversed().length
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                if (offset >= formatted.length) return bersih.length
                return formatted.take(offset).count { it.isDigit() }
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
