package id.flexi.kasir.domain.usecase

import id.flexi.kasir.data.auth.SesiStore
import id.flexi.kasir.domain.model.AkunUser
import id.flexi.kasir.domain.model.LebarStruk
import id.flexi.kasir.domain.model.NetworkOperationResult
import id.flexi.kasir.domain.model.PrinterType
import id.flexi.kasir.domain.model.ReceiptPrintFormat
import id.flexi.kasir.domain.model.StoreSetting
import id.flexi.kasir.domain.repository.AuthRepository
import id.flexi.kasir.domain.repository.RepositoriStoreSetting
import kotlinx.coroutines.flow.first

class LoginUser(
    private val authRepository: AuthRepository,
    private val sesiStore: SesiStore,
    private val repositoriStoreSetting: RepositoriStoreSetting,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
    ): NetworkOperationResult<AkunUser> {
        val hasil = authRepository.login(email = email, password = password)
        // Setelah login berhasil, sync pengaturan gerai dari server ke local StoreSetting
        if (hasil is NetworkOperationResult.Berhasil) {
            syncPengaturanGerai(hasil.data)
        }
        return hasil
    }

    /**
     * Sync pengaturan gerai dari server (tersimpan di SesiStore) ke local StoreSetting DataStore.
     * Ini memastikan pengaturan seperti nama usaha, logo, alamat, tagline, printer, dll
     * selalu terupdate dari server setiap kali user login.
     */
    private suspend fun syncPengaturanGerai(akun: AkunUser) {
        val geraiAktif = akun.geraiAktif ?: return
        // Ambil pengaturan lokal saat ini
        val pengaturanLokal = repositoriStoreSetting.ambilPengaturan().first()
        // Update field yang bersumber dari server (info gerai)
        val pengaturanBaru = pengaturanLokal.copy(
            // Info umum gerai
            namaUsaha = geraiAktif.nama.ifBlank { pengaturanLokal.namaUsaha },
            alamat = geraiAktif.alamat ?: pengaturanLokal.alamat,
            tagline = geraiAktif.tagline ?: pengaturanLokal.tagline,
            logoUri = geraiAktif.logoUri ?: pengaturanLokal.logoUri,
            // Struk
            strukHeader = geraiAktif.headerStruk ?: pengaturanLokal.strukHeader,
            strukFooter = geraiAktif.footerStruk ?: pengaturanLokal.strukFooter,
            lebarStruk = geraiAktif.ukuranKertas?.let { ukuran ->
                when (ukuran) {
                    "58" -> LebarStruk.Mm58
                    "80" -> LebarStruk.Mm80
                    else -> null
                }
            } ?: pengaturanLokal.lebarStruk,
            // Pajak & Biaya
            // Note: tarifPajak & biayaLayanan dari server belum ditambahkan ke StoreSetting
            // Mereka hanya tersimpan di SesiStore/GeraiTersimpan untuk saat ini
            // Printer
            printerType = geraiAktif.printerTipe?.let { tipe ->
                when (tipe.lowercase()) {
                    "bluetooth" -> PrinterType.Bluetooth
                    "usb" -> PrinterType.Usb
                    else -> PrinterType.None
                }
            } ?: pengaturanLokal.printerType,
            printerName = geraiAktif.printerNama ?: pengaturanLokal.printerName,
            // Cetak otomatis
            receiptPrintFormat = if (geraiAktif.autoCetak == true) {
                ReceiptPrintFormat.Automatic
            } else {
                pengaturanLokal.receiptPrintFormat
            },
            // Pembayaran
            PaymentMethodTunaiAktif = geraiAktif.metodePembayaran?.let { metode ->
                metode.contains("tunai", ignoreCase = true) || metode.contains("cash", ignoreCase = true)
            } ?: pengaturanLokal.PaymentMethodTunaiAktif,
            PaymentMethodQrisAktif = geraiAktif.metodePembayaran?.let { metode ->
                metode.contains("qris", ignoreCase = true) || metode.contains("qr", ignoreCase = true)
            } ?: pengaturanLokal.PaymentMethodQrisAktif,
        )
        // Simpan ke local StoreSetting
        repositoriStoreSetting.simpanPengaturan(pengaturanBaru)
    }
}
