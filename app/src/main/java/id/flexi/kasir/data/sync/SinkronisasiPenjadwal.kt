package id.flexi.kasir.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Penjadwal sinkronisasi berbasis WorkManager + NetworkCallback.
 *
 * - [jadwalkanBerkala]: kerja periodik tiap 15 menit (unik, tidak dobel).
 * - [mintaSinkronisasiSekarang]: kerja sekali jalan (menimpa yang tertunda)
 *   untuk mensinkronkan segera setelah login/pilih gerai atau perubahan penting.
 * - [pasangPemantauJaringan]: memantau ketersediaan jaringan lewat
 *   [ConnectivityManager.NetworkCallback]; saat jaringan baru tersedia,
 *   sinkronisasi otomatis dijalankan tanpa perlu tombol.
 *
 * WorkManager sudah mensyaratkan jaringan tersedia; bila offline, WorkManager
 * menunda sampai koneksi pulih. NetworkCallback memberikan respons lebih cepat
 * saat jaringan baru muncul (tanpa menunggu siklus periodik berikutnya).
 */
object SinkronisasiPenjadwal {

    private const val KERJA_PERIODIK = "sinkronisasi_periodik"
    private const val KERJA_SEKARANG = "sinkronisasi_sekarang"

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    fun jadwalkanBerkala(konteks: Context) {
        val permintaan = PeriodicWorkRequestBuilder<SinkronisasiWorker>(
            15,
            TimeUnit.MINUTES,
        )
            .setConstraints(kendala())
            .build()

        WorkManager.getInstance(konteks)
            .enqueueUniquePeriodicWork(
                KERJA_PERIODIK,
                ExistingPeriodicWorkPolicy.UPDATE,
                permintaan,
            )
    }

    fun mintaSinkronisasiSekarang(konteks: Context) {
        val permintaan = OneTimeWorkRequestBuilder<SinkronisasiWorker>()
            .setConstraints(kendala())
            .build()

        WorkManager.getInstance(konteks)
            .enqueueUniqueWork(
                KERJA_SEKARANG,
                ExistingWorkPolicy.REPLACE,
                permintaan,
            )
    }

    /**
     * Memantau perubahan jaringan. Saat jaringan internet tersedia,
     * sinkronisasi otomatis dijalankan tanpa tombol.
     */
    fun pasangPemantauJaringan(konteks: Context) {
        if (networkCallback != null) return

        val connectivityManager =
            konteks.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val permintaan = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                mintaSinkronisasiSekarang(konteks)
            }
        }

        connectivityManager.registerNetworkCallback(permintaan, callback)
        networkCallback = callback
    }

    /**
     * Lepas pemantauan jaringan (misal saat aplikasi dihancurkan).
     */
    fun lepasPemantauJaringan(konteks: Context) {
        val callback = networkCallback ?: return
        val connectivityManager =
            konteks.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(callback)
        networkCallback = null
    }

    private fun kendala(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
}
