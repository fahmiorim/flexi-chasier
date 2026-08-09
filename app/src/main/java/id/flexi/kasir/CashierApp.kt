package id.flexi.kasir

import android.app.Application
import id.flexi.kasir.data.sync.SinkronisasiPenjadwal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Kelas Application utama untuk Flexi Kasir.
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

        // Minta sinkronisasi segera setiap kali gerai aktif tersedia: saat aplikasi
        // dibuka dengan sesi aktif, atau setelah login/pilih gerai. Dengan begitu
        // katalog produk & data lain langsung terisi dari server — pengguna baru
        // tidak perlu menunggu siklus berkala untuk melihat katalognya.
        scope.launch {
            kontainer.SesiStore.amatiSesi()
                .map { sesi -> sesi?.geraiAktifId }
                .distinctUntilChanged()
                .collect { geraiId ->
                    if (geraiId != null) {
                        SinkronisasiPenjadwal.mintaSinkronisasiSekarang(this@CashierApp)
                    }
                }
        }
    }
}
