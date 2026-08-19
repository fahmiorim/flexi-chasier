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

        // Auto-sync saat jaringan tersedia (tanpa tombol).
        SinkronisasiPenjadwal.pasangPemantauJaringan(this)

        // Minta sinkronisasi segera + hubungkan real-time setiap kali gerai aktif tersedia:
        // saat aplikasi dibuka dengan sesi aktif, atau setelah login/pilih gerai.
        scope.launch {
            kontainer.SesiStore.amatiSesi()
                .map { sesi -> sesi?.geraiAktifId }
                .distinctUntilChanged()
                .collect { geraiId ->
                    if (geraiId != null) {
                        // Sinkronisasi data dari server
                        SinkronisasiPenjadwal.mintaSinkronisasiSekarang(this@CashierApp)
                        // Hubungkan real-time push (idempoten — tidak duplikat)
                        kontainer.KlienRealtime.hubungkan()
                    } else {
                        // Tidak ada gerai aktif → putuskan koneksi real-time
                        kontainer.KlienRealtime.putuskan()
                    }
                }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        // Bersihkan koneksi real-time saat aplikasi dihancurkan
        if (::kontainer.isInitialized) {
            kontainer.KlienRealtime.putuskan()
        }
        SinkronisasiPenjadwal.lepasPemantauJaringan(this)
    }
}
