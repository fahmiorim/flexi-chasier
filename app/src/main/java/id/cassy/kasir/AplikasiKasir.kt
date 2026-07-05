package id.cassy.kasir

import android.app.Application

/**
 * Kelas Application utama untuk Cassy Kasir.
 * Berfungsi sebagai titik pusat inisialisasi dan akses dependensi global.
 */
class AplikasiKasir : Application() {

    /**
     * Kontainer dependensi (Service Locator) yang dapat diakses di seluruh aplikasi.
     * Menggunakan delegasi properti untuk inisialisasi pada saat onCreate.
     */
    lateinit var kontainer: GudangDependensiKasir
        private set

    override fun onCreate() {
        super.onCreate()
        // Inisialisasi kontainer dependensi saat aplikasi pertama kali dibuat
        kontainer = GudangDependensiKasir(this)
    }
}
