package id.flexi.kasir.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import id.flexi.kasir.CashierApp

/**
 * Worker sinkronisasi latar belakang (dijalankan WorkManager).
 *
 * Membaca gerai aktif dari sesi lalu menjalankan mesin sinkronisasi
 * (dorong outbox + tarik perubahan). Berjalan berkala dan juga saat
 * diminta aplikasi setelah gerai dipilih / setelah perubahan penting.
 *
 * - Berhasil → [Result.success]
 * - Sesi/akses bermasalah (401/403) → [Result.failure] (berhenti diproses)
 * - Gagal sementara → [Result.retry] (dijadwalkan ulang oleh WorkManager)
 */
class SinkronisasiWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val kontainer = (applicationContext as CashierApp).kontainer
        val hasil = kontainer.MesinSinkronisasi.sinkronkanGeraiAktif()

        return when {
            hasil.berhasil -> Result.success()
            hasil.kodeError == 401 || hasil.kodeError == 403 -> {
                // Token/sesi tidak valid — bersihkan sesi agar aplikasi
                // navigasi ke layar login.
                kontainer.TokenStore.hapus()
                kontainer.SesiStore.hapus()
                Result.failure()
            }
            else -> Result.retry()
        }
    }
}
