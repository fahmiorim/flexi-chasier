package id.flexi.kasir.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.flexi.kasir.domain.model.CartItem
import id.flexi.kasir.domain.model.Meja
import id.flexi.kasir.domain.model.PaymentMethod
import id.flexi.kasir.domain.model.OrderType
import id.flexi.kasir.domain.model.TableStatus
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.model.TransactionCostBreakdown
import id.flexi.kasir.domain.model.Uang
import id.flexi.kasir.domain.util.sebagaiRupiah
import id.flexi.kasir.ui.component.FlexiDialog
import id.flexi.kasir.ui.component.FlexiDialogHeader
import kotlinx.coroutines.launch

// ═══════════════════════════════════════
// CHECKOUT CONFIRM DIALOG
// ═══════════════════════════════════════

@Composable
internal fun CheckoutConfirmDialog(
    statusKonfirmasiCheckout: CheckoutConfirmStatus,
    saatBatalkan: () -> Unit,
    saatKonfirmasi: () -> Unit,
    saatBayarTunai: (Long) -> Unit = {},
    saatSimpanPesanan: () -> Unit,
    saatSimpanDanCetak: () -> Unit = {},
    saatGabungDanBayar: () -> Unit = {},
    saatGabungDanSimpan: () -> Unit = {},
    saatGabungDanBayarTunai: (Long) -> Unit = {},
    saatCatatanBerubah: (String) -> Unit,
    saatPaymentMethodBerubah: (PaymentMethod) -> Unit = {},
    saatOrderTypeBerubah: (OrderType) -> Unit = {},
    saatMejaDipilih: (String?) -> Unit = {},
    daftarCartItem: List<CartItem> = emptyList(),
    saatSplitBill: (Set<String>, PaymentMethod, Long) -> Unit = { _, _, _ -> },
) {
    val tombolNonAktif = statusKonfirmasiCheckout.apakahSedangMenggabungkan
    val modeSimpan = statusKonfirmasiCheckout.modeSimpan
    val tunaiAktif = statusKonfirmasiCheckout.paymentMethod == PaymentMethod.Cash && !modeSimpan

    val billLain = statusKonfirmasiCheckout.billLainDiMeja

    var tampilSplitBill by remember { mutableStateOf(false) }
    var tampilKonfirmasiGabung by remember { mutableStateOf(false) }
    var modeInputTunai by remember { mutableStateOf(false) }
    var modeKonfirmasiQris by remember { mutableStateOf(false) }
    var inputUangTunai by remember { mutableStateOf("") }
    var gabungMode by remember { mutableStateOf(false) }

    // Hitung total gabungan jika dalam mode Gabung (cart + bill lain)
    val totalGabungan = if (gabungMode && billLain.isNotEmpty()) {
        val totalBillLain = billLain.sumOf { t ->
            val subtotal = t.daftarCartItem.sumOf { item -> item.produk.harga * item.jumlah }
            val breakdown = TransactionCostBreakdown(
                subtotal = Uang.dariRupiah(subtotal),
                potongan = t.potongan,
                biayaLayanan = t.biayaLayanan,
                pajak = t.pajak,
            )
            breakdown.totalAkhir.nilaiRupiah
        }
        statusKonfirmasiCheckout.totalAkhirNilai + totalBillLain
    } else {
        statusKonfirmasiCheckout.totalAkhirNilai
    }

    val nominalTunai = inputUangTunai.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val cukupTunai = nominalTunai >= totalGabungan

    // ── Dialog konfirmasi gabung bill ──
    if (tampilKonfirmasiGabung && !modeSimpan && billLain.isNotEmpty()) {
        KonfirmasiGabungBillDialog(
            daftarCartItem = daftarCartItem,
            billLain = billLain,
            onKonfirmasi = {
                tampilKonfirmasiGabung = false
                if (tunaiAktif) {
                    gabungMode = true
                    modeInputTunai = true
                } else if (statusKonfirmasiCheckout.paymentMethod == PaymentMethod.Qris) {
                    gabungMode = true
                    modeKonfirmasiQris = true
                } else {
                    saatGabungDanBayar()
                }
            },
            onTutup = { tampilKonfirmasiGabung = false },
        )
    }

    if (tampilSplitBill && !modeSimpan && daftarCartItem.isNotEmpty()) {
        SplitBillDialog(
            daftarCartItem = daftarCartItem,
            onKonfirmasi = { ids, metode, uangTunai ->
                tampilSplitBill = false
                saatSplitBill(ids, metode, uangTunai)
            },
            onTutup = { tampilSplitBill = false },
        )
    }

    val checkoutScrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    fun gulirKeBawah() {
        coroutineScope.launch {
            checkoutScrollState.animateScrollTo(checkoutScrollState.maxValue)
        }
    }

    FlexiDialog(
        onDismissRequest = saatBatalkan,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(checkoutScrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FlexiDialogHeader(
                icon = if (modeSimpan) Icons.Default.Save else Icons.Default.ShoppingCartCheckout,
                title = if (modeSimpan) "Simpan Pesanan" else statusKonfirmasiCheckout.judul,
                subtitle = if (!modeSimpan) statusKonfirmasiCheckout.deskripsi else null,
                onClose = saatBatalkan,
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // ── Konten ──
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (modeInputTunai) {
                        CashNumpadInputSection(
                            totalTagihan = totalGabungan,
                            nominalTerinput = inputUangTunai,
                            onNominalChange = {
                                inputUangTunai = it
                                gulirKeBawah()
                            },
                            gabungMode = gabungMode,
                            cukupTunai = cukupTunai,
                            nominalTunai = nominalTunai,
                            onBayarTunai = {
                                if (gabungMode) saatGabungDanBayarTunai(nominalTunai)
                                else saatBayarTunai(nominalTunai)
                            },
                            onKembali = { modeInputTunai = false; gabungMode = false },
                        )
                    } else if (modeKonfirmasiQris) {
                        QrisConfirmationSection(
                            totalGabungan = totalGabungan,
                            gabungMode = gabungMode,
                            onKonfirmasi = {
                                modeKonfirmasiQris = false
                                if (gabungMode) {
                                    gabungMode = false
                                    saatGabungDanBayar()
                                } else {
                                    saatKonfirmasi()
                                }
                            },
                            onKembali = { modeKonfirmasiQris = false; gabungMode = false },
                        )
                    } else {
                        // Metode Pembayaran & Detail Pesanan
                        if (!modeSimpan && (statusKonfirmasiCheckout.PaymentMethodTunaiTersedia ||
                                    statusKonfirmasiCheckout.PaymentMethodQrisTersedia)
                        ) {
                            BagianLabel(icon = Icons.Outlined.Payments, teks = "Metode Pembayaran")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (statusKonfirmasiCheckout.PaymentMethodTunaiTersedia) {
                                    FilterChipMetode(
                                        selected = statusKonfirmasiCheckout.paymentMethod == PaymentMethod.Cash,
                                        onClick = { saatPaymentMethodBerubah(PaymentMethod.Cash) },
                                        icon = Icons.Default.Wallet, label = "Tunai",
                                    )
                                }
                                if (statusKonfirmasiCheckout.PaymentMethodQrisTersedia) {
                                    FilterChipMetode(
                                        selected = statusKonfirmasiCheckout.paymentMethod == PaymentMethod.Qris,
                                        onClick = { saatPaymentMethodBerubah(PaymentMethod.Qris) },
                                        icon = Icons.Default.QrCode, label = "QRIS",
                                    )
                                }
                            }
                        }

                        BagianLabel(icon = Icons.Default.ContentCopy, teks = "Tipe Pesanan")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChipMetode(
                                selected = statusKonfirmasiCheckout.orderType == OrderType.DineIn,
                                onClick = { saatOrderTypeBerubah(OrderType.DineIn) },
                                icon = Icons.Default.TableRestaurant, label = "Dine In",
                            )
                            FilterChipMetode(
                                selected = statusKonfirmasiCheckout.orderType == OrderType.TakeAway,
                                onClick = { saatOrderTypeBerubah(OrderType.TakeAway) },
                                icon = Icons.Default.Add, label = "Take Away",
                            )
                        }

                        if (statusKonfirmasiCheckout.orderType == OrderType.DineIn) {
                            DineInOrderSection(
                                daftarMeja = statusKonfirmasiCheckout.daftarMeja,
                                mejaId = statusKonfirmasiCheckout.mejaId,
                                catatan = statusKonfirmasiCheckout.catatan,
                                onMejaDipilih = { saatMejaDipilih(it) },
                                onCatatanBerubah = saatCatatanBerubah,
                            )
                        }

                        BillLainCard(
                            billLain = billLain,
                            modeSimpan = modeSimpan,
                            totalAkhirNilai = statusKonfirmasiCheckout.totalAkhirNilai,
                            onGabungDanBayar = { tampilKonfirmasiGabung = true },
                        )

                        if (modeSimpan && statusKonfirmasiCheckout.orderType == OrderType.TakeAway) {
                            OutlinedTextField(
                                value = statusKonfirmasiCheckout.catatan,
                                onValueChange = saatCatatanBerubah,
                                label = { Text("Catatan Pesanan (Opsional)") },
                                placeholder = { Text("Contoh: pisah saus") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Done),
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        // Tombol Aksi Utama
                        CheckoutActionButtons(
                            modeSimpan = modeSimpan,
                            tombolNonAktif = tombolNonAktif,
                            tunaiAktif = tunaiAktif,
                            paymentMethod = statusKonfirmasiCheckout.paymentMethod,
                            daftarCartItem = daftarCartItem,
                            labelKonfirmasi = statusKonfirmasiCheckout.labelKonfirmasi,
                            onSimpanDanCetak = saatSimpanDanCetak,
                            onSimpanSaja = saatSimpanPesanan,
                            onBayar = {
                                if (tunaiAktif) {
                                    modeInputTunai = true
                                } else if (statusKonfirmasiCheckout.paymentMethod == PaymentMethod.Qris) {
                                    modeKonfirmasiQris = true
                                } else {
                                    saatKonfirmasi()
                                }
                            },
                            onSplitBill = { tampilSplitBill = true },
                            onBatal = saatBatalkan,
                            onGabungDanSimpan = saatGabungDanSimpan,
                            onGabungDanBayar = { tampilKonfirmasiGabung = true },
                            billLainNonEmpty = billLain.isNotEmpty(),
                        )
                    }
                }
        }
    }
}

// ═══════════════════════════════════════
// INTERNAL SUB-COMPOSABLES FOR CHECKOUT
// ═══════════════════════════════════════

@Composable
private fun CashNumpadInputSection(
    totalTagihan: Long,
    nominalTerinput: String,
    onNominalChange: (String) -> Unit,
    gabungMode: Boolean,
    cukupTunai: Boolean,
    nominalTunai: Long,
    onBayarTunai: () -> Unit,
    onKembali: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
        // Ringkasan Total
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = if (gabungMode) "Total Gabungan" else "Total Belanja",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = totalTagihan.sebagaiRupiah(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Icon(
                    Icons.Default.Wallet, contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                )
            }
        }

        // Rekomendasi uang
        val pecahan = remember(totalTagihan) { hasilRekomendasiNominal(totalTagihan) }
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            pecahan.forEachIndexed { index, nominal ->
                val terpilih = nominalTunai == nominal
                FilterChip(
                    onClick = { onNominalChange(nominal.toString()) },
                    label = {
                        Text(
                            if (index == 0) "Uang Pas" else nominal.sebagaiRupiah(),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    selected = terpilih,
                    modifier = Modifier.heightIn(min = 32.dp),
                )
            }
        }

        // Input uang tunai
        OutlinedTextField(
            value = nominalTerinput,
            onValueChange = { onNominalChange(it.filter { c -> c.isDigit() }) },
            label = { Text("Jumlah Uang Tunai") },
            placeholder = { Text("Masukkan nominal uang pelanggan") },
            leadingIcon = {
                Text(
                    text = "Rp",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 12.dp),
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = nominalTerinput.isNotEmpty() && !cukupTunai,
            supportingText = if (nominalTerinput.isNotEmpty() && !cukupTunai) {
                { Text("Uang tidak mencukupi", color = MaterialTheme.colorScheme.error) }
            } else null,
        )

        // Info kembalian
        if (cukupTunai && nominalTunai > 0L) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            "Kembalian",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            "Dibayar ${nominalTunai.sebagaiRupiah()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        (nominalTunai - totalTagihan).sebagaiRupiah(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }

        Button(
            onClick = onBayarTunai,
            enabled = cukupTunai && nominalTunai > 0L,
            modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(Icons.Default.Wallet, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (gabungMode) "Bayar Gabungan Tunai" else "Konfirmasi Pembayaran Tunai",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        OutlinedButton(
            onClick = onKembali,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Kembali", fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun QrisConfirmationSection(
    totalGabungan: Long,
    gabungMode: Boolean,
    onKonfirmasi: () -> Unit,
    onKembali: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (gabungMode) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (gabungMode) "Total Gabungan" else "Total Belanja",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = totalGabungan.sebagaiRupiah(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
            modifier = Modifier.fillMaxWidth().border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
            ),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.QrCode,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column {
                    Text(
                        text = "Pembayaran QRIS",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Tunjukkan QR Code dinamis dan konfirmasi jika dana sudah terverifikasi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Button(
            onClick = onKonfirmasi,
            modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(Icons.Outlined.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (gabungMode) "Konfirmasi Gabung & Bayar QRIS" else "Konfirmasi Pembayaran QRIS",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        OutlinedButton(
            onClick = onKembali,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Kembali", fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun DineInOrderSection(
    daftarMeja: List<Meja>,
    mejaId: String?,
    catatan: String,
    onMejaDipilih: (String?) -> Unit,
    onCatatanBerubah: (String) -> Unit,
) {
    BagianLabel(icon = Icons.Default.TableRestaurant, teks = "Nomor Meja")
    var tampilPilihMeja by remember { mutableStateOf(false) }
    val mejaTerpilih = daftarMeja.firstOrNull { it.id == mejaId }

    ElevatedCard(
        onClick = { tampilPilihMeja = true },
        modifier = Modifier.fillMaxWidth().border(
            width = 1.dp,
            color = if (mejaTerpilih != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (mejaTerpilih != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.TableRestaurant, contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = if (mejaTerpilih != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mejaTerpilih?.let { "Meja ${it.nomor}" } ?: "Pilih meja makan",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (mejaTerpilih != null) FontWeight.Bold else FontWeight.Medium,
                )
                Text(
                    text = if (mejaTerpilih != null) "Meja terpilih untuk pesanan" else "Ketuk untuk memilih meja makan aktif",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (tampilPilihMeja) {
        PilihMejaDialog(
            daftarMeja = daftarMeja,
            mejaIdTerpilih = mejaId,
            onPilih = { id -> onMejaDipilih(id); tampilPilihMeja = false },
            onTutup = { tampilPilihMeja = false },
        )
    }

    Spacer(modifier = Modifier.height(2.dp))
    OutlinedTextField(
        value = catatan,
        onValueChange = onCatatanBerubah,
        label = { Text("Catatan Pesanan") },
        placeholder = { Text("Contoh: Sendok 3, pedas manis") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Done),
    )
}

@Composable
private fun BillLainCard(
    billLain: List<Transaction>,
    modeSimpan: Boolean,
    totalAkhirNilai: Long,
    onGabungDanBayar: () -> Unit,
) {
    if (!modeSimpan && billLain.isNotEmpty()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ElevatedCard(
            modifier = Modifier.fillMaxWidth().border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp),
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.15f),
            ),
            onClick = onGabungDanBayar,
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Icons.Default.ContentCopy, contentDescription = null,
                        modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.tertiary,
                    )
                    Text(
                        text = "Ada ${billLain.size} bill aktif di meja ini",
                        style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                val totalBillLain = billLain.sumOf { t ->
                    val subtotal = t.daftarCartItem.sumOf { item -> item.produk.harga * item.jumlah }
                    val breakdown = TransactionCostBreakdown(
                        subtotal = Uang.dariRupiah(subtotal), potongan = t.potongan,
                        biayaLayanan = t.biayaLayanan, pajak = t.pajak,
                    )
                    breakdown.totalAkhir.nilaiRupiah
                }
                val totalSemua = totalAkhirNilai + totalBillLain
                Text(
                    text = "Total Semua Gabungan: ${totalSemua.sebagaiRupiah()}",
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun CheckoutActionButtons(
    modeSimpan: Boolean,
    tombolNonAktif: Boolean,
    tunaiAktif: Boolean,
    paymentMethod: PaymentMethod,
    daftarCartItem: List<CartItem>,
    labelKonfirmasi: String,
    onSimpanDanCetak: () -> Unit,
    onSimpanSaja: () -> Unit,
    onBayar: () -> Unit,
    onSplitBill: () -> Unit,
    onBatal: () -> Unit,
    onGabungDanSimpan: () -> Unit,
    onGabungDanBayar: () -> Unit,
    billLainNonEmpty: Boolean,
) {
    if (modeSimpan) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onSimpanDanCetak,
                enabled = !tombolNonAktif,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Simpan & Cetak Struk", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
            FilledTonalButton(
                onClick = onSimpanSaja,
                enabled = !tombolNonAktif,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(text = "Simpan Saja", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (billLainNonEmpty) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Button(
                        onClick = onGabungDanSimpan,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).heightIn(min = 46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Gabung & Simpan",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                    Button(
                        onClick = onGabungDanBayar,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).heightIn(min = 46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text(
                            text = "Gabung & Bayar",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Button(
                onClick = onBayar,
                enabled = !tombolNonAktif,
                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = labelKonfirmasi,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }

            if (daftarCartItem.size > 1) {
                FilledTonalButton(
                    onClick = onSplitBill,
                    enabled = !tombolNonAktif,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.CallSplit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Split Bill (Bagi Tagihan)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            OutlinedButton(
                onClick = onBatal,
                modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(text = "Batal", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ═══════════════════════════════════════
// FILTER CHIP & LABEL
// ═══════════════════════════════════════

@Composable
internal fun FilterChipMetode(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}

@Composable
internal fun BagianLabel(
    icon: ImageVector,
    teks: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = teks,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ═══════════════════════════════════════
// KONFIRMASI GABUNG BILL DIALOG
// ═══════════════════════════════════════

@Composable
internal fun KonfirmasiGabungBillDialog(
    daftarCartItem: List<CartItem>,
    billLain: List<Transaction>,
    onKonfirmasi: () -> Unit,
    onTutup: () -> Unit,
) {
    val totalBillLain = billLain.sumOf { t ->
        val subtotal = t.daftarCartItem.sumOf { item -> item.produk.harga * item.jumlah }
        val breakdown = TransactionCostBreakdown(
            subtotal = Uang.dariRupiah(subtotal),
            potongan = t.potongan,
            biayaLayanan = t.biayaLayanan,
            pajak = t.pajak,
        )
        breakdown.totalAkhir.nilaiRupiah
    }
    val totalCart = daftarCartItem.sumOf { it.produk.harga * it.jumlah }
    val totalGabungan = totalCart + totalBillLain
    val scrollState = rememberScrollState()

    FlexiDialog(onDismissRequest = onTutup) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(scrollState).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FlexiDialogHeader(
                icon = Icons.Default.ContentCopy,
                title = "Gabung Bill",
                subtitle = "${billLain.size + 1} bill akan digabung",
                onClose = onTutup,
            )

            Text(
                text = "Konfirmasi ke customer, bill berikut akan digabung:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Bill Saat Ini
            BillSummaryCard(
                daftarItem = daftarCartItem,
                label = "Bill Saat Ini",
                total = totalCart,
                warna = MaterialTheme.colorScheme.primary,
                badgeColor = MaterialTheme.colorScheme.primary,
            )

            // Bill Lain
            billLain.forEachIndexed { i, bill ->
                val billSubtotal = bill.daftarCartItem.sumOf { it.produk.harga * it.jumlah }
                BillSummaryCard(
                    daftarItem = bill.daftarCartItem,
                    label = "Bill ${i + 2}",
                    total = billSubtotal,
                    warna = MaterialTheme.colorScheme.secondary,
                    badgeColor = MaterialTheme.colorScheme.secondary,
                )
            }

            // Total
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "Total Semua Bill",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "${daftarCartItem.size + billLain.sumOf { it.daftarCartItem.size }} item",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = totalGabungan.sebagaiRupiah(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // Buttons
            Button(
                onClick = onKonfirmasi,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Gabung & Bayar • ${totalGabungan.sebagaiRupiah()}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }

            OutlinedButton(
                onClick = onTutup,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Batal", fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun BillSummaryCard(
    daftarItem: List<CartItem>,
    label: String,
    total: Long,
    warna: androidx.compose.ui.graphics.Color,
    badgeColor: androidx.compose.ui.graphics.Color,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Badge(containerColor = badgeColor) {
                    Text(
                        text = "${daftarItem.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = warna,
                    modifier = Modifier.weight(1f),
                )
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = total.sebagaiRupiah(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = warna,
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = warna.copy(alpha = 0.1f),
                    ),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            daftarItem.forEachIndexed { idx, item ->
                if (idx > 0) HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = "${item.jumlah}x",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = warna,
                        )
                        Text(
                            text = item.produk.nama + (item.varian?.let { " (${it.nama})" } ?: ""),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Text(
                        text = (item.produk.harga * item.jumlah).sebagaiRupiah(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════
// PILIH MEJA DIALOG
// ═══════════════════════════════════════

internal fun buatGridMeja(daftarMeja: List<Meja>): Pair<List<List<String?>>, IntArray> {
    val posisi = daftarMeja.mapNotNull { meja ->
        val sisa = meja.id.removePrefix("grid_")
        if (sisa != meja.id) {
            val parts = sisa.split("_")
            if (parts.size == 2) {
                val b = parts[0].toIntOrNull()
                val k = parts[1].toIntOrNull()
                if (b != null && k != null) Triple(b, k, meja.nomor) else null
            } else null
        } else {
            val lama = meja.id.removePrefix("meja_")
            val parts = lama.split("_r")
            if (parts.size == 2) {
                val koordinat = parts[1].split("_c")
                if (koordinat.size == 2) {
                    val b = koordinat[0].toIntOrNull()
                    val k = koordinat[1].toIntOrNull()
                    if (b != null && k != null) Triple(b, k, meja.nomor) else null
                } else null
            } else null
        }
    }
    val maxBaris = (posisi.maxOfOrNull { it.first } ?: 0) + 1
    val maxKolom = (posisi.maxOfOrNull { it.second } ?: 0) + 1
    val grid = MutableList(maxBaris) { MutableList<String?>(maxKolom) { null } }
    for ((b, k, nomor) in posisi) {
        if (b < maxBaris && k < maxKolom) grid[b][k] = nomor
    }
    return Pair(grid, intArrayOf(maxBaris, maxKolom))
}

@Composable
internal fun PilihMejaDialog(
    daftarMeja: List<Meja>,
    mejaIdTerpilih: String?,
    onPilih: (String?) -> Unit,
    onTutup: () -> Unit,
) {
    val (grid, dimensi) = remember(daftarMeja) { buatGridMeja(daftarMeja) }
    val jumlahBaris = dimensi[0]
    val jumlahKolom = dimensi[1]
    val scrollState = rememberScrollState()

    FlexiDialog(onDismissRequest = onTutup) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(scrollState).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FlexiDialogHeader(
                icon = Icons.Default.TableRestaurant,
                title = "Pilih Meja",
                subtitle = "Ketuk meja untuk memilih",
                onClose = onTutup,
            )

            for (baris in 0..<jumlahBaris) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (kolom in 0..<jumlahKolom) {
                        val nomor = grid[baris][kolom]
                        val meja = daftarMeja.firstOrNull { it.nomor == nomor }
                        val idMeja = meja?.id
                        val terpilih = idMeja == mejaIdTerpilih
                        val terisi = meja?.tableStatus == TableStatus.Occupied

                        if (nomor != null) {
                            MejaGridCard(
                                nomor = nomor,
                                terpilih = terpilih,
                                terisi = terisi,
                                onPilih = { onPilih(if (terpilih) null else idMeja) },
                            )
                        } else {
                            Surface(
                                modifier = Modifier.weight(1f).height(80.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "+",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = onTutup,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tutup", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RowScope.MejaGridCard(
    nomor: String,
    terpilih: Boolean,
    terisi: Boolean,
    onPilih: () -> Unit,
) {
    ElevatedCard(
        onClick = onPilih,
        modifier = Modifier.weight(1f).height(80.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (terpilih) 4.dp else 1.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = when {
                terpilih -> MaterialTheme.colorScheme.primary
                terisi -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.primaryContainer
            },
        ),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (terpilih) Icons.Default.CheckCircle else Icons.Default.TableRestaurant,
                    contentDescription = null,
                    tint = when {
                        terpilih -> MaterialTheme.colorScheme.onPrimary
                        terisi -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                    },
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = nomor,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        terpilih -> MaterialTheme.colorScheme.onPrimary
                        terisi -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                    },
                    textAlign = TextAlign.Center,
                )
                if (terisi) {
                    Text(
                        text = "Terisi",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        fontSize = 9.sp,
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════
// SPLIT BILL DIALOG
// ═══════════════════════════════════════

@Composable
internal fun SplitBillDialog(
    daftarCartItem: List<CartItem>,
    onKonfirmasi: (ids: Set<String>, metode: PaymentMethod, uangTunai: Long) -> Unit,
    onTutup: () -> Unit,
) {
    val semuaKunci = remember(daftarCartItem) {
        daftarCartItem.map { itemKey(it) }.toSet()
    }
    var idTerpilih by remember { mutableStateOf(emptySet<String>()) }
    var metodeBayar by remember { mutableStateOf(PaymentMethod.Cash) }
    var inputUangTunai by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val subtotalTerpilih = remember(idTerpilih, daftarCartItem) {
        daftarCartItem.filter { itemKey(it) in idTerpilih }
            .sumOf { it.produk.harga * it.jumlah }
    }
    val nominalTunai = inputUangTunai.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val cukupTunai = nominalTunai >= subtotalTerpilih
    val kembalianTunai = if (cukupTunai) nominalTunai - subtotalTerpilih else 0L

    FlexiDialog(onDismissRequest = onTutup) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(scrollState).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            FlexiDialogHeader(
                icon = Icons.AutoMirrored.Filled.CallSplit,
                title = "Split Bill",
                subtitle = "Pilih item untuk dipisah",
                onClose = onTutup,
            )

            // Select all toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Centang item yang mau dibayar sekarang.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = {
                    idTerpilih = if (idTerpilih.size == semuaKunci.size) emptySet() else semuaKunci
                }) {
                    Text(
                        text = if (idTerpilih.size == semuaKunci.size) "Hapus Semua" else "Pilih Semua",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            HorizontalDivider(thickness = 0.5.dp)

            // Item list
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                daftarCartItem.forEachIndexed { _, item ->
                    val kunci = itemKey(item)
                    val checked = kunci in idTerpilih

                    ElevatedCard(
                        onClick = {
                            idTerpilih = if (checked) idTerpilih - kunci else idTerpilih + kunci
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (checked) 2.dp else 0.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (checked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                            else MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { tercentang ->
                                    idTerpilih = if (tercentang) idTerpilih + kunci else idTerpilih - kunci
                                },
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.produk.nama,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    if (item.varian != null) {
                                        Text(
                                            text = " (${item.varian.nama})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                                Text(
                                    text = "${item.jumlah} x ${item.produk.harga.sebagaiRupiah()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                text = (item.produk.harga * item.jumlah).sebagaiRupiah(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 0.5.dp)

            // Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        Icons.Outlined.ShoppingCart, contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${idTerpilih.size} item dipilih",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = subtotalTerpilih.sebagaiRupiah(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Payment method
            BagianLabel(icon = Icons.Outlined.Payments, teks = "Metode pembayaran untuk item terpilih")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChipMetode(
                    selected = metodeBayar == PaymentMethod.Cash,
                    onClick = { metodeBayar = PaymentMethod.Cash; inputUangTunai = "" },
                    icon = Icons.Default.Wallet, label = "Tunai",
                )
                FilterChipMetode(
                    selected = metodeBayar == PaymentMethod.Qris,
                    onClick = { metodeBayar = PaymentMethod.Qris; inputUangTunai = "" },
                    icon = Icons.Default.QrCode, label = "QRIS",
                )
            }

            // Cash input when Cash selected
            if (metodeBayar == PaymentMethod.Cash && idTerpilih.isNotEmpty()) {
                CashSplitInputSection(
                    subtotalTerpilih = subtotalTerpilih,
                    inputUangTunai = inputUangTunai,
                    nominalTunai = nominalTunai,
                    cukupTunai = cukupTunai,
                    kembalianTunai = kembalianTunai,
                    onInputChange = { inputUangTunai = it },
                )
            }

            // Buttons
            Button(
                onClick = {
                    val uangTunai = if (metodeBayar == PaymentMethod.Cash) nominalTunai else 0L
                    onKonfirmasi(idTerpilih, metodeBayar, uangTunai)
                },
                enabled = idTerpilih.isNotEmpty() && idTerpilih.size < semuaKunci.size
                    && (metodeBayar != PaymentMethod.Cash || cukupTunai),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.CallSplit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Bayar ${idTerpilih.size} item (${metodeBayar.label})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }

            OutlinedButton(
                onClick = onTutup,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Batal", fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun CashSplitInputSection(
    subtotalTerpilih: Long,
    inputUangTunai: String,
    nominalTunai: Long,
    cukupTunai: Boolean,
    kembalianTunai: Long,
    onInputChange: (String) -> Unit,
) {
    // Pilihan nominal
    val pecahan = remember(subtotalTerpilih) {
        hasilRekomendasiNominal(subtotalTerpilih)
    }

    if (pecahan.isNotEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            pecahan.forEachIndexed { index, nominal ->
                val terpilih = nominalTunai == nominal
                FilterChip(
                    onClick = { onInputChange(nominal.toString()) },
                    label = {
                        Text(
                            if (index == 0) "Uang Pas" else nominal.sebagaiRupiah(),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    selected = terpilih,
                    modifier = Modifier.heightIn(min = 32.dp),
                )
            }
        }
    }

    OutlinedTextField(
        value = inputUangTunai,
        onValueChange = { onInputChange(it.filter { c -> c.isDigit() }) },
        label = { Text("Jumlah Uang Tunai") },
        placeholder = { Text("Masukkan nominal uang pelanggan") },
        leadingIcon = {
            Text(
                text = "Rp",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 12.dp),
            )
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = inputUangTunai.isNotEmpty() && !cukupTunai,
        supportingText = if (inputUangTunai.isNotEmpty() && !cukupTunai) {
            { Text("Uang tidak mencukupi", color = MaterialTheme.colorScheme.error) }
        } else null,
    )

    if (cukupTunai && nominalTunai > 0L) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        "Kembalian",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Dibayar ${nominalTunai.sebagaiRupiah()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    kembalianTunai.sebagaiRupiah(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

// ═══════════════════════════════════════
// UTILITY FUNCTIONS
// ═══════════════════════════════════════

internal fun itemKey(item: CartItem): String = "${item.produk.id}|${item.varian?.nama ?: ""}"

internal fun hasilRekomendasiNominal(total: Long): List<Long> {
    val notes = listOf(5000L, 10000L, 20000L, 50000L, 100000L)
    val combos = mutableListOf<Pair<Long, Int>>()
    for (a in notes) {
        combos.add(Pair(a, 1))
        for (b in notes) {
            combos.add(Pair(a + b, 2))
            for (c in notes) {
                combos.add(Pair(a + b + c, 3))
            }
        }
    }
    val hasil = mutableListOf(total) // Uang Pas
    val best = combos
        .filter { it.first >= total }
        .groupBy { it.first }
        .mapValues { (_, list) -> list.minBy { it.second } }
        .values.sortedBy { it.first }
    var minNotes = Int.MAX_VALUE
    for ((sum, noteCount) in best) {
        if (sum == total) continue
        if (noteCount > minNotes) continue
        hasil.add(sum)
        minNotes = noteCount
        if (hasil.size >= 5) break
    }
    if (100000L >= total && 100000L !in hasil) hasil.add(100000L)
    return hasil
}
