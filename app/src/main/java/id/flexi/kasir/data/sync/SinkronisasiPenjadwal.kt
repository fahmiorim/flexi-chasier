package id.flexi.kasir.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Penjadwal sinkronisasi berbasis WorkManager.
 *
 * - [jadwalkanBerkala]: kerja periodik tiap 15 menit (unik, tidak dobel).
 * - [mintaSinkronisasiSekarang]: kerja sekali jalan (menimpa yang tertunda)
 *   untuk mensinkronkan segera setelah login/pilih gerai atau perubahan penting.
 *
 * Keduanya mensyaratkan jaringan tersedia; bila offline, WorkManager menunda
 * sampai koneksi pulih.
 */
object SinkronisasiPenjadwal {

    private const val KERJA_PERIODIK = "sinkronisasi_periodik"
    private const val KERJA_SEKARANG = "sinkronisasi_sekarang"

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

    private fun kendala(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
}
