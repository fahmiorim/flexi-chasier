package id.flexi.kasir.print

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import id.flexi.kasir.domain.model.LebarStruk
import id.flexi.kasir.domain.model.StoreSetting
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.util.hitungKembalian
import id.flexi.kasir.domain.util.hitungSubtotalKeranjangUang
import id.flexi.kasir.domain.util.hitungTotalAkhirTransaction
import id.flexi.kasir.domain.util.sebagaiRupiah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Hasil operasi pencetakan struk.
 */
sealed class PrintResult {
    data object Berhasil : PrintResult()
    data class Gagal(val pesan: String) : PrintResult()
}

/**
 * Manajer untuk mencetak struk ke printer thermal via Bluetooth atau USB.
 *
 * Menggunakan Android SDK bawaan (BluetoothSocket / USB serial)
 * tanpa library eksternal — mengirim perintah ESC/POS langsung dalam byte.
 *
 * @param konteks Konteks aplikasi untuk mengakses hardware.
 */
class ThermalPrinterManager(
    private val konteks: Context,
) {

    companion object {
        // UUID SPP (Serial Port Profile) standar untuk printer Bluetooth
        private val UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val DPI = 203f
        private const val LEBAR_MM_58 = 48 // 58mm paper printable width
        private const val LEBAR_MM_80 = 72 // 80mm paper printable width
        private const val KARAKTER_PER_BARIS_58 = 32
        private const val KARAKTER_PER_BARIS_80 = 48

        /**
         * Charset CP437 — encoding standar untuk kebanyakan printer thermal.
         * Mendukung karakter Indonesia seperti é, è, ê, ë, à, â, ñ, dll.
         */
        private val CHARSET_CETAK: Charset = Charset.forName("CP437")

        private fun karakterPerBaris(lebar: LebarStruk): Int =
            when (lebar) {
                LebarStruk.Mm58 -> KARAKTER_PER_BARIS_58
                LebarStruk.Mm80 -> KARAKTER_PER_BARIS_80
            }

        private fun lebarMm(lebar: LebarStruk): Int =
            when (lebar) {
                LebarStruk.Mm58 -> LEBAR_MM_58
                LebarStruk.Mm80 -> LEBAR_MM_80
            }

        /**
         * Kata kunci untuk auto-detect printer Bluetooth.
         * Mencakup merek-merek umum: Epson, Bixolon, Star, Citizen, Xprinter,
         * Gprinter, Honeywell, Zebra, Custom, Argox, Datamax, dll.
         */
        /**
         * Kata kunci untuk auto-detect printer Bluetooth.
         * Masih dipakai di cetakStruk() untuk fallback jika user belum pilih printer.
         */
        private val KATA_KUNCI_PRINTER_BT = listOf(
            "printer", "thermal", "pos", "receipt",
            "bixolon", "epson",
            "xprinter", "gprinter", "star", "citizen",
            "honeywell", "zebra", "custom",
        )
    }

    // ESC/POS Commands
    private object EscPos {
        val INIT = byteArrayOf(0x1B, 0x40)              // Initialize printer
        val LF = byteArrayOf(0x0A)                      // Line feed
        val CUT = byteArrayOf(0x1D, 0x56, 0x00)         // Full cut
        val BOLD_ON = byteArrayOf(0x1B, 0x45, 0x01)     // Bold ON
        val BOLD_OFF = byteArrayOf(0x1B, 0x45, 0x00)    // Bold OFF
        val ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0x00)  // Left align
        val ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 0x01)// Center align
        val ALIGN_RIGHT = byteArrayOf(0x1B, 0x61, 0x02) // Right align
        val FONT_SIZE_NORMAL = byteArrayOf(0x1D, 0x21, 0x00) // Normal font size
        val FONT_SIZE_BIG = byteArrayOf(0x1D, 0x21, 0x11)    // Double width + height
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        try {
            val manager = konteks.getSystemService(Context.BLUETOOTH_SERVICE) as? android.bluetooth.BluetoothManager
            manager?.adapter
        } catch (_: SecurityException) {
            null
        }
    }

    /**
     * Mencetak struk ke printer yang terdeteksi secara otomatis.
     * Prioritas: Bluetooth > USB.
     */
    suspend fun cetakStruk(Transaction: Transaction): PrintResult = withContext(Dispatchers.IO) {
        try {
            // Coba koneksi Bluetooth dulu
            val perangkatBluetooth = cariPrinterBluetooth()
            if (perangkatBluetooth != null) {
                return@withContext cetakKeBluetooth(perangkatBluetooth, Transaction)
            }

            // Fallback ke USB
            val perangkatUsb = cariPrinterUsb()
            if (perangkatUsb != null) {
                return@withContext cetakKeUsb(perangkatUsb, Transaction)
            }

            PrintResult.Gagal("Tidak ada printer terdeteksi. Sambungkan printer Bluetooth atau USB.")
        } catch (e: SecurityException) {
            PrintResult.Gagal("Izin Bluetooth tidak diberikan. Periksa pengaturan izin.")
        } catch (e: Exception) {
            PrintResult.Gagal("Gagal mencetak: ${e.message ?: "Kesalahan tidak diketahui"}")
        }
    }

    /**
     * Mengirim test print ke printer tertentu (untuk verifikasi koneksi).
     */
    suspend fun testPrint(
        printerType: id.flexi.kasir.domain.model.PrinterType,
        printerAddress: String,
    ): PrintResult = withContext(Dispatchers.IO) {
        try {
            when (printerType) {
                id.flexi.kasir.domain.model.PrinterType.Bluetooth -> {
                    val manager = konteks.getSystemService(Context.BLUETOOTH_SERVICE) as? android.bluetooth.BluetoothManager
                    val adapter = manager?.adapter
                        ?: return@withContext PrintResult.Gagal("Bluetooth tidak tersedia.")
                    val device = if (printerAddress.isNotBlank()) {
                        adapter.getRemoteDevice(printerAddress)
                    } else {
                        cariPrinterBluetooth()
                            ?: return@withContext PrintResult.Gagal("Printer Bluetooth tidak ditemukan.")
                    }
                    var socket: BluetoothSocket? = null
                    try {
                        socket = device.createRfcommSocketToServiceRecord(UUID_SPP)
                        socket.connect()
                        val output = socket.outputStream
                        // Kirim test page
                        output.write(EscPos.INIT)
                        output.write(EscPos.ALIGN_CENTER)
                        output.write(EscPos.FONT_SIZE_BIG)
                        output.write("FLEXI KASIR".toByteArray(CHARSET_CETAK))
                        output.write(EscPos.LF)
                        output.write(EscPos.FONT_SIZE_NORMAL)
                        output.write("= Test Print =".toByteArray(CHARSET_CETAK))
                        output.write(EscPos.LF)
                        output.write(garis(karakterPerBaris(LebarStruk.Mm80)).toByteArray(CHARSET_CETAK))
                        output.write(EscPos.LF)
                        output.write(EscPos.ALIGN_LEFT)
                        output.write("Printer berhasil terkoneksi!".toByteArray(CHARSET_CETAK))
                        output.write(EscPos.LF)
                        output.write("Waktu: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("id", "ID")).format(Date())}".toByteArray(CHARSET_CETAK))
                        output.write(EscPos.LF)
                        output.write(EscPos.ALIGN_CENTER)
                        output.write(garis(karakterPerBaris(LebarStruk.Mm80)).toByteArray(CHARSET_CETAK))
                        output.write(EscPos.LF)
                        output.write("www.flexikasir.id".toByteArray(CHARSET_CETAK))
                        output.write(EscPos.LF)
                        output.write(EscPos.LF)
                        output.write(EscPos.LF)
                        output.write(EscPos.CUT)
                        output.flush()
                        PrintResult.Berhasil
                    } finally {
                        try { socket?.close() } catch (_: Exception) {}
                    }
                }
                id.flexi.kasir.domain.model.PrinterType.Usb -> {
                    val device = cariPrinterUsb()
                        ?: return@withContext PrintResult.Gagal("Printer USB tidak terdeteksi.")
                    val usbManager = konteks.getSystemService(Context.USB_SERVICE) as? UsbManager
                        ?: return@withContext PrintResult.Gagal("USB tidak tersedia.")
                    if (!usbManager.hasPermission(device)) {
                        return@withContext PrintResult.Gagal("Izin USB belum diberikan.")
                    }
                    val connection = usbManager.openDevice(device)
                        ?: return@withContext PrintResult.Gagal("Gagal buka koneksi USB.")
                    try {
                        val usbInterface = device.getInterface(0)
                        connection.claimInterface(usbInterface, true)
                        val endpointOut = (0 until usbInterface.endpointCount)
                            .map { usbInterface.getEndpoint(it) }
                            .firstOrNull { it.type == UsbConstants.USB_ENDPOINT_XFER_BULK && it.direction == UsbConstants.USB_DIR_OUT }
                            ?: return@withContext PrintResult.Gagal("Tidak ada endpoint OUT.")
                        val testBytes = ByteArrayOutputStream()
                        testBytes.write(EscPos.INIT)
                        testBytes.write(EscPos.ALIGN_CENTER)
                        testBytes.write("FLEXI KASIR - Test Print".toByteArray(CHARSET_CETAK))
                        testBytes.write(EscPos.LF)
                        testBytes.write(EscPos.LF)
                        testBytes.write(EscPos.CUT)
                        val data = testBytes.toByteArray()
                        var offset = 0
                        while (offset < data.size) {
                            connection.bulkTransfer(endpointOut, data, offset, minOf(endpointOut.maxPacketSize, data.size - offset), 5000)
                            offset += endpointOut.maxPacketSize
                        }
                        PrintResult.Berhasil
                    } finally {
                        try { connection.close() } catch (_: Exception) {}
                    }
                }
                id.flexi.kasir.domain.model.PrinterType.None -> {
                    PrintResult.Gagal("Printer tidak dikonfigurasi.")
                }
            }
        } catch (e: SecurityException) {
            PrintResult.Gagal("Izin Bluetooth tidak diberikan.")
        } catch (e: Exception) {
            PrintResult.Gagal("Gagal test print: ${e.message}")
        }
    }

    /**
     * Mencetak struk dapur (kitchen ticket) — hanya berisi daftar item,
     * nomor antrian, dan catatan. Tanpa info pembayaran/harga.
     * Digunakan agar dapur/barista mendapat cetakan fisik saat pesanan baru masuk.
     */
    suspend fun cetakStrukDapur(
        Transaction: Transaction,
        pengaturanStruk: StoreSetting? = null,
        printerType: id.flexi.kasir.domain.model.PrinterType = id.flexi.kasir.domain.model.PrinterType.Bluetooth,
        printerAddress: String = "",
    ): PrintResult = withContext(Dispatchers.IO) {
        try {
            when (printerType) {
                id.flexi.kasir.domain.model.PrinterType.Bluetooth -> {
                    if (printerAddress.isBlank()) {
                        val device = cariPrinterBluetooth()
                        if (device != null) {
                            cetakStrukDapurKeBluetooth(device, Transaction, pengaturanStruk)
                        } else {
                            PrintResult.Gagal("Printer Bluetooth tidak ditemukan.")
                        }
                    } else {
                        val manager = konteks.getSystemService(Context.BLUETOOTH_SERVICE) as? android.bluetooth.BluetoothManager
                        val adapter = manager?.adapter
                            ?: return@withContext PrintResult.Gagal("Bluetooth tidak tersedia.")
                        val device = adapter.getRemoteDevice(printerAddress)
                        cetakStrukDapurKeBluetooth(device, Transaction, pengaturanStruk)
                    }
                }
                id.flexi.kasir.domain.model.PrinterType.Usb -> {
                    val device = cariPrinterUsb()
                    if (device != null) {
                        cetakStrukDapurKeUsb(device, Transaction, pengaturanStruk)
                    } else {
                        PrintResult.Gagal("Printer USB tidak terdeteksi.")
                    }
                }
                id.flexi.kasir.domain.model.PrinterType.None -> {
                    PrintResult.Gagal("Printer tidak dikonfigurasi.")
                }
            }
        } catch (e: SecurityException) {
            PrintResult.Gagal("Izin Bluetooth tidak diberikan.")
        } catch (e: Exception) {
            PrintResult.Gagal("Gagal mencetak struk dapur: ${e.message ?: "Kesalahan tidak diketahui"}")
        }
    }

    private suspend fun cetakStrukDapurKeBluetooth(
        device: BluetoothDevice,
        Transaction: Transaction,
        pengaturanStruk: StoreSetting? = null,
    ): PrintResult = withContext(Dispatchers.IO) {
        var socket: BluetoothSocket? = null
        try {
            socket = device.createRfcommSocketToServiceRecord(UUID_SPP)
            socket.connect()
            val outputStream = socket.outputStream
            val data = buatByteStrukDapur(Transaction, pengaturanStruk)
            outputStream.write(data)
            outputStream.flush()
            PrintResult.Berhasil
        } catch (e: Exception) {
            PrintResult.Gagal("Gagal cetak struk dapur: ${e.message}")
        } finally {
            try { socket?.close() } catch (_: Exception) {}
        }
    }

    private suspend fun cetakStrukDapurKeUsb(
        device: UsbDevice,
        Transaction: Transaction,
        pengaturanStruk: StoreSetting? = null,
    ): PrintResult = withContext(Dispatchers.IO) {
        val usbManager = konteks.getSystemService(Context.USB_SERVICE) as? UsbManager
            ?: return@withContext PrintResult.Gagal("USB tidak tersedia.")
        var connection: UsbDeviceConnection? = null
        try {
            if (!usbManager.hasPermission(device)) {
                return@withContext PrintResult.Gagal("Izin USB belum diberikan.")
            }
            connection = usbManager.openDevice(device)
            if (connection == null) return@withContext PrintResult.Gagal("Gagal buka koneksi USB.")
            val usbInterface = device.getInterface(0)
            connection.claimInterface(usbInterface, true)
            val endpointOut = (0 until usbInterface.endpointCount)
                .map { usbInterface.getEndpoint(it) }
                .firstOrNull { it.type == UsbConstants.USB_ENDPOINT_XFER_BULK && it.direction == UsbConstants.USB_DIR_OUT }
                ?: return@withContext PrintResult.Gagal("Tidak ada endpoint OUT.")
            val data = buatByteStrukDapur(Transaction, pengaturanStruk)
            val chunkSize = endpointOut.maxPacketSize
            var offset = 0
            while (offset < data.size) {
                connection.bulkTransfer(endpointOut, data, offset, minOf(chunkSize, data.size - offset), 5000)
                offset += chunkSize
            }
            PrintResult.Berhasil
        } catch (e: Exception) {
            PrintResult.Gagal("Gagal cetak struk dapur via USB: ${e.message}")
        } finally {
            try { connection?.close() } catch (_: Exception) {}
        }
    }

    private fun buatByteStrukDapur(
        Transaction: Transaction,
        pengaturanStruk: StoreSetting? = null,
    ): ByteArray {
        val settings = pengaturanStruk ?: StoreSetting()
        val lebar = karakterPerBaris(settings.lebarStruk)
        val formatTanggal = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))
        val waktuCetak = formatTanggal.format(Date(Transaction.waktuTransactionEpochMili))

        val output = ByteArrayOutputStream()

        // Initialize
        output.write(EscPos.INIT)

        // ── Header: TANDA DAPUR ──
        output.write(EscPos.ALIGN_CENTER)
        output.write(EscPos.FONT_SIZE_BIG)
        output.write("*** DAPUR ***".toByteArray(CHARSET_CETAK))
        output.write(EscPos.LF)
        output.write(EscPos.FONT_SIZE_NORMAL)

        val namaHeader = settings.namaUsaha.ifBlank { "FLEXI KASIR" }
        output.write(namaHeader.toByteArray(CHARSET_CETAK))
        output.write(EscPos.LF)
        output.write(garis(lebar).toByteArray(CHARSET_CETAK))
        output.write(EscPos.LF)

        // ── Info Pesanan ──
        output.write(EscPos.FONT_SIZE_BIG)
        if (Transaction.nomorAntrian != null) {
            output.write("ANTRIAN #${Transaction.nomorAntrian}".toByteArray(CHARSET_CETAK))
            output.write(EscPos.LF)
        }
        output.write(EscPos.FONT_SIZE_NORMAL)
        output.write(Transaction.id.take(8).uppercase().toByteArray(CHARSET_CETAK))
        output.write(EscPos.LF)
        output.write(waktuCetak.toByteArray(CHARSET_CETAK))
        output.write(EscPos.LF)
        output.write(Transaction.orderType.label.toByteArray(CHARSET_CETAK))
        output.write(EscPos.LF)

        // Info meja (jika Dine In)
        if (Transaction.orderType == id.flexi.kasir.domain.model.OrderType.DineIn && Transaction.mejaId != null) {
            output.write(EscPos.BOLD_ON)
            output.write("Meja: ${Transaction.mejaId}".toByteArray(CHARSET_CETAK))
            output.write(EscPos.BOLD_OFF)
            output.write(EscPos.LF)
        }

        output.write(garis(lebar).toByteArray(CHARSET_CETAK))
        output.write(EscPos.LF)

        // ── Daftar Item (tanpa harga) ──
        output.write(EscPos.ALIGN_LEFT)
        Transaction.daftarCartItem.forEach { item ->
            val nama = item.varian?.let { "${item.produk.nama} (${it.nama})" }
                ?: item.produk.nama
            output.write(EscPos.BOLD_ON)
            output.write("${item.jumlah}x".toByteArray(CHARSET_CETAK))
            output.write(EscPos.BOLD_OFF)
            output.write(" ${nama.take(lebar - 5)}".toByteArray(CHARSET_CETAK))
            output.write(EscPos.LF)
            // Catatan item
            if (!item.catatan.isNullOrBlank()) {
                output.write("   > ${item.catatan}".take(lebar).toByteArray(CHARSET_CETAK))
                output.write(EscPos.LF)
            }
        }

        output.write(garis(lebar).toByteArray(CHARSET_CETAK))
        output.write(EscPos.LF)

        // ── Catatan Pesanan ──
        if (!Transaction.catatan.isNullOrBlank()) {
            output.write(EscPos.ALIGN_CENTER)
            output.write(EscPos.BOLD_ON)
            output.write("CATATAN:".toByteArray(CHARSET_CETAK))
            output.write(EscPos.BOLD_OFF)
            output.write(EscPos.LF)
            output.write(Transaction.catatan.toByteArray(CHARSET_CETAK))
            output.write(EscPos.LF)
            output.write(garis(lebar).toByteArray(CHARSET_CETAK))
            output.write(EscPos.LF)
        }

        // ── Footer ──
        output.write(EscPos.ALIGN_CENTER)
        output.write(EscPos.FONT_SIZE_BIG)
        output.write("Siap Diantar".toByteArray(CHARSET_CETAK))
        output.write(EscPos.LF)
        output.write(EscPos.FONT_SIZE_NORMAL)

        // Feed + Cut
        output.write(EscPos.LF)
        output.write(EscPos.LF)
        output.write(EscPos.LF)
        output.write(EscPos.CUT)

        return output.toByteArray()
    }

    /**
     * Mencetak struk berdasarkan konfigurasi printer yang dipilih user.
     *
     * @param printerType Tipe printer (None, Bluetooth, Usb).
     * @param printerAddress Alamat MAC untuk Bluetooth, atau "usb_auto" untuk USB.
     * @param pengaturanStruk Pengaturan struk (header, footer, lebar, copy, dll).
     */
    suspend fun cetakStrukDenganKonfigurasi(
        Transaction: Transaction,
        printerType: id.flexi.kasir.domain.model.PrinterType,
        printerAddress: String,
        pengaturanStruk: StoreSetting? = null,
    ): PrintResult = withContext(Dispatchers.IO) {
        try {
            when (printerType) {
                id.flexi.kasir.domain.model.PrinterType.Bluetooth -> {
                    if (printerAddress.isBlank()) {
                        val device = cariPrinterBluetooth()
                        if (device != null) {
                            cetakKeBluetooth(device, Transaction, pengaturanStruk)
                        } else {
                            PrintResult.Gagal("Printer Bluetooth tidak ditemukan. Pastikan sudah dipasangkan.")
                        }
                    } else {
                        cetakKeAlamatBluetooth(printerAddress, Transaction, pengaturanStruk)
                    }
                }
                id.flexi.kasir.domain.model.PrinterType.Usb -> {
                    val device = cariPrinterUsb()
                    if (device != null) {
                        cetakKeUsb(device, Transaction, pengaturanStruk)
                    } else {
                        PrintResult.Gagal("Printer USB tidak terdeteksi. Periksa sambungan.")
                    }
                }
                id.flexi.kasir.domain.model.PrinterType.None -> {
                    PrintResult.Gagal("Printer tidak dikonfigurasi.")
                }
            }
        } catch (e: SecurityException) {
            PrintResult.Gagal("Izin Bluetooth tidak diberikan.")
        } catch (e: Exception) {
            PrintResult.Gagal("Gagal mencetak: ${e.message ?: "Kesalahan tidak diketahui"}")
        }
    }

    /**
     * Mencetak struk ke alamat MAC Bluetooth tertentu.
     */
    suspend fun cetakKeAlamatBluetooth(
        alamatMac: String,
        Transaction: Transaction,
        pengaturanStruk: StoreSetting? = null,
    ): PrintResult = withContext(Dispatchers.IO) {
            try {
                val manager = konteks.getSystemService(Context.BLUETOOTH_SERVICE) as? android.bluetooth.BluetoothManager
                val adapter = manager?.adapter
                    ?: return@withContext PrintResult.Gagal("Bluetooth tidak tersedia.")
                val device = adapter.getRemoteDevice(alamatMac)
                cetakKeBluetooth(device, Transaction, pengaturanStruk)
            } catch (e: SecurityException) {
                PrintResult.Gagal("Izin Bluetooth tidak diberikan.")
            } catch (e: Exception) {
                PrintResult.Gagal("Gagal mencetak: ${e.message}")
            }
        }

    // ─── Bluetooth ──────────────────────────────────────────────

    /**
     * Mencari printer Bluetooth pertama yang sudah dipasangkan.
     * Izin BLUETOOTH_CONNECT diperiksa di pemanggil (percetakan membungkus
     * SecurityException); di sini dianotasikan agar lint tidak menandai
     * bondedDevices sebagai MissingPermission.
     */
    @SuppressLint("MissingPermission")
    private fun cariPrinterBluetooth(): BluetoothDevice? {
        val adapter = bluetoothAdapter ?: return null
        if (!adapter.isEnabled) return null

        val pairedDevices = adapter.bondedDevices
        return pairedDevices?.firstOrNull { device ->
            // Filter: biasanya printer thermal punya nama mengandung kata kunci
            val nama = device.name?.lowercase() ?: ""
            KATA_KUNCI_PRINTER_BT.any { kata -> nama.contains(kata) }
        }
    }

    /**
     * Mencetak ke perangkat Bluetooth.
     */
    private suspend fun cetakKeBluetooth(
        device: BluetoothDevice,
        Transaction: Transaction,
        pengaturanStruk: StoreSetting? = null,
    ): PrintResult = withContext(Dispatchers.IO) {
        // Coba beberapa metode koneksi + retry untuk kompatibilitas maksimal
        val metode = listOf(
            "Insecure" to { device.createInsecureRfcommSocketToServiceRecord(UUID_SPP) },
            "RFCOMM(UUID)" to { device.createRfcommSocketToServiceRecord(UUID_SPP) },
        )

        var lastError: String = "Tidak ada metode koneksi yang berhasil"

        for ((namaMetode, buatSocket) in metode) {
            for (percobaan in 1..2) {
                var socket: BluetoothSocket? = null
                try {
                    socket = buatSocket()
                    socket.connect()
                    Thread.sleep(300)
                    val outputStream = socket.outputStream
                    outputStream.write(EscPos.INIT)
                    outputStream.flush()
                    Thread.sleep(100)
                    tulisStruk(outputStream, Transaction, pengaturanStruk)
                    return@withContext PrintResult.Berhasil
                } catch (e: Exception) {
                    lastError = "$namaMetode(#$percobaan): ${e.message}"
                    try { socket?.close() } catch (_: Exception) {}
                    Thread.sleep(200)
                }
            }
        }

        PrintResult.Gagal("Gagal mencetak: $lastError")
    }

    // ─── USB ────────────────────────────────────────────────────

    /**
     * Mencari printer USB yang terhubung.
     */
    private fun cariPrinterUsb(): UsbDevice? {
        val usbManager = konteks.getSystemService(Context.USB_SERVICE) as? UsbManager ?: return null
        val deviceList = usbManager.deviceList ?: return null

        return deviceList.values.firstOrNull { device ->
            // Printer thermal biasanya class = 7 (PRINTER) atau interface class-nya 7
            device.interfaceCount > 0 && (device.deviceClass == 7 ||
                device.getInterface(0).interfaceClass == 7 ||
                device.getInterface(0).interfaceClass == UsbConstants.USB_CLASS_CDC_DATA)
        }
    }

    /**
     * Mencetak ke perangkat USB.
     */
    private suspend fun cetakKeUsb(
        device: UsbDevice,
        Transaction: Transaction,
        pengaturanStruk: StoreSetting? = null,
    ): PrintResult = withContext(Dispatchers.IO) {
        val usbManager = konteks.getSystemService(Context.USB_SERVICE) as? UsbManager
            ?: return@withContext PrintResult.Gagal("USB tidak tersedia.")

        var connection: UsbDeviceConnection? = null
        try {
            if (!usbManager.hasPermission(device)) {
                return@withContext PrintResult.Gagal("Izin USB belum diberikan.")
            }

            connection = usbManager.openDevice(device)
            if (connection == null) {
                return@withContext PrintResult.Gagal("Gagal membuka koneksi USB.")
            }

            val usbInterface = device.getInterface(0)
            connection.claimInterface(usbInterface, true)

            val endpointOut = (0 until usbInterface.endpointCount)
                .map { usbInterface.getEndpoint(it) }
                .firstOrNull { it.type == UsbConstants.USB_ENDPOINT_XFER_BULK && it.direction == UsbConstants.USB_DIR_OUT }

            if (endpointOut == null) {
                return@withContext PrintResult.Gagal("Tidak ditemukan endpoint OUT pada printer USB.")
            }

            val dataStruk = buatByteStrukDenganPengaturan(Transaction, pengaturanStruk)
            val chunkSize = endpointOut.maxPacketSize
            var offset = 0

            while (offset < dataStruk.size) {
                val panjang = minOf(chunkSize, dataStruk.size - offset)
                connection.bulkTransfer(endpointOut, dataStruk, offset, panjang, 5000)
                offset += panjang
            }

            PrintResult.Berhasil
        } catch (e: Exception) {
            PrintResult.Gagal("Gagal mencetak via USB: ${e.message}")
        } finally {
            try {
                connection?.close()
            } catch (_: Exception) {}
        }
    }

    // ─── Format struk ────────────────────────────────────────────

    /**
     * Menulis data struk ke OutputStream (Bluetooth), dengan multiple copy jika diatur.
     */
    private fun tulisStruk(stream: OutputStream, Transaction: Transaction, pengaturanStruk: StoreSetting? = null) {
        val data = buatByteStrukDenganPengaturan(Transaction, pengaturanStruk)
        kirimBertahap(stream, data)
    }

    /**
     * Mengirim data ke printer secara bertahap (chunk) untuk mencegah
     * socket timeout pada struk yang panjang. Printer thermal butuh waktu
     * memproses data sebelum menerima berikutnya.
     */
    private fun kirimBertahap(stream: OutputStream, data: ByteArray, chunkSize: Int = 256) {
        var offset = 0
        while (offset < data.size) {
            val panjang = minOf(chunkSize, data.size - offset)
            stream.write(data, offset, panjang)
            stream.flush()
            offset += panjang
            if (offset < data.size) {
                Thread.sleep(50)
            }
        }
    }

    /**
     * Mencetak laporan riwayat penjualan ke printer thermal.
     * Berisi ringkasan per periode, breakdown per tanggal, dan daftar transaksi.
     */
    suspend fun cetakLaporanRiwayat(
        daftarTransaksi: List<Transaction>,
        labelPeriode: String,
        pengaturanStruk: StoreSetting? = null,
        printerType: id.flexi.kasir.domain.model.PrinterType = id.flexi.kasir.domain.model.PrinterType.Bluetooth,
        printerAddress: String = "",
    ): PrintResult = withContext(Dispatchers.IO) {
        val settings = pengaturanStruk ?: StoreSetting()
        try {
            when (printerType) {
                id.flexi.kasir.domain.model.PrinterType.Bluetooth -> {
                    if (printerAddress.isBlank()) {
                        val device = cariPrinterBluetooth()
                        if (device != null) {
                            cetakLaporanKeBluetooth(device, daftarTransaksi, labelPeriode, settings)
                        } else {
                            PrintResult.Gagal("Printer Bluetooth tidak ditemukan.")
                        }
                    } else {
                        val manager = konteks.getSystemService(Context.BLUETOOTH_SERVICE) as? android.bluetooth.BluetoothManager
                        val adapter = manager?.adapter
                            ?: return@withContext PrintResult.Gagal("Bluetooth tidak tersedia.")
                        val device = adapter.getRemoteDevice(printerAddress)
                        cetakLaporanKeBluetooth(device, daftarTransaksi, labelPeriode, settings)
                    }
                }
                id.flexi.kasir.domain.model.PrinterType.Usb -> {
                    val device = cariPrinterUsb()
                    if (device != null) {
                        cetakLaporanKeUsb(device, daftarTransaksi, labelPeriode, settings)
                    } else {
                        PrintResult.Gagal("Printer USB tidak terdeteksi.")
                    }
                }
                id.flexi.kasir.domain.model.PrinterType.None -> {
                    PrintResult.Gagal("Printer tidak dikonfigurasi.")
                }
            }
        } catch (e: SecurityException) {
            PrintResult.Gagal("Izin Bluetooth tidak diberikan.")
        } catch (e: Exception) {
            PrintResult.Gagal("Gagal mencetak: ${e.message ?: "Kesalahan tidak diketahui"}")
        }
    }

    private suspend fun cetakLaporanKeBluetooth(
        device: BluetoothDevice,
        daftarTransaksi: List<Transaction>,
        labelPeriode: String,
        settings: StoreSetting = StoreSetting(),
    ): PrintResult = withContext(Dispatchers.IO) {
        var socket: BluetoothSocket? = null
        try {
            socket = device.createRfcommSocketToServiceRecord(UUID_SPP)
            socket.connect()
            val outputStream = socket.outputStream
            val data = buatByteLaporanRiwayat(daftarTransaksi, labelPeriode, settings.namaUsaha, settings.tagline)
            kirimBertahap(outputStream, data)
            PrintResult.Berhasil
        } catch (e: Exception) {
            PrintResult.Gagal("Gagal cetak laporan: ${e.message}")
        } finally {
            try { socket?.close() } catch (_: Exception) {}
        }
    }

    private suspend fun cetakLaporanKeUsb(
        device: UsbDevice,
        daftarTransaksi: List<Transaction>,
        labelPeriode: String,
        settings: StoreSetting = StoreSetting(),
    ): PrintResult = withContext(Dispatchers.IO) {
        val usbManager = konteks.getSystemService(Context.USB_SERVICE) as? UsbManager
            ?: return@withContext PrintResult.Gagal("USB tidak tersedia.")
        var connection: UsbDeviceConnection? = null
        try {
            if (!usbManager.hasPermission(device)) {
                return@withContext PrintResult.Gagal("Izin USB belum diberikan.")
            }
            connection = usbManager.openDevice(device)
            if (connection == null) return@withContext PrintResult.Gagal("Gagal buka koneksi USB.")
            val usbInterface = device.getInterface(0)
            connection.claimInterface(usbInterface, true)
            val endpointOut = (0 until usbInterface.endpointCount)
                .map { usbInterface.getEndpoint(it) }
                .firstOrNull { it.type == UsbConstants.USB_ENDPOINT_XFER_BULK && it.direction == UsbConstants.USB_DIR_OUT }
                ?: return@withContext PrintResult.Gagal("Tidak ada endpoint OUT.")
            val data = buatByteLaporanRiwayat(daftarTransaksi, labelPeriode, settings.namaUsaha, settings.tagline)
            val chunkSize = endpointOut.maxPacketSize
            var offset = 0
            while (offset < data.size) {
                connection.bulkTransfer(endpointOut, data, offset, minOf(chunkSize, data.size - offset), 5000)
                offset += chunkSize
            }
            PrintResult.Berhasil
        } catch (e: Exception) {
            PrintResult.Gagal("Gagal cetak laporan via USB: ${e.message}")
        } finally {
            try { connection?.close() } catch (_: Exception) {}
        }
    }

    private fun buatByteLaporanRiwayat(
        daftarTransaksi: List<Transaction>,
        labelPeriode: String,
        namaUsaha: String = "",
        tagline: String = "",
    ): ByteArray {
        val transaksiAktif = daftarTransaksi.filter { !it.dibatalkan }
        val output = ByteArrayOutputStream()

        output.write(EscPos.INIT)

        // ── Header ──
        output.write(EscPos.ALIGN_CENTER)
        val namaUsahaHeader = namaUsaha.ifBlank { "FLEXI KASIR" }
        output.write(EscPos.FONT_SIZE_BIG)
        output.write(namaUsahaHeader.toByteArray(CHARSET_CETAK))
        output.write(EscPos.LF)
        if (tagline.isNotBlank()) {
            output.write(EscPos.FONT_SIZE_NORMAL)
            output.write(tagline.take(karakterPerBaris(LebarStruk.Mm58)).toByteArray(CHARSET_CETAK))
            output.write(EscPos.LF)
        }
        output.write(EscPos.FONT_SIZE_NORMAL)
        output.write("LAPORAN PENJUALAN".toByteArray(CHARSET_CETAK))
        output.write(EscPos.LF)
        output.write(garis().toByteArray(CHARSET_CETAK))
        output.write(EscPos.LF)
        output.write(labelPeriode.toByteArray(CHARSET_CETAK))
        output.write(EscPos.LF)
        output.write(EscPos.LF)

        // ── Ringkasan ──
        val totalPenjualan = transaksiAktif.sumOf { it.hitungTotalAkhirTransaction() }
        val totalTunai = transaksiAktif.filter { it.paymentMethod == id.flexi.kasir.domain.model.PaymentMethod.Cash }
            .sumOf { it.hitungTotalAkhirTransaction() }
        val totalQris = transaksiAktif.filter { it.paymentMethod == id.flexi.kasir.domain.model.PaymentMethod.Qris }
            .sumOf { it.hitungTotalAkhirTransaction() }
        val jumlahItem = transaksiAktif.sumOf { t -> t.daftarCartItem.sumOf { it.jumlah } }

        output.write(EscPos.ALIGN_LEFT)
        output.write(EscPos.BOLD_ON)
        output.write(tulisDuaKolom("Total Transaksi", "${transaksiAktif.size}"))
        output.write(EscPos.LF)
        output.write(tulisDuaKolom("Total Item", "$jumlahItem"))
        output.write(EscPos.LF)
        output.write(EscPos.BOLD_OFF)
        output.write(garisTitik().toByteArray(CHARSET_CETAK))
        output.write(EscPos.LF)
        output.write(tulisDuaKolom("Tunai", totalTunai.sebagaiRupiah()))
        output.write(EscPos.LF)
        output.write(tulisDuaKolom("QRIS", totalQris.sebagaiRupiah()))
        output.write(EscPos.LF)
        output.write(EscPos.BOLD_ON)
        output.write(tulisDuaKolom("TOTAL", totalPenjualan.sebagaiRupiah()))
        output.write(EscPos.BOLD_OFF)
        output.write(EscPos.LF)
        output.write(EscPos.LF)

        // ── Per Tanggal ──
        val fmtTgl = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val fmtJam = SimpleDateFormat("HH:mm", Locale("id", "ID"))
        val perTanggal = transaksiAktif
            .sortedByDescending { it.waktuTransactionEpochMili }
            .groupBy { fmtTgl.format(Date(it.waktuTransactionEpochMili)) }

        perTanggal.entries.forEach { (tanggal, daftar) ->
            val subTotal = daftar.sumOf { it.hitungTotalAkhirTransaction() }
            output.write(EscPos.BOLD_ON)
            output.write(tulisDuaKolom(tanggal, "${daftar.size} trx"))
            output.write(EscPos.BOLD_OFF)
            output.write(EscPos.LF)
            output.write(garisTitik().toByteArray(CHARSET_CETAK))
            output.write(EscPos.LF)

            daftar.forEach { t ->
                val waktu = fmtJam.format(Date(t.waktuTransactionEpochMili))
                val idPendek = t.id.take(8).uppercase()
                val metode = if (t.paymentMethod == id.flexi.kasir.domain.model.PaymentMethod.Cash) "TN" else "QR"
                val totalTrans = t.hitungTotalAkhirTransaction().sebagaiRupiah()
                output.write("$waktu $idPendek [$metode]".toByteArray(CHARSET_CETAK))
                output.write(EscPos.LF)
                output.write(EscPos.ALIGN_RIGHT)
                output.write(totalTrans.toByteArray(CHARSET_CETAK))
                output.write(EscPos.LF)
                output.write(EscPos.ALIGN_LEFT)
            }

            output.write(EscPos.BOLD_ON)
            output.write(tulisDuaKolom("Subtotal", subTotal.sebagaiRupiah()))
            output.write(EscPos.BOLD_OFF)
            output.write(EscPos.LF)
            output.write(EscPos.LF)
        }

        // ── Footer ──
        output.write(EscPos.ALIGN_CENTER)
        output.write(garis().toByteArray(CHARSET_CETAK))
        output.write(EscPos.LF)
        output.write(EscPos.FONT_SIZE_BIG)
        output.write("Terima Kasih".toByteArray(CHARSET_CETAK))
        output.write(EscPos.LF)
        output.write(EscPos.FONT_SIZE_NORMAL)
        output.write("www.flexikasir.id".toByteArray(CHARSET_CETAK))

        output.write(EscPos.LF)
        output.write(EscPos.LF)
        output.write(EscPos.LF)
        output.write(EscPos.CUT)

        return output.toByteArray()
    }

    /**
     * Membuat array byte ESC/POS untuk struk belanja dengan pengaturan kustom.
     */
    private fun buatByteStrukDenganPengaturan(Transaction: Transaction, pengaturanStruk: StoreSetting? = null): ByteArray {
        val settings = pengaturanStruk ?: StoreSetting()
        val lebar = karakterPerBaris(settings.lebarStruk)
        val jumlahCopy = settings.jumlahCopyCetak.coerceIn(1, 5)

        val formatTanggal = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))
        val waktuCetak = formatTanggal.format(Date(Transaction.waktuTransactionEpochMili))
        // Subtotal memakai harga efektif per item (varian diutamakan, lalu harga
        // produk dasar) — konsisten dengan hitungSubtotalKeranjangUang() agar
        // Subtotal dan Total di nota cocok dengan nominal yang dibayar.
        val subtotal = Transaction.daftarCartItem
            .hitungSubtotalKeranjangUang()
            .nilaiRupiah

        val singleCopy = buildSingleByteStruk(Transaction, waktuCetak, subtotal, settings, lebar)

        // Jika hanya 1 copy, return langsung
        if (jumlahCopy <= 1) return singleCopy

        // Multiple copies: gabungkan dengan potongan kertas di antaranya
        val output = ByteArrayOutputStream()
        for (i in 1..jumlahCopy) {
            if (i > 1) {
                // Feed kertas sedikit agar ada jarak antar copy
                output.write(EscPos.LF)
                output.write(EscPos.LF)
            }
            output.write(singleCopy)
        }
        return output.toByteArray()
    }

    private fun buildSingleByteStruk(
        Transaction: Transaction,
        waktuCetak: String,
        subtotal: Long,
        settings: StoreSetting,
        lebar: Int,
    ): ByteArray {
        val output = ByteArrayOutputStream()

        // Initialize
        output.write(EscPos.INIT)

        // ── Header ──
        output.write(EscPos.ALIGN_CENTER)

        // Logo (jika aktif & ada URI)
        if (settings.tampilkanLogoDiStruk && settings.logoUri.isNotBlank()) {
            val lebarDots = lebar * 8
            val logoRaster = muatLogoDanUbahKeRaster(settings.logoUri, lebarDots)
            if (logoRaster != null) {
                output.write(EscPos.ALIGN_CENTER)
                output.write(logoRaster)
                output.write(EscPos.LF)
            }
        }

        val namaHeader = settings.namaUsaha.ifBlank { "FLEXI KASIR" }
        output.write(EscPos.FONT_SIZE_BIG)
        output.write(namaHeader.toByteArray(CHARSET_CETAK))
        output.write(EscPos.LF)

        // Tagline (jika ada)
        if (settings.tagline.isNotBlank()) {
            output.write(EscPos.FONT_SIZE_NORMAL)
            output.write(settings.tagline.take(lebar).toByteArray(CHARSET_CETAK))
            output.write(EscPos.LF)
        }

        // Header custom
        if (settings.strukHeader.isNotBlank()) {
            output.write(EscPos.FONT_SIZE_NORMAL)
            settings.strukHeader.lines().forEach { baris ->
                output.write(baris.take(lebar).toByteArray(CHARSET_CETAK))
                output.write(EscPos.LF)
            }
        }

        output.write(garisTitik(lebar).toByteArray(CHARSET_CETAK))
        output.write(EscPos.LF)

        // Info transaksi
        output.write(EscPos.ALIGN_CENTER)
        output.write(Transaction.id.take(8).uppercase().toByteArray(CHARSET_CETAK))
        output.write(EscPos.LF)
        output.write(waktuCetak.toByteArray(CHARSET_CETAK))
        output.write(EscPos.LF)
        output.write(Transaction.orderType.name.toByteArray(CHARSET_CETAK))
        output.write(EscPos.LF)
        Transaction.nomorAntrian?.let {
            output.write(EscPos.BOLD_ON)
            output.write("No. Antrian: $it".toByteArray(CHARSET_CETAK))
            output.write(EscPos.BOLD_OFF)
            output.write(EscPos.LF)
        }

        // Daftar item
        output.write(EscPos.ALIGN_LEFT)
        val maxNamaPanjang = (lebar * 0.6).toInt().coerceIn(15, 30)
        Transaction.daftarCartItem.forEach { item ->
            val hargaSatuan = item.varian?.harga ?: item.produk.harga
            // Sertakan nama varian agar dua varian produk yang sama tidak tampil
            // sebagai baris identik dengan harga berbeda.
            val nama = item.varian?.let { "${item.produk.nama} (${it.nama})" }
                ?: item.produk.nama
            val namaCetak = nama.take(maxNamaPanjang)
            val hargaStr = hargaSatuan.sebagaiRupiah()
            val subtotalItem = (hargaSatuan * item.jumlah).sebagaiRupiah()

            output.write(namaCetak.toByteArray(CHARSET_CETAK))
            output.write(EscPos.LF)
            output.write(EscPos.BOLD_ON)
            val barisItem = "${item.jumlah} x $hargaStr"
            val padding = " ".repeat(maxOf(0, lebar - barisItem.length - subtotalItem.length - 2))
            output.write("$barisItem$padding$subtotalItem".toByteArray(CHARSET_CETAK))
            output.write(EscPos.BOLD_OFF)
            output.write(EscPos.LF)
        }

        output.write(garisTitik(lebar).toByteArray(CHARSET_CETAK))
        output.write(EscPos.LF)

        // Rincian biaya
        output.write(EscPos.ALIGN_LEFT)
        output.write(tulisDuaKolom("Subtotal", subtotal.sebagaiRupiah(), lebar))
        output.write(EscPos.LF)
        if (Transaction.potongan.nilaiRupiah > 0) {
            output.write(tulisDuaKolom("Potongan", "-${Transaction.potongan.sebagaiRupiah()}", lebar))
            output.write(EscPos.LF)
        }
        if (settings.tampilkanPajakDiStruk) {
            if (Transaction.biayaLayanan.nilaiRupiah > 0) {
                output.write(tulisDuaKolom("Biaya Layanan", Transaction.biayaLayanan.sebagaiRupiah(), lebar))
                output.write(EscPos.LF)
            }
            if (Transaction.pajak.nilaiRupiah > 0) {
                output.write(tulisDuaKolom("Pajak", Transaction.pajak.sebagaiRupiah(), lebar))
                output.write(EscPos.LF)
            }
        }

        output.write(garisTitik(lebar).toByteArray(CHARSET_CETAK))
        output.write(EscPos.LF)

        // Total akhir persis sama dengan nilai transaksi tersimpan (termasuk
        // clamp potongan di TransactionCostBreakdown).
        val totalVal = Transaction.hitungTotalAkhirTransaction()
        output.write(EscPos.BOLD_ON)
        output.write(tulisDuaKolom("Total", totalVal.sebagaiRupiah(), lebar))
        output.write(EscPos.BOLD_OFF)
        output.write(EscPos.LF)

        // Pembayaran: metode (Tunai/QRIS), nominal dibayar, dan kembalian.
        val nominalDibayar = Transaction.uangDibayar.nilaiRupiah
        if (nominalDibayar > 0) {
            output.write(tulisDuaKolom(Transaction.paymentMethod.label, nominalDibayar.sebagaiRupiah(), lebar))
            output.write(EscPos.LF)
            val kembalian = hitungKembalian(nominalDibayar, totalVal)
            if (kembalian > 0) {
                output.write(tulisDuaKolom("Kembalian", kembalian.sebagaiRupiah(), lebar))
                output.write(EscPos.LF)
            }
        }

        // Catatan
        if (!Transaction.catatan.isNullOrBlank()) {
            output.write(garisTitik(lebar).toByteArray(CHARSET_CETAK))
            output.write(EscPos.LF)
            output.write(EscPos.ALIGN_CENTER)
            output.write(EscPos.BOLD_ON)
            output.write("Catatan:".toByteArray(CHARSET_CETAK))
            output.write(EscPos.BOLD_OFF)
            output.write(EscPos.LF)
            output.write(Transaction.catatan.toByteArray(CHARSET_CETAK))
            output.write(EscPos.LF)
        }

        // Footer custom
        if (settings.strukFooter.isNotBlank()) {
            output.write(garisTitik(lebar).toByteArray(CHARSET_CETAK))
            output.write(EscPos.LF)
            output.write(EscPos.ALIGN_CENTER)
            settings.strukFooter.lines().forEach { baris ->
                output.write(baris.take(lebar).toByteArray(CHARSET_CETAK))
                output.write(EscPos.LF)
            }
        }

        // Footer standar
        output.write(EscPos.ALIGN_CENTER)
        output.write(garisTitik(lebar).toByteArray(CHARSET_CETAK))
        output.write(EscPos.LF)
        output.write("Terima Kasih".toByteArray(CHARSET_CETAK))
        output.write(EscPos.LF)
        output.write("www.flexikasir.id".toByteArray(CHARSET_CETAK))

        // Feed + Cut
        output.write(EscPos.LF)
        output.write(EscPos.CUT)

        return output.toByteArray()
    }

    /**
     * Membuat baris teks dengan dua kolom (kiri dan kanan).
     */
    private fun tulisDuaKolom(kiri: String, kanan: String, lebar: Int = 32): ByteArray {
        val maxPanjang = lebar
        val bersihKiri = kiri.take(maxPanjang - kanan.length - 2)
        val padding = " ".repeat(maxOf(0, maxPanjang - bersihKiri.length - kanan.length))
        return "$bersihKiri$padding$kanan".toByteArray(CHARSET_CETAK)
    }

    private fun garis(lebar: Int = 32): String = "=".repeat(lebar)

    private fun garisTitik(lebar: Int = 32): String = "-".repeat(lebar)

    /**
     * Memuat logo dari URI dan mengubah ke format raster ESC/POS.
     * Mengembalikan byte array perintah GS v 0, atau null jika gagal.
     */
    private fun muatLogoDanUbahKeRaster(logoUri: String, lebarDots: Int): ByteArray? {
        try {
            val maybeBitmap: Bitmap? = when {
                logoUri.startsWith("/") || logoUri.contains("files/") || logoUri.contains("cache/") -> {
                    BitmapFactory.decodeFile(logoUri)
                }
                logoUri.startsWith("content://") -> {
                    val uri = Uri.parse(logoUri)
                    konteks.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                }
                logoUri.startsWith("file://") -> {
                    BitmapFactory.decodeFile(Uri.parse(logoUri).path)
                }
                else -> null
            }
            val bitmap = maybeBitmap ?: return null

            // Resize ke lebar printer, pertahankan aspect ratio
            val tinggiAsli = bitmap.height.toFloat()
            val lebarAsli = bitmap.width.toFloat()
            val tinggiBaru = (tinggiAsli * lebarDots / lebarAsli).toInt()
                .coerceIn(1, 400)
            val scaled = Bitmap.createScaledBitmap(bitmap, lebarDots, tinggiBaru, true)
            if (scaled != bitmap) bitmap.recycle()

            // Convert ke monochrome
            val monokrom = Bitmap.createBitmap(lebarDots, tinggiBaru, Bitmap.Config.ALPHA_8)
            val canvas = android.graphics.Canvas(monokrom)
            canvas.drawColor(android.graphics.Color.WHITE)
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                isAntiAlias = false
            }
            canvas.drawBitmap(scaled, 0f, 0f, paint)
            if (scaled != monokrom) scaled.recycle()

            // Encode ke ESC/POS GS v 0
            val bytesPerLine = (lebarDots + 7) / 8
            val data = ByteArrayOutputStream()
            data.write(0x1D) // GS
            data.write(0x76) // v
            data.write(0x30) // 0 = normal
            data.write(bytesPerLine and 0xFF)
            data.write((bytesPerLine shr 8) and 0xFF)
            data.write(tinggiBaru and 0xFF)
            data.write((tinggiBaru shr 8) and 0xFF)

            for (y in 0 until tinggiBaru) {
                for (xByte in 0 until bytesPerLine) {
                    var byte = 0
                    for (bit in 0 until 8) {
                        val x = xByte * 8 + bit
                        if (x < lebarDots) {
                            val pixel = monokrom.getPixel(x, y)
                            if (android.graphics.Color.red(pixel) < 128) {
                                byte = byte or (0x80 shr bit)
                            }
                        }
                    }
                    data.write(byte)
                }
            }
            monokrom.recycle()
            return data.toByteArray()
        } catch (_: Exception) {
            return null
        }
    }

    // ─── Bluetooth Discovery ──────────────────────────────────

    /**
     * Hasil scan printer Bluetooth di sekitar.
     */
    data class HasilScanPrinter(
        val alamatMac: String,
        val nama: String,
        val sudahDipasangkan: Boolean,
    )

    /**
     * Callback untuk hasil scan printer Bluetooth.
     */
    fun interface ScanPrinterCallback {
        fun onHasilScan(daftarPrinter: List<HasilScanPrinter>)
    }

    private var receiverScanPrinter: android.content.BroadcastReceiver? = null

    /**
     * Memulai scanning printer Bluetooth di sekitar (termasuk yang belum dipasangkan).
     * Hasil dikembalikan via callback setelah discovery selesai (~12 detik).
     *
     * CATATAN: Untuk Android 12+ (API 31), BLUETOOTH_SCAN harus sudah di-grant.
     */
    @SuppressLint("MissingPermission")
    fun mulaiScanPrinter(callback: ScanPrinterCallback) {
        val adapter = bluetoothAdapter ?: run {
            callback.onHasilScan(emptyList())
            return
        }
        if (!adapter.isEnabled) {
            callback.onHasilScan(emptyList())
            return
        }

        // Hentikan scan sebelumnya jika masih jalan
        hentikanScanPrinter()

        val daftarPrinter = mutableListOf<HasilScanPrinter>()
        val namaPrinterDikenal = mutableSetOf<String>() // hindari duplikat

        // Tambah paired devices dulu (langsung)
        adapter.bondedDevices?.forEach { device ->
            val nama = device.name?.lowercase() ?: ""
            if (KATA_KUNCI_PRINTER_BT.any { kata -> nama.contains(kata) }) {
                val key = device.address
                if (namaPrinterDikenal.add(key)) {
                    daftarPrinter.add(
                        HasilScanPrinter(
                            alamatMac = device.address,
                            nama = device.name ?: "Printer Unknown",
                            sudahDipasangkan = true,
                        )
                    )
                }
            }
        }

        // Register receiver untuk hasil discovery
        val filter = android.content.IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }

        receiverScanPrinter = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
                when (intent?.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                        device ?: return
                        val nama = device.name?.lowercase() ?: ""
                        if (KATA_KUNCI_PRINTER_BT.any { kata -> nama.contains(kata) }) {
                            val key = device.address
                            if (namaPrinterDikenal.add(key)) {
                                daftarPrinter.add(
                                    HasilScanPrinter(
                                        alamatMac = device.address,
                                        nama = device.name ?: "Printer Unknown",
                                        sudahDipasangkan = device.bondState == BluetoothDevice.BOND_BONDED,
                                    )
                                )
                            }
                        }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        callback.onHasilScan(daftarPrinter.toList())
                        hentikanScanPrinter()
                    }
                }
            }
        }

        konteks.registerReceiver(receiverScanPrinter, filter)
        adapter.startDiscovery()
    }

    /**
     * Menghentikan scan printer Bluetooth yang sedang berjalan.
     */
    @SuppressLint("MissingPermission")
    fun hentikanScanPrinter() {
        try {
            bluetoothAdapter?.cancelDiscovery()
        } catch (_: Exception) {}
        try {
            receiverScanPrinter?.let { konteks.unregisterReceiver(it) }
        } catch (_: Exception) {}
        receiverScanPrinter = null
    }

    /**
     * Memasangkan (pair) perangkat Bluetooth secara programmatic.
     * Mengembalikan true jika pairing berhasil/dimulai.
     */
    @SuppressLint("MissingPermission")
    fun pasangkanPerangkat(alamatMac: String): Boolean {
        val adapter = bluetoothAdapter ?: return false
        val device = adapter.getRemoteDevice(alamatMac) ?: return false

        return try {
            // Mulai pairing — user akan melihat dialog konfirmasi di Android
            device.createBond()
            true
        } catch (e: SecurityException) {
            false
        }
    }
}
