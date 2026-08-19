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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
// DATA CLASSES
// ═══════════════════════════════════════

/** Item hasil scan printer Bluetooth. */
data class ScanPrinterItem(
    val alamatMac: String,
    val nama: String,
    val sudahDipasangkan: Boolean,
)

// ═══════════════════════════════════════
// DIALOG PILIH PRINTER BLUETOOTH
// ═══════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogPilihPrinter(
    printerAddress: String,
    onPilih: (alamat: String, nama: String) -> Unit,
    onTutup: () -> Unit,
) {
    val context = LocalContext.current
    var daftarDevice by remember { mutableStateOf(emptyList<ScanPrinterItem>()) }
    var sedangScan by remember { mutableStateOf(false) }
    var scanReceiver by remember { mutableStateOf<android.content.BroadcastReceiver?>(null) }

    // Load paired devices saat dialog dibuka
    LaunchedEffect(Unit) {
        try {
            val manager = context.getSystemService(android.bluetooth.BluetoothManager::class.java)
            val adapter = manager?.adapter
            if (adapter != null && adapter.isEnabled) {
                val paired = adapter.bondedDevices?.mapNotNull { device ->
                    if (device.name?.isNotBlank() == true) {
                        ScanPrinterItem(device.address, device.name!!, true)
                    } else null
                } ?: emptyList()
                daftarDevice = paired
            }
        } catch (_: SecurityException) {}
    }

    // Cleanup receiver
    DisposableEffect(Unit) {
        onDispose {
            scanReceiver?.let {
                try { context.unregisterReceiver(it) } catch (_: Exception) {}
            }
            try {
                val manager = context.getSystemService(android.bluetooth.BluetoothManager::class.java)
                manager?.adapter?.cancelDiscovery()
            } catch (_: Exception) {}
        }
    }

    id.flexi.kasir.ui.component.FlexiDialog(onDismissRequest = onTutup) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            id.flexi.kasir.ui.component.FlexiDialogHeader(
                icon = Icons.Default.CheckCircle,
                title = "Pilih Printer Bluetooth",
                subtitle = "Ketuk perangkat untuk memilih",
                onClose = onTutup,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Daftar perangkat
            if (daftarDevice.isEmpty() && !sedangScan) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                ) {
                    Text(
                        text = "Tidak ada perangkat Bluetooth yang dipasangkan.\nNyalakan Bluetooth dan scan perangkat baru.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.heightIn(max = 300.dp),
                ) {
                    items(daftarDevice.size) { index ->
                        val device = daftarDevice[index]
                        Surface(
                            onClick = { onPilih(device.alamatMac, device.nama) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (device.alamatMac == printerAddress)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (device.alamatMac == printerAddress) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(device.nama, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text(device.alamatMac, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (device.sudahDipasangkan) {
                                    Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
                                        Text("Tersambung", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tombol Scan
            OutlinedButton(
                onClick = @Suppress("DEPRECATION") {
                    sedangScan = true
                    try {
                        val manager = context.getSystemService(android.bluetooth.BluetoothManager::class.java)
                        val adapter = manager?.adapter
                        if (adapter == null || !adapter.isEnabled) {
                            sedangScan = false
                            return@OutlinedButton
                        }
                        adapter.cancelDiscovery()

                        val namaDikenal = daftarDevice.map { it.alamatMac }.toMutableSet()

                        val filter = android.content.IntentFilter().apply {
                            addAction(android.bluetooth.BluetoothDevice.ACTION_FOUND)
                            addAction(android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                        }

                        scanReceiver = object : android.content.BroadcastReceiver() {
                            override fun onReceive(ctx: android.content.Context?, intent: android.content.Intent?) {
                                when (intent?.action) {
                                    android.bluetooth.BluetoothDevice.ACTION_FOUND -> {
                                        val device = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                            intent.getParcelableExtra(android.bluetooth.BluetoothDevice.EXTRA_DEVICE, android.bluetooth.BluetoothDevice::class.java)
                                        } else {
                                            @Suppress("DEPRECATION")
                                            intent.getParcelableExtra(android.bluetooth.BluetoothDevice.EXTRA_DEVICE)
                                        }
                                        device ?: return
                                        if (device.name?.isNotBlank() == true && namaDikenal.add(device.address)) {
                                            daftarDevice = daftarDevice + ScanPrinterItem(device.address, device.name!!, device.bondState == android.bluetooth.BluetoothDevice.BOND_BONDED)
                                        }
                                    }
                                    android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                                        sedangScan = false
                                        try { context.unregisterReceiver(this) } catch (_: Exception) {}
                                        scanReceiver = null
                                    }
                                }
                            }
                        }
                        context.registerReceiver(scanReceiver, filter)
                        adapter.startDiscovery()
                    } catch (_: SecurityException) {
                        sedangScan = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                enabled = !sedangScan,
            ) {
                if (sedangScan) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.height(16.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Memindai perangkat baru...")
                } else {
                    Text("Scan Perangkat Baru")
                }
            }
        }
    }
}

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
            try {
                // Simpan ke internal storage (persistent, tidak terhapus saat cache clear)
                val logoFile = java.io.File(context.filesDir, "logo_usaha.png")
                context.contentResolver.openInputStream(selectedUri)?.use { input ->
                    java.io.FileOutputStream(logoFile).use { output ->
                        input.copyTo(output)
                    }
                }
                // Simpan path file, bukan content URI (yang bisa expired)
                perbaruiLogoUri(logoFile.absolutePath)
                // Juga cache untuk PDF export
                java.io.File(context.cacheDir, "logo_cached.png").let { cacheFile ->
                    java.io.FileInputStream(logoFile).use { input ->
                        java.io.FileOutputStream(cacheFile).use { output ->
                            input.copyTo(output)
                        }
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
                        when {
                            // 1. File path langsung (persistent)
                            logoUri.startsWith("/") || logoUri.contains("files/") -> {
                                BitmapFactory.decodeFile(logoUri)
                            }
                            // 2. Cache file
                            java.io.File(context.cacheDir, "logo_cached.png").exists() -> {
                                BitmapFactory.decodeFile(java.io.File(context.cacheDir, "logo_cached.png").absolutePath)
                            }
                            // 3. Content URI (legacy)
                            logoUri.startsWith("content://") -> {
                                val uri = Uri.parse(logoUri)
                                context.contentResolver.openInputStream(uri)?.use { stream ->
                                    BitmapFactory.decodeStream(stream)
                                }
                            }
                            // 4. File URI
                            logoUri.startsWith("file://") -> {
                                BitmapFactory.decodeFile(Uri.parse(logoUri).path)
                            }
                            else -> null
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
    saatTestPrint: () -> Unit = {},
) {
    val context = LocalContext.current
    var dropdownTerbuka by remember { mutableStateOf(false) }
    var pesanBluetooth by remember { mutableStateOf<String?>(null) }
    var sedangScan by remember { mutableStateOf(false) }
    var daftarPrinterScan by remember { mutableStateOf(emptyList<ScanPrinterItem>()) }
    var scanReceiver by remember { mutableStateOf<android.content.BroadcastReceiver?>(null) }
    var testPrintStatus by remember { mutableStateOf<String?>(null) }

    // Cleanup receiver saat composable leave composition
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            scanReceiver?.let {
                try { context.unregisterReceiver(it) } catch (_: Exception) {}
            }
            try {
                val manager = context.getSystemService(android.bluetooth.BluetoothManager::class.java)
                manager?.adapter?.cancelDiscovery()
            } catch (_: Exception) {}
        }
    }

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

            // ── Tombol Test Print ──
            OutlinedButton(
                onClick = {
                    testPrintStatus = "Mengirim test print..."
                    saatTestPrint()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Test Print")
            }

            testPrintStatus?.let { status ->
                val warna = if (status.contains("berhasil")) {
                    MaterialTheme.colorScheme.primary
                } else if (status.contains("Mengirim")) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.error
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = warna.copy(alpha = 0.08f),
                ) {
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall,
                        color = warna,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    )
                }
            }
        }

        if (printerType == PrinterType.Bluetooth) {
            // ── Tombol buka modal pilih printer ──
            var tampilDialog by remember { mutableStateOf(false) }

            OutlinedButton(
                onClick = { tampilDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Pilih Printer Bluetooth")
            }

            // ── Modal dialog pilih printer ──
            if (tampilDialog) {
                DialogPilihPrinter(
                    printerAddress = printerAddress,
                    onPilih = { alamat, nama ->
                        perbaruiPrinter(alamat, nama)
                        tampilDialog = false
                    },
                    onTutup = { tampilDialog = false },
                )
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
// SECTION: PRINTER DAPUR
// ═══════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BagianPrinterDapur(
    printerDapurAktif: Boolean,
    printerDapurType: PrinterType,
    printerDapurName: String,
    printerDapurAddress: String,
    perbaruiPrinterDapurAktif: (Boolean) -> Unit,
    perbaruiPrinterDapurType: (PrinterType) -> Unit,
    perbaruiPrinterDapur: (alamat: String, nama: String) -> Unit,
) {
    val context = LocalContext.current
    var dropdownTerbuka by remember { mutableStateOf(false) }
    var pesanBluetooth by remember { mutableStateOf<String?>(null) }
    var sedangScan by remember { mutableStateOf(false) }
    var daftarPrinterScan by remember { mutableStateOf(emptyList<ScanPrinterItem>()) }
    var scanReceiver by remember { mutableStateOf<android.content.BroadcastReceiver?>(null) }

    // Cleanup receiver saat composable leave composition
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            scanReceiver?.let {
                try { context.unregisterReceiver(it) } catch (_: Exception) {}
            }
            try {
                val manager = context.getSystemService(android.bluetooth.BluetoothManager::class.java)
                manager?.adapter?.cancelDiscovery()
            } catch (_: Exception) {}
        }
    }

    KartuBagian("Printer Dapur") {
        BarisSwitch(
            label = "Aktifkan Printer Dapur",
            deskripsi = "Cetak struk dapur (kitchen ticket) secara otomatis",
            checked = printerDapurAktif,
            onCheckedChange = perbaruiPrinterDapurAktif,
        )

        if (printerDapurAktif) {
            ExposedDropdownMenuBox(
                expanded = dropdownTerbuka,
                onExpandedChange = { dropdownTerbuka = it },
            ) {
                OutlinedTextField(
                    value = when (printerDapurType) {
                        PrinterType.None -> "Tidak ada"
                        PrinterType.Bluetooth -> "Bluetooth"
                        PrinterType.Usb -> "USB"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Jenis printer dapur") },
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
                            perbaruiPrinterDapurType(PrinterType.None)
                            perbaruiPrinterDapur("", "")
                            pesanBluetooth = null
                            dropdownTerbuka = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Bluetooth") },
                        onClick = {
                            perbaruiPrinterDapurType(PrinterType.Bluetooth)
                            pesanBluetooth = null
                            dropdownTerbuka = false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("USB") },
                        onClick = {
                            perbaruiPrinterDapurType(PrinterType.Usb)
                            perbaruiPrinterDapur("usb_auto", "USB Auto Detect")
                            pesanBluetooth = null
                            dropdownTerbuka = false
                        },
                    )
                }
            }

            if (printerDapurName.isNotBlank()) {
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
                            text = "Terpilih: $printerDapurName",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            if (printerDapurType == PrinterType.Bluetooth) {
                var tampilDialog by remember { mutableStateOf(false) }

                OutlinedButton(
                    onClick = { tampilDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Pilih Printer Bluetooth")
                }

                if (tampilDialog) {
                    DialogPilihPrinter(
                        printerAddress = printerDapurAddress,
                        onPilih = { alamat, nama ->
                            perbaruiPrinterDapur(alamat, nama)
                            tampilDialog = false
                        },
                        onTutup = { tampilDialog = false },
                    )
                }
            }

            if (printerDapurType == PrinterType.Usb) {
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
                            text = "Printer USB dapur akan terdeteksi otomatis saat tersambung.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
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
// SECTION: PAJAK & BIAYA LAYANAN
// ═══════════════════════════════════════

@Composable
internal fun BagianPajakDanBiaya(
    modifier: Modifier = Modifier,
    basisPoinPajak: String,
    biayaLayanan: String,
    perbaruiBasisPoinPajak: (String) -> Unit,
    perbaruiBiayaLayanan: (String) -> Unit,
) {
    KartuBagian(judul = "Pajak & Biaya Layanan", modifier = modifier) {
        OutlinedTextField(
            value = basisPoinPajak,
            onValueChange = perbaruiBasisPoinPajak,
            label = { Text("Pajak (basis poin)") },
            placeholder = { Text("mis. 1100 untuk 11%") },
            supportingText = { Text("0 berarti tanpa pajak. Diterapkan otomatis per transaksi.") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
        )
        OutlinedTextField(
            value = biayaLayanan,
            onValueChange = perbaruiBiayaLayanan,
            label = { Text("Biaya Layanan (Rp)") },
            placeholder = { Text("mis. 2000") },
            supportingText = { Text("Biaya tetap yang dibebankan per transaksi.") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
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
                    val namaToko = namaUsaha.ifBlank { "FLEXI KASIR" }
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
