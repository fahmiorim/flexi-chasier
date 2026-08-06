package id.flexi.kasir.ui.settings

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Info
import id.flexi.kasir.ui.component.FlexiCard
import id.flexi.kasir.ui.SinkronMesinStatus
import id.flexi.kasir.ui.labelJudulSinkron
import id.flexi.kasir.ui.labelMetadataSinkron
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import id.flexi.kasir.domain.model.CatalogDisplay
import id.flexi.kasir.domain.model.LebarStruk
import id.flexi.kasir.domain.model.PrinterType
import id.flexi.kasir.domain.model.ReceiptPrintFormat
import id.flexi.kasir.domain.util.sebagaiRupiah
import id.flexi.kasir.ui.component.FlexiDialog
import id.flexi.kasir.ui.component.FlexiDialogHeader

// ═══════════════════════════════════════
// REUSABLE BUILDING BLOCKS
// ═══════════════════════════════════════

@Composable
internal fun KartuBagian(
    judul: String,
    modifier: Modifier = Modifier,
    konten: @Composable ColumnScope.() -> Unit,
) {
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant

    FlexiCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = judul,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            HorizontalDivider(color = outlineVariant.copy(alpha = 0.4f))
            konten()
        }
    }
}

@Composable
internal fun BarisSwitch(
    label: String,
    deskripsi: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = deskripsi,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// ═══════════════════════════════════════
// SECTION: IDENTITAS USAHA
// ═══════════════════════════════════════

@Composable
internal fun BagianIdentitasUsaha(
    modifier: Modifier = Modifier,
    logoUri: String,
    namaUsaha: String,
    alamat: String,
    tagline: String = "",
    perbaruiLogoUri: (String) -> Unit,
    perbaruiNamaUsaha: (String) -> Unit,
    perbaruiAlamat: (String) -> Unit,
    perbaruiTagline: (String) -> Unit = {},
) {
    val context = LocalContext.current

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            perbaruiLogoUri(selectedUri.toString())
            // Cache ke internal cache untuk akses reliable (PDF export, dll)
            try {
                context.contentResolver.openInputStream(selectedUri)?.use { input ->
                    java.io.FileOutputStream(
                        java.io.File(context.cacheDir, "logo_cached.png")
                    ).use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (_: Exception) { }
        }
    }

    KartuBagian(judul = "Identitas Usaha", modifier = modifier) {
        Text(
            text = "Logo Usaha",
            style = MaterialTheme.typography.bodyLarge,
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            if (logoUri.isNotBlank()) {
                val bitmap = remember(logoUri) {
                    try {
                        val uri = Uri.parse(logoUri)
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    } catch (_: Exception) { null }
                }

                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Logo usaha",
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }

        OutlinedButton(
            onClick = { pickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(if (logoUri.isBlank()) "Pilih Logo" else "Ganti Logo")
        }

        OutlinedTextField(
            value = namaUsaha,
            onValueChange = perbaruiNamaUsaha,
            label = { Text("Nama Usaha") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
        )

        OutlinedTextField(
            value = alamat,
            onValueChange = perbaruiAlamat,
            label = { Text("Alamat") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            shape = RoundedCornerShape(12.dp),
        )

        // ── Tagline ──
        OutlinedTextField(
            value = tagline,
            onValueChange = perbaruiTagline,
            label = { Text("Tagline / Slogan") },
            placeholder = { Text("Solusi Digital UMKM Modern") },
            supportingText = { Text("Muncul di sidebar & header struk (opsional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
        )
    }
}

// ═══════════════════════════════════════
// SECTION: TAMPILAN KATALOG
// ═══════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BagianTampilan(
    modifier: Modifier = Modifier,
    catalogDisplay: CatalogDisplay,
    perbaruiCatalogDisplay: (CatalogDisplay) -> Unit,
) {
    KartuBagian(judul = "Tampilan Katalog", modifier = modifier) {
        var dropdownTerbuka by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = dropdownTerbuka,
            onExpandedChange = { dropdownTerbuka = it },
        ) {
            OutlinedTextField(
                value = when (catalogDisplay) {
                    CatalogDisplay.List -> "Daftar (List)"
                    CatalogDisplay.Grid -> "Grid"
                },
                onValueChange = {},
                readOnly = true,
                label = { Text("Bentuk tampilan") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownTerbuka) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
            )

            ExposedDropdownMenu(
                expanded = dropdownTerbuka,
                onDismissRequest = { dropdownTerbuka = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Grid") },
                    onClick = {
                        perbaruiCatalogDisplay(CatalogDisplay.Grid)
                        dropdownTerbuka = false
                    },
                )
                DropdownMenuItem(
                    text = { Text("Daftar (List)") },
                    onClick = {
                        perbaruiCatalogDisplay(CatalogDisplay.List)
                        dropdownTerbuka = false
                    },
                )
            }
        }
    }
}

// ═══════════════════════════════════════
// SECTION: METODE PAYMENT
// ═══════════════════════════════════════

@Composable
internal fun BagianPayment(
    modifier: Modifier = Modifier,
    PaymentMethodTunaiAktif: Boolean,
    PaymentMethodQrisAktif: Boolean,
    perbaruiPaymentMethodTunai: (Boolean) -> Unit,
    perbaruiPaymentMethodQris: (Boolean) -> Unit,
) {
    KartuBagian(judul = "Metode Payment", modifier = modifier) {
        BarisSwitch(
            label = "Tunai",
            deskripsi = "Payment menggunakan uang tunai",
            checked = PaymentMethodTunaiAktif,
            onCheckedChange = perbaruiPaymentMethodTunai,
        )
        BarisSwitch(
            label = "QRIS",
            deskripsi = "Payment scan QRIS",
            checked = PaymentMethodQrisAktif,
            onCheckedChange = perbaruiPaymentMethodQris,
        )
    }
}

// ═══════════════════════════════════════
// SECTION: PRINTER
// ═══════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BagianPrinter(
    printerType: PrinterType,
    printerName: String,
    printerAddress: String,
    perbaruiPrinterType: (PrinterType) -> Unit,
    perbaruiPrinter: (alamat: String, nama: String) -> Unit,
) {
    val context = LocalContext.current
    var dropdownTerbuka by remember { mutableStateOf(false) }
    var pesanBluetooth by remember { mutableStateOf<String?>(null) }

    KartuBagian("Printer") {
        ExposedDropdownMenuBox(
            expanded = dropdownTerbuka,
            onExpandedChange = { dropdownTerbuka = it },
        ) {
            OutlinedTextField(
                value = when (printerType) {
                    PrinterType.None -> "Tidak ada"
                    PrinterType.Bluetooth -> "Bluetooth"
                    PrinterType.Usb -> "USB"
                },
                onValueChange = {},
                readOnly = true,
                label = { Text("Jenis printer") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownTerbuka) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
            )

            ExposedDropdownMenu(
                expanded = dropdownTerbuka,
                onDismissRequest = { dropdownTerbuka = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Tidak ada") },
                    onClick = {
                        perbaruiPrinterType(PrinterType.None)
                        perbaruiPrinter("", "")
                        pesanBluetooth = null
                        dropdownTerbuka = false
                    },
                )
                DropdownMenuItem(
                    text = { Text("Bluetooth") },
                    onClick = {
                        perbaruiPrinterType(PrinterType.Bluetooth)
                        pesanBluetooth = null
                        dropdownTerbuka = false
                    },
                )
                DropdownMenuItem(
                    text = { Text("USB") },
                    onClick = {
                        perbaruiPrinterType(PrinterType.Usb)
                        perbaruiPrinter("usb_auto", "USB Auto Detect")
                        pesanBluetooth = null
                        dropdownTerbuka = false
                    },
                )
            }
        }

        if (printerName.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Terpilih: $printerName",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        if (printerType == PrinterType.Bluetooth) {
            OutlinedButton(
                onClick = @Suppress("DEPRECATION") {
                    pesanBluetooth = null
                    try {
                        val manager = context.getSystemService(android.bluetooth.BluetoothManager::class.java)
                        val adapter = manager?.adapter
                        if (adapter == null) {
                            pesanBluetooth = "Bluetooth tidak tersedia di perangkat ini."
                            return@OutlinedButton
                        }
                        if (!adapter.isEnabled) {
                            pesanBluetooth = "Bluetooth dalam keadaan mati. Nyalakan Bluetooth terlebih dahulu."
                            return@OutlinedButton
                        }
                        val devices = adapter.bondedDevices?.filter { device ->
                            val nama = device.name?.lowercase() ?: ""
                            nama.contains("printer") || nama.contains("thermal") ||
                                nama.contains("pos") || nama.contains("receipt") ||
                                nama.contains("tm-") || nama.contains("epson") ||
                                nama.contains("bixolon")
                        } ?: emptyList()

                        if (devices.isEmpty()) {
                            pesanBluetooth = "Tidak ditemukan printer Bluetooth yang terpasang. Pastikan printer sudah dipairing."
                        } else if (devices.size == 1) {
                            val device = devices.first()
                            perbaruiPrinter(device.address, device.name ?: "Printer Bluetooth")
                            pesanBluetooth = "Printer '${device.name ?: "Printer Bluetooth"}' berhasil ditemukan."
                        } else {
                            val device = devices.first()
                            perbaruiPrinter(device.address, device.name ?: "Printer Bluetooth")
                            pesanBluetooth = "Ditemukan ${devices.size} printer. Menggunakan '${device.name}'."
                        }
                    } catch (_: SecurityException) {
                        pesanBluetooth = "Izin Bluetooth belum diberikan. Berikan izin di Pengaturan > Aplikasi."
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Cari Printer Bluetooth")
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                pesanBluetooth?.let { pesan ->
                    val warna = if (pesan.contains("berhasil")) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = warna.copy(alpha = 0.08f),
                    ) {
                        Text(
                            text = pesan,
                            style = MaterialTheme.typography.bodySmall,
                            color = warna,
                            fontWeight = if (pesan.contains("berhasil")) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "Pastikan printer Bluetooth sudah dipasangkan (pairing) dengan perangkat ini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        if (printerType == PrinterType.Usb) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Printer USB akan terdeteksi otomatis saat tersambung.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════
// SECTION: MANAJEMEN KAS
// ═══════════════════════════════════════

@Composable
internal fun BagianKas(
    modifier: Modifier = Modifier,
    manajemenKasAktif: Boolean,
    perbaruiManajemenKas: (Boolean) -> Unit,
) {
    KartuBagian(judul = "Manajemen Kas", modifier = modifier) {
        BarisSwitch(
            label = "Manajemen Kas",
            deskripsi = "Atur buka/tutup kas setiap shift untuk lacak saldo harian",
            checked = manajemenKasAktif,
            onCheckedChange = perbaruiManajemenKas,
        )
    }
}

// ═══════════════════════════════════════
// SECTION: PENGATURAN STRUK
// ═══════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BagianStruk(
    modifier: Modifier = Modifier,
    namaUsaha: String = "",
    logoUri: String = "",
    alamat: String = "",
    tagline: String = "",
    strukHeader: String,
    strukFooter: String,
    lebarStruk: LebarStruk,
    jumlahCopyCetak: String,
    tampilkanLogoDiStruk: Boolean,
    tampilkanPajakDiStruk: Boolean,
    receiptPrintFormat: ReceiptPrintFormat,
    perbaruiStrukHeader: (String) -> Unit,
    perbaruiStrukFooter: (String) -> Unit,
    perbaruiLebarStruk: (LebarStruk) -> Unit,
    perbaruiJumlahCopyCetak: (String) -> Unit,
    perbaruiTampilkanLogoDiStruk: (Boolean) -> Unit,
    perbaruiTampilkanPajakDiStruk: (Boolean) -> Unit,
    perbaruiReceiptPrintFormat: (ReceiptPrintFormat) -> Unit,
) {
    var dropdownCetak by remember { mutableStateOf(false) }
    var dropdownLebar by remember { mutableStateOf(false) }

    KartuBagian(judul = "Pengaturan Struk", modifier = modifier) {
        // ── Cetak otomatis / manual ──
        ExposedDropdownMenuBox(
            expanded = dropdownCetak,
            onExpandedChange = { dropdownCetak = it },
        ) {
            OutlinedTextField(
                value = when (receiptPrintFormat) {
                    ReceiptPrintFormat.Automatic -> "Otomatis setelah bayar"
                    ReceiptPrintFormat.Manual -> "Cetak manual"
                },
                onValueChange = {},
                readOnly = true,
                label = { Text("Cetak struk") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownCetak) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
            )

            ExposedDropdownMenu(
                expanded = dropdownCetak,
                onDismissRequest = { dropdownCetak = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Otomatis setelah bayar") },
                    onClick = {
                        perbaruiReceiptPrintFormat(ReceiptPrintFormat.Automatic)
                        dropdownCetak = false
                    },
                )
                DropdownMenuItem(
                    text = { Text("Cetak manual") },
                    onClick = {
                        perbaruiReceiptPrintFormat(ReceiptPrintFormat.Manual)
                        dropdownCetak = false
                    },
                )
            }
        }

        // ── Lebar kertas ──
        ExposedDropdownMenuBox(
            expanded = dropdownLebar,
            onExpandedChange = { dropdownLebar = it },
        ) {
            OutlinedTextField(
                value = lebarStruk.label,
                onValueChange = {},
                readOnly = true,
                label = { Text("Lebar kertas") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownLebar) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
            )

            ExposedDropdownMenu(
                expanded = dropdownLebar,
                onDismissRequest = { dropdownLebar = false },
            ) {
                DropdownMenuItem(
                    text = { Text("58 mm") },
                    onClick = {
                        perbaruiLebarStruk(LebarStruk.Mm58)
                        dropdownLebar = false
                    },
                )
                DropdownMenuItem(
                    text = { Text("80 mm") },
                    onClick = {
                        perbaruiLebarStruk(LebarStruk.Mm80)
                        dropdownLebar = false
                    },
                )
            }
        }

        // ── Header struk ──
        OutlinedTextField(
            value = strukHeader,
            onValueChange = perbaruiStrukHeader,
            label = { Text("Header struk") },
            placeholder = { Text("Terima kasih sudah berbelanja") },
            supportingText = { Text("Teks di bagian atas struk (opsional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 1,
            maxLines = 3,
            shape = RoundedCornerShape(12.dp),
        )

        // ── Footer struk ──
        OutlinedTextField(
            value = strukFooter,
            onValueChange = perbaruiStrukFooter,
            label = { Text("Footer struk") },
            placeholder = { Text("Barang yang sudah dibeli tidak dapat dikembalikan") },
            supportingText = { Text("Teks di bagian bawah struk sebelum 'Terima Kasih'") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 1,
            maxLines = 3,
            shape = RoundedCornerShape(12.dp),
        )

        // ── Jumlah copy ──
        OutlinedTextField(
            value = jumlahCopyCetak,
            onValueChange = perbaruiJumlahCopyCetak,
            label = { Text("Jumlah cetak") },
            placeholder = { Text("1") },
            supportingText = { Text("1-5 copy struk") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // ── Toggle logo ──
        BarisSwitch(
            label = "Tampilkan logo di struk",
            deskripsi = "Cetak logo usaha di bagian atas struk",
            checked = tampilkanLogoDiStruk,
            onCheckedChange = perbaruiTampilkanLogoDiStruk,
        )

        // ── Toggle pajak ──
        BarisSwitch(
            label = "Tampilkan rincian pajak",
            deskripsi = "Tampilkan biaya pajak & layanan di struk",
            checked = tampilkanPajakDiStruk,
            onCheckedChange = perbaruiTampilkanPajakDiStruk,
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // ── Preview button ──
        var tampilPreview by remember { mutableStateOf(false) }

        OutlinedButton(
            onClick = { tampilPreview = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Preview Struk", fontWeight = FontWeight.SemiBold)
        }

        if (tampilPreview) {
            DialogPreviewStruk(
                namaUsaha = namaUsaha,
                alamat = alamat,
                logoUri = logoUri,
                tagline = tagline,
                header = strukHeader,
                footer = strukFooter,
                lebar = lebarStruk,
                tampilkanLogo = tampilkanLogoDiStruk,
                tampilkanPajak = tampilkanPajakDiStruk,
                onTutup = { tampilPreview = false },
            )
        }
    }
}

// ═══════════════════════════════════════
// SECTION: PREVIEW STRUK DIALOG
// ═══════════════════════════════════════

@Composable
internal fun DialogPreviewStruk(
    namaUsaha: String,
    alamat: String,
    logoUri: String,
    tagline: String = "",
    header: String,
    footer: String,
    lebar: LebarStruk,
    tampilkanLogo: Boolean,
    tampilkanPajak: Boolean,
    onTutup: () -> Unit,
) {
    val maxChar = when (lebar) {
        LebarStruk.Mm58 -> 32
        LebarStruk.Mm80 -> 48
    }
    val scrollState = rememberScrollState()
    val hScrollState = rememberScrollState()

    // Sample data untuk preview
    val sampleItems = listOf(
        Triple("Kopi Susu", 2, 18000L),
        Triple("Mie Goreng", 1, 15000L),
        Triple("Es Teh Manis", 3, 5000L),
        Triple("Nasi Goreng", 1, 25000L),
    )
    val subtotal = sampleItems.sumOf { (_, qty, price) -> qty * price }
    val totalItem = sampleItems.sumOf { (_, qty, _) -> qty }
    val potongan = 2000L
    val biayaLayanan = 2000L
    val pajak = ((subtotal - potongan) * 0.1).toLong()
    val totalAkhir = subtotal - potongan + biayaLayanan + pajak

    val formatTanggal = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale("id", "ID"))
    val waktuPreview = formatTanggal.format(java.util.Date())

    fun garisP(): String = "═".repeat(maxChar)
    fun garisTitikP(): String = "─".repeat(maxChar)
    fun duaKolom(kiri: String, kanan: String): String {
        val bersihKiri = kiri.take(maxChar - kanan.length - 2)
        val padding = " ".repeat(maxOf(0, maxChar - bersihKiri.length - kanan.length))
        return "$bersihKiri$padding$kanan"
    }
    fun centerText(t: String): String {
        val bersih = t.take(maxChar)
        val paddingKiri = maxOf(0, (maxChar - bersih.length) / 2)
        return " ".repeat(paddingKiri) + bersih
    }

    FlexiDialog(onDismissRequest = onTutup) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(hScrollState)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            FlexiDialogHeader(
                icon = Icons.Default.Info,
                title = "Preview Struk",
                subtitle = "${lebar.label} — ${maxChar} karakter per baris",
                onClose = onTutup,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Kertas struk ──
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = (maxChar * 12).dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                tonalElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    val monoStyle = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = if (lebar == LebarStruk.Mm58) 9.sp else 10.sp,
                        lineHeight = if (lebar == LebarStruk.Mm58) 14.sp else 15.sp,
                    )

                    // Header — Nama Usaha
                    val namaToko = namaUsaha.ifBlank { "FLEXI CASHIER" }
                    Text(centerText(namaToko.uppercase()), style = monoStyle.copy(fontWeight = FontWeight.Bold))

                    // Tagline (jika ada)
                    if (tagline.isNotBlank()) {
                        Text(centerText(tagline), style = monoStyle)
                    }

                    // Alamat usaha
                    if (alamat.isNotBlank()) {
                        alamat.lines().forEach { baris ->
                            Text(centerText(baris), style = monoStyle)
                        }
                    }

                    // Logo (teks fallback jika logoUri ada)
                    if (tampilkanLogo && logoUri.isNotBlank()) {
                        Text(centerText("[LOGO]"), style = monoStyle)
                    }

                    // Header custom
                    if (header.isNotBlank()) {
                        header.lines().forEach { baris ->
                            Text(centerText(baris), style = monoStyle)
                        }
                    }

                    Text(garisP(), style = monoStyle)

                    // Info transaksi
                    Text(centerText("#A1B2C3D4"), style = monoStyle)
                    Text(centerText(waktuPreview), style = monoStyle)
                    Text("Dine In", style = monoStyle)
                    Text("No. Antrian: 5", style = monoStyle.copy(fontWeight = FontWeight.Bold))

                    Text(garisP(), style = monoStyle)

                    // Daftar item
                    sampleItems.forEach { (nama, qty, price) ->
                        val hargaStr = price.sebagaiRupiah()
                        val subtotalStr = (qty * price).sebagaiRupiah()
                        Text(nama.take(maxChar - 3), style = monoStyle)
                        val barisItem = "$qty x $hargaStr"
                        val padding = " ".repeat(maxOf(0, maxChar - barisItem.length - subtotalStr.length - 1))
                        Text("$barisItem$padding$subtotalStr", style = monoStyle.copy(fontWeight = FontWeight.Bold))
                    }

                    Text(garisP(), style = monoStyle)

                    // Rincian
                    Text(duaKolom("Subtotal", subtotal.sebagaiRupiah()), style = monoStyle)
                    Text(duaKolom("Potongan", "-${potongan.sebagaiRupiah()}"), style = monoStyle)

                    if (tampilkanPajak) {
                        Text(duaKolom("Biaya Layanan", biayaLayanan.sebagaiRupiah()), style = monoStyle)
                        Text(duaKolom("Pajak 10%", pajak.sebagaiRupiah()), style = monoStyle)
                    }

                    Text(garisTitikP(), style = monoStyle)
                    Text(duaKolom("TOTAL", totalAkhir.sebagaiRupiah()), style = monoStyle.copy(fontWeight = FontWeight.Bold))

                    Text(garisP(), style = monoStyle)

                    // Footer custom
                    if (footer.isNotBlank()) {
                        footer.lines().forEach { baris ->
                            Text(centerText(baris), style = monoStyle)
                        }
                        Text(garisP(), style = monoStyle)
                    }

                    // Footer standar
                    Text(centerText("Terima Kasih"), style = monoStyle.copy(fontWeight = FontWeight.Bold))
                    Text(centerText("www.flexikasir.id"), style = monoStyle)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onTutup,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Tutup", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ═══════════════════════════════════════
// SECTION: SINKRONISASI
// ═══════════════════════════════════════

@Composable
internal fun BagianSinkronisasi(
    modifier: Modifier = Modifier,
    sinkronMesinStatus: SinkronMesinStatus,
    saatSinkronkan: () -> Unit,
) {
    val warnaStatus = when (val s = sinkronMesinStatus.status) {
        id.flexi.kasir.domain.model.SyncStatus.Synced -> MaterialTheme.colorScheme.primary
        id.flexi.kasir.domain.model.SyncStatus.LocalChanges -> MaterialTheme.colorScheme.tertiary
        id.flexi.kasir.domain.model.SyncStatus.Syncing -> MaterialTheme.colorScheme.primary
        is id.flexi.kasir.domain.model.SyncStatus.Gagal -> MaterialTheme.colorScheme.error
        id.flexi.kasir.domain.model.SyncStatus.Never -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    KartuBagian(judul = "Sinkronisasi", modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = warnaStatus.copy(alpha = 0.08f),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (sinkronMesinStatus.apakahSedangBerjalan) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = warnaStatus,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = warnaStatus,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = labelJudulSinkron(sinkronMesinStatus),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = warnaStatus,
                    )
                    Text(
                        text = labelMetadataSinkron(sinkronMesinStatus),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (sinkronMesinStatus.jumlahPerubahanLokal > 0) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${sinkronMesinStatus.jumlahPerubahanLokal} perubahan lokal belum dikirim ke server.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Button(
            onClick = saatSinkronkan,
            enabled = !sinkronMesinStatus.apakahSedangBerjalan,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = if (sinkronMesinStatus.apakahSedangBerjalan) "Menyinkronkan..." else "Sinkronkan Sekarang",
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ═══════════════════════════════════════
// SECTION: LAINNYA
// ═══════════════════════════════════════

@Composable
internal fun BagianLainnya(
    modifier: Modifier = Modifier,
    suaraNotifikasiAktif: Boolean,
    satuanStokDefault: String,
    jumlahTopFavorit: String,
    perbaruiSuaraNotifikasi: (Boolean) -> Unit,
    perbaruiSatuanStokDefault: (String) -> Unit,
    perbaruiJumlahTopFavorit: (String) -> Unit,
) {
    KartuBagian(judul = "Lainnya", modifier = modifier) {
        BarisSwitch(
            label = "Suara notifikasi",
            deskripsi = "Bunyi setiap scan / tambah item",
            checked = suaraNotifikasiAktif,
            onCheckedChange = perbaruiSuaraNotifikasi,
        )

        OutlinedTextField(
            value = satuanStokDefault,
            onValueChange = perbaruiSatuanStokDefault,
            label = { Text("Satuan stok default") },
            placeholder = { Text("pcs, cup, gelas, gram") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
        )

        OutlinedTextField(
            value = jumlahTopFavorit,
            onValueChange = perbaruiJumlahTopFavorit,
            label = { Text("Jumlah produk favorit otomatis") },
            placeholder = { Text("10") },
            supportingText = { Text("Produk paling laris akan otomatis jadi favorit") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
        )
    }
}
