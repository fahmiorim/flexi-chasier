package id.flexi.kasir

import android.app.Application
import id.flexi.kasir.data.sync.SinkronisasiPenjadwal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Kelas Application utama untuk Flexi Cashier.
 * Berfungsi sebagai titik pusat inisialisasi dan akses dependensi global.
 */
class CashierApp : Application() {

    /**
     * Kontainer dependensi (Service Locator) yang dapat diakses di seluruh aplikasi.
     * Menggunakan delegasi properti untuk inisialisasi pada saat onCreate.
     */
    lateinit var kontainer: CashierDependencyContainer
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        // Inisialisasi kontainer dependensi saat aplikasi pertama kali dibuat
        kontainer = CashierDependencyContainer(this)

        // Sinkronisasi berkala di latar belakang (outbox push + pull perubahan).
        SinkronisasiPenjadwal.jadwalkanBerkala(this)

        // Jika sudah ada sesi dengan gerai aktif, langsung minta sinkronisasi sekali.
        scope.launch {
            val sesi = kontainer.SesiStore.amatiSesi().first()
            if (sesi?.geraiAktifId != null) {
                SinkronisasiPenjadwal.mintaSinkronisasiSekarang(this@CashierApp)
            }
        }
    }
}
