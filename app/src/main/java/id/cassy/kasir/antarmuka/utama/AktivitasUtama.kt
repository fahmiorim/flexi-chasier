package id.cassy.kasir.antarmuka.utama

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import id.cassy.kasir.antarmuka.AplikasiCassyKasir

/**
 * Titik masuk utama aplikasi CassyKasir.
 * Mengelola siklus hidup aktivitas utama dan menginisialisasi tampilan Compose.
 */
class AktivitasUtama : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Mengaktifkan fitur Edge-to-Edge untuk tampilan yang lebih imersif dan modern
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            // Memulai fungsi komposabel utama aplikasi
            AplikasiCassyKasir()
        }
    }
}
