package id.flexi.kasir.print

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import id.flexi.kasir.domain.model.LebarStruk
import id.flexi.kasir.domain.model.StoreSetting
import id.flexi.kasir.domain.model.Transaction
import id.flexi.kasir.domain.util.hitungTotalAkhirTransaction
import id.flexi.kasir.domain.util.sebagaiRupiah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
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
     */
    private fun cariPrinterBluetooth(): BluetoothDevice? {
        val adapter = bluetoothAdapter ?: return null
        if (!adapter.isEnabled) return null

        val pairedDevices = adapter.bondedDevices
        return pairedDevices?.firstOrNull { device ->
            // Filter: biasanya printer thermal punya nama mengandung kata kunci
            val nama = device.name?.lowercase() ?: ""
            nama.contains("printer") || nama.contains("thermal") ||
                nama.contains("pos") || nama.contains("receipt") ||
                nama.contains("mp") || nama.contains("tm-") ||
                nama.contains("bixolon") || nama.contains("epson")
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
        var socket: BluetoothSocket? = null
        try {
            socket = device.createRfcommSocketToServiceRecord(UUID_SPP)
            socket.connect()
            val outputStream = socket.outputStream
            tulisStruk(outputStream, Transaction, pengaturanStruk)
            PrintResult.Berhasil
        } catch (e: SecurityException) {
            PrintResult.Gagal("Izin Bluetooth tidak diberikan.")
        } catch (e: Exception) {
            PrintResult.Gagal("Gagal mencetak: ${e.message}")
        } finally {
            try {
                socket?.close()
            } catch (_: Exception) {}
        }
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
        stream.write(data)
        stream.flush()
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
            outputStream.write(data)
            outputStream.flush()
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
        val lines = mutableListOf<ByteArray>()
        lines.add(EscPos.INIT)

        // ── Header ──
        lines.add(EscPos.ALIGN_CENTER)
        val namaUsahaHeader = namaUsaha.ifBlank { "FLEXI CASHIER" }
        lines.add(EscPos.FONT_SIZE_BIG)
        lines.add(namaUsahaHeader.toByteArray(charset("US-ASCII")))
        lines.add(EscPos.LF)
        if (tagline.isNotBlank()) {
            lines.add(EscPos.FONT_SIZE_NORMAL)
            lines.add(tagline.take(karakterPerBaris(LebarStruk.Mm58)).toByteArray(charset("US-ASCII")))
            lines.add(EscPos.LF)
        }
        lines.add(EscPos.FONT_SIZE_NORMAL)
        lines.add("LAPORAN PENJUALAN".toByteArray(charset("US-ASCII")))
        lines.add(EscPos.LF)
        lines.add(garis().toByteArray(charset("US-ASCII")))
        lines.add(EscPos.LF)
        lines.add(labelPeriode.toByteArray(charset("US-ASCII")))
        lines.add(EscPos.LF)
        lines.add(EscPos.LF)

        // ── Ringkasan ──
        val totalPenjualan = transaksiAktif.sumOf { it.hitungTotalAkhirTransaction() }
        val totalTunai = transaksiAktif.filter { it.paymentMethod == id.flexi.kasir.domain.model.PaymentMethod.Cash }
            .sumOf { it.hitungTotalAkhirTransaction() }
        val totalQris = transaksiAktif.filter { it.paymentMethod == id.flexi.kasir.domain.model.PaymentMethod.Qris }
            .sumOf { it.hitungTotalAkhirTransaction() }
        val jumlahItem = transaksiAktif.sumOf { t -> t.daftarCartItem.sumOf { it.jumlah } }

        lines.add(EscPos.ALIGN_LEFT)
        lines.add(EscPos.BOLD_ON)
        lines.add(tulisDuaKolom("Total Transaksi", "${transaksiAktif.size}"))
        lines.add(EscPos.LF)
        lines.add(tulisDuaKolom("Total Item", "$jumlahItem"))
        lines.add(EscPos.LF)
        lines.add(EscPos.BOLD_OFF)
        lines.add(garisTitik().toByteArray(charset("US-ASCII")))
        lines.add(EscPos.LF)
        lines.add(tulisDuaKolom("Tunai", totalTunai.sebagaiRupiah()))
        lines.add(EscPos.LF)
        lines.add(tulisDuaKolom("QRIS", totalQris.sebagaiRupiah()))
        lines.add(EscPos.LF)
        lines.add(EscPos.BOLD_ON)
        lines.add(tulisDuaKolom("TOTAL", totalPenjualan.sebagaiRupiah()))
        lines.add(EscPos.BOLD_OFF)
        lines.add(EscPos.LF)
        lines.add(EscPos.LF)

        // ── Per Tanggal ──
        val fmtTgl = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val fmtJam = SimpleDateFormat("HH:mm", Locale("id", "ID"))
        val perTanggal = transaksiAktif
            .sortedByDescending { it.waktuTransactionEpochMili }
            .groupBy { fmtTgl.format(Date(it.waktuTransactionEpochMili)) }

        perTanggal.entries.forEach { (tanggal, daftar) ->
            val subTotal = daftar.sumOf { it.hitungTotalAkhirTransaction() }
            lines.add(EscPos.BOLD_ON)
            lines.add(tulisDuaKolom(tanggal, "${daftar.size} trx"))
            lines.add(EscPos.BOLD_OFF)
            lines.add(EscPos.LF)
            lines.add(garisTitik().toByteArray(charset("US-ASCII")))
            lines.add(EscPos.LF)

            daftar.forEach { t ->
                val waktu = fmtJam.format(Date(t.waktuTransactionEpochMili))
                val idPendek = t.id.take(8).uppercase()
                val metode = if (t.paymentMethod == id.flexi.kasir.domain.model.PaymentMethod.Cash) "TN" else "QR"
                val totalTrans = t.hitungTotalAkhirTransaction().sebagaiRupiah()
                lines.add("$waktu $idPendek [$metode]".toByteArray(charset("US-ASCII")))
                lines.add(EscPos.LF)
                lines.add(EscPos.ALIGN_RIGHT)
                lines.add(totalTrans.toByteArray(charset("US-ASCII")))
                lines.add(EscPos.LF)
                lines.add(EscPos.ALIGN_LEFT)
            }

            lines.add(EscPos.BOLD_ON)
            lines.add(tulisDuaKolom("Subtotal", subTotal.sebagaiRupiah()))
            lines.add(EscPos.BOLD_OFF)
            lines.add(EscPos.LF)
            lines.add(EscPos.LF)
        }

        // ── Footer ──
        lines.add(EscPos.ALIGN_CENTER)
        lines.add(garis().toByteArray(charset("US-ASCII")))
        lines.add(EscPos.LF)
        lines.add(EscPos.FONT_SIZE_BIG)
        lines.add("Terima Kasih".toByteArray(charset("US-ASCII")))
        lines.add(EscPos.LF)
        lines.add(EscPos.FONT_SIZE_NORMAL)
        lines.add("www.flexikasir.id".toByteArray(charset("US-ASCII")))

        lines.add(EscPos.LF)
        lines.add(EscPos.LF)
        lines.add(EscPos.LF)
        lines.add(EscPos.CUT)

        return lines.fold(ByteArray(0)) { acc, bytes -> acc + bytes }
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
        val subtotal = Transaction.daftarCartItem.sumOf { item -> item.produk.harga * item.jumlah }

        val singleCopy = buildSingleByteStruk(Transaction, waktuCetak, subtotal, settings, lebar)

        // Jika hanya 1 copy, return langsung
        if (jumlahCopy <= 1) return singleCopy

        // Multiple copies: gabungkan dengan potongan kertas di antaranya
        val copies = mutableListOf<ByteArray>()
        for (i in 1..jumlahCopy) {
            if (i > 1) {
                // Feed kertas sedikit agar ada jarak antar copy
                copies.add(EscPos.LF)
                copies.add(EscPos.LF)
            }
            copies.add(buildSingleByteStruk(Transaction, waktuCetak, subtotal, settings, lebar))
        }
        return copies.fold(ByteArray(0)) { acc, bytes -> acc + bytes }
    }

    private fun buildSingleByteStruk(
        Transaction: Transaction,
        waktuCetak: String,
        subtotal: Long,
        settings: StoreSetting,
        lebar: Int,
    ): ByteArray {
        val lines = mutableListOf<ByteArray>()

        // Initialize
        lines.add(EscPos.INIT)

        // ── Header ──
        lines.add(EscPos.ALIGN_CENTER)
        val namaHeader = settings.namaUsaha.ifBlank { "FLEXI CASHIER" }
        lines.add(EscPos.FONT_SIZE_BIG)
        lines.add(namaHeader.toByteArray(charset("US-ASCII")))
        lines.add(EscPos.LF)

        // Tagline (jika ada)
        if (settings.tagline.isNotBlank()) {
            lines.add(EscPos.FONT_SIZE_NORMAL)
            lines.add(settings.tagline.take(lebar).toByteArray(charset("US-ASCII")))
            lines.add(EscPos.LF)
        }

        // Header custom
        if (settings.strukHeader.isNotBlank()) {
            lines.add(EscPos.FONT_SIZE_NORMAL)
            settings.strukHeader.lines().forEach { baris ->
                lines.add(baris.take(lebar).toByteArray(charset("US-ASCII")))
                lines.add(EscPos.LF)
            }
        }

        lines.add(garis(lebar).toByteArray(charset("US-ASCII")))
        lines.add(EscPos.LF)

        // Info transaksi
        lines.add(EscPos.ALIGN_CENTER)
        lines.add(Transaction.id.take(8).uppercase().toByteArray(charset("US-ASCII")))
        lines.add(EscPos.LF)
        lines.add(waktuCetak.toByteArray(charset("US-ASCII")))
        lines.add(EscPos.LF)
        lines.add(Transaction.orderType.name.toByteArray(charset("US-ASCII")))
        lines.add(EscPos.LF)
        Transaction.nomorAntrian?.let {
            lines.add(EscPos.BOLD_ON)
            lines.add("No. Antrian: $it".toByteArray(charset("US-ASCII")))
            lines.add(EscPos.BOLD_OFF)
            lines.add(EscPos.LF)
        }

        lines.add(garis(lebar).toByteArray(charset("US-ASCII")))
        lines.add(EscPos.LF)

        // Daftar item
        lines.add(EscPos.ALIGN_LEFT)
        val maxNamaPanjang = (lebar * 0.6).toInt().coerceIn(15, 30)
        Transaction.daftarCartItem.forEach { item ->
            val nama = item.produk.nama.take(maxNamaPanjang)
            val hargaStr = item.produk.harga.sebagaiRupiah()
            val subtotalItem = (item.produk.harga * item.jumlah).sebagaiRupiah()

            lines.add(nama.toByteArray(charset("US-ASCII")))
            lines.add(EscPos.LF)
            lines.add(EscPos.BOLD_ON)
            val barisItem = "${item.jumlah} x $hargaStr"
            val padding = " ".repeat(maxOf(0, lebar - barisItem.length - subtotalItem.length - 2))
            lines.add("$barisItem$padding$subtotalItem".toByteArray(charset("US-ASCII")))
            lines.add(EscPos.BOLD_OFF)
            lines.add(EscPos.LF)
        }

        lines.add(garis(lebar).toByteArray(charset("US-ASCII")))
        lines.add(EscPos.LF)

        // Rincian biaya
        lines.add(EscPos.ALIGN_LEFT)
        lines.add(tulisDuaKolom("Subtotal", subtotal.sebagaiRupiah(), lebar))
        lines.add(EscPos.LF)
        if (Transaction.potongan.nilaiRupiah > 0) {
            lines.add(tulisDuaKolom("Potongan", "-${Transaction.potongan.sebagaiRupiah()}", lebar))
            lines.add(EscPos.LF)
        }
        if (settings.tampilkanPajakDiStruk) {
            if (Transaction.biayaLayanan.nilaiRupiah > 0) {
                lines.add(tulisDuaKolom("Biaya Layanan", Transaction.biayaLayanan.sebagaiRupiah(), lebar))
                lines.add(EscPos.LF)
            }
            if (Transaction.pajak.nilaiRupiah > 0) {
                lines.add(tulisDuaKolom("Pajak", Transaction.pajak.sebagaiRupiah(), lebar))
                lines.add(EscPos.LF)
            }
        }

        lines.add(garis(lebar).toByteArray(charset("US-ASCII")))
        lines.add(EscPos.LF)

        // Total
        val totalVal = subtotal - Transaction.potongan.nilaiRupiah
            .coerceAtMost(subtotal) + Transaction.biayaLayanan.nilaiRupiah + Transaction.pajak.nilaiRupiah
        lines.add(EscPos.BOLD_ON)
        lines.add(tulisDuaKolom("Total", totalVal.sebagaiRupiah(), lebar))
        lines.add(EscPos.BOLD_OFF)
        lines.add(EscPos.LF)

        // Catatan
        if (!Transaction.catatan.isNullOrBlank()) {
            lines.add(garis(lebar).toByteArray(charset("US-ASCII")))
            lines.add(EscPos.LF)
            lines.add(EscPos.ALIGN_CENTER)
            lines.add(EscPos.BOLD_ON)
            lines.add("Catatan:".toByteArray(charset("US-ASCII")))
            lines.add(EscPos.BOLD_OFF)
            lines.add(EscPos.LF)
            lines.add(Transaction.catatan.toByteArray(charset("US-ASCII")))
            lines.add(EscPos.LF)
        }

        // Footer custom
        if (settings.strukFooter.isNotBlank()) {
            lines.add(garis(lebar).toByteArray(charset("US-ASCII")))
            lines.add(EscPos.LF)
            lines.add(EscPos.ALIGN_CENTER)
            settings.strukFooter.lines().forEach { baris ->
                lines.add(baris.take(lebar).toByteArray(charset("US-ASCII")))
                lines.add(EscPos.LF)
            }
        }

        // Footer standar
        lines.add(EscPos.ALIGN_CENTER)
        lines.add(garis(lebar).toByteArray(charset("US-ASCII")))
        lines.add(EscPos.LF)
        lines.add(EscPos.FONT_SIZE_BIG)
        lines.add("Terima Kasih".toByteArray(charset("US-ASCII")))
        lines.add(EscPos.LF)
        lines.add(EscPos.FONT_SIZE_NORMAL)
        lines.add("www.flexikasir.id".toByteArray(charset("US-ASCII")))

        // Feed + Cut
        lines.add(EscPos.LF)
        lines.add(EscPos.LF)
        lines.add(EscPos.LF)
        lines.add(EscPos.CUT)

        return lines.fold(ByteArray(0)) { acc, bytes -> acc + bytes }
    }

    /**
     * Membuat baris teks dengan dua kolom (kiri dan kanan).
     */
    private fun tulisDuaKolom(kiri: String, kanan: String, lebar: Int = 32): ByteArray {
        val maxPanjang = lebar
        val bersihKiri = kiri.take(maxPanjang - kanan.length - 2)
        val padding = " ".repeat(maxOf(0, maxPanjang - bersihKiri.length - kanan.length))
        return "$bersihKiri$padding$kanan".toByteArray(charset("US-ASCII"))
    }

    private fun garis(lebar: Int = 32): String = "=".repeat(lebar)

    private fun garisTitik(lebar: Int = 32): String = "-".repeat(lebar)


}
